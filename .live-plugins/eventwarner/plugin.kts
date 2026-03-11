import com.intellij.codeInspection.*
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.project.Project
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.impl.ProjectViewPane
import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.reference.RefManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import liveplugin.PluginUtil
import liveplugin.implementation.Console
import liveplugin.registerInspection
import liveplugin.show
import liveplugin.showInConsole
import java.io.File
import java.util.concurrent.ConcurrentHashMap

// depends-on-plugin com.intellij.java

// Register both local and global inspections
registerInspection(EventVersionConsistencyLocalInspection()) // Local inspection

if (!isIdeStartup) {
    show("Event Version Consistency Inspections loaded<br/>Local: Real-time analysis as you edit<br/>Global: Project-wide analysis on demand")
}
val console = liveplugin.implementation.showInConsole("Hi", "Migration Helper", project!!)



class EventVersionConsistencyInspection : GlobalInspectionTool() {

    override fun runInspection(
        scope: AnalysisScope,
        manager: InspectionManager,
        globalContext: GlobalInspectionContext,
        problemDescriptorsProcessor: ProblemDescriptionsProcessor
    ) {
        val project = globalContext.project

        // Log start of analysis
        ApplicationManager.getApplication().invokeLater {
            console.print("Starting global event version consistency analysis...\n", Console.guessContentTypeOf("Starting global event version consistency analysis...\n"))
        }

        try {
            // Find all UEvent subclasses in the project
            val eventClasses = findAllUEventSubclasses(project, scope)

            ApplicationManager.getApplication().invokeLater {
                console.print("Found ${eventClasses.size} UEvent classes for analysis\n", Console.guessContentTypeOf("Found ${eventClasses.size} UEvent classes for analysis\n"))
            }

            // Get version directories
            val versionDirectories = getVersionDirectories(project)

            // Analyze each event class
            eventClasses.forEach { eventClass ->
                analyzeEventForGlobalInspection(eventClass, versionDirectories, manager, problemDescriptorsProcessor, globalContext)
            }

            ApplicationManager.getApplication().invokeLater {
                console.print("Global event version consistency analysis complete!\n", Console.guessContentTypeOf("Global event version consistency analysis complete!\n"))
            }

        } catch (e: Exception) {
            ApplicationManager.getApplication().invokeLater {
                console.print("Error in global inspection: ${e.message}\n", Console.guessContentTypeOf("Error in global inspection: ${e.message}\n"))
            }
            e.printStackTrace()
        }
    }

    private fun findAllUEventSubclasses(project: Project, scope: AnalysisScope): List<PsiClass> {
        val eventClasses = mutableListOf<PsiClass>()

        scope.accept(object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file is PsiJavaFile) {
                    file.classes.forEach { psiClass ->
                        if (isUEventSubclass(psiClass)) {
                            eventClasses.add(psiClass)
                        }
                    }
                }
            }
        })

        return eventClasses
    }

    private fun analyzeEventForGlobalInspection(
        aClass: PsiClass,
        versionDirectories: List<String>,
        manager: InspectionManager,
        processor: ProblemDescriptionsProcessor,
        globalContext: GlobalInspectionContext
    ) {
        val eventName = aClass.name ?: return
        val project = globalContext.project

        try {
            if (versionDirectories.isEmpty()) {
                val fallbackVersions = listOf("1.8.9", "1.21.11", "1.21.5")
                reportGlobalProblem(aClass, eventName, emptySet(), fallbackVersions.toSet(), fallbackVersions,
                    manager, processor, globalContext, "no version directories found")
                return
            }

            // Search for references to this specific event class
            val references = ReferencesSearch.search(aClass, GlobalSearchScope.projectScope(project)).findAll()

            val versionsWithEvent = mutableSetOf<String>()

            // Group references by version directory
            versionDirectories.forEach { version ->
                val versionScope = createVersionScope(project, version)
                val referencesInVersion = references.filter { ref ->
                    val file = ref.element.containingFile?.virtualFile
                    file != null && versionScope.contains(file)
                }

                if (referencesInVersion.isNotEmpty()) {
                    versionsWithEvent.add(version)
                }
            }

            val versionsWithoutEvent = versionDirectories.toSet() - versionsWithEvent

            // Log the analysis asynchronously
            ApplicationManager.getApplication().invokeLater {
                console.print("Analyzed '$eventName': Found in ${versionsWithEvent.size}/${versionDirectories.size} versions\n", Console.guessContentTypeOf("Analyzed '$eventName': Found in ${versionsWithEvent.size}/${versionDirectories.size} versions\n"))
                if (versionsWithEvent.isNotEmpty()) {
                    console.print("  Present in: ${versionsWithEvent.sorted().joinToString(", ")}\n", Console.guessContentTypeOf("  Present in: ${versionsWithEvent.sorted().joinToString(", ")}\n"))
                }
                if (versionsWithoutEvent.isNotEmpty()) {
                    console.print("  Missing in: ${versionsWithoutEvent.sorted().joinToString(", ")}\n", Console.guessContentTypeOf("  Missing in: ${versionsWithoutEvent.sorted().joinToString(", ")}\n"))
                }
            }

            reportGlobalProblem(aClass, eventName, versionsWithEvent, versionsWithoutEvent, versionDirectories,
                manager, processor, globalContext, null)

        } catch (e: Exception) {
            ApplicationManager.getApplication().invokeLater {
                console.print("Error analyzing '$eventName': ${e.message}\n", Console.guessContentTypeOf("Error analyzing '$eventName': ${e.message}\n"))
            }
            e.printStackTrace()

            reportGlobalProblem(aClass, eventName, emptySet(), emptySet(), emptyList(),
                manager, processor, globalContext, "analysis failed: ${e.message}")
        }
    }

    private fun reportGlobalProblem(
        aClass: PsiClass,
        eventName: String,
        versionsWithEvent: Set<String>,
        versionsWithoutEvent: Set<String>,
        allVersions: List<String>,
        manager: InspectionManager,
        processor: ProblemDescriptionsProcessor,
        globalContext: GlobalInspectionContext,
        errorMessage: String?
    ) {
        val versionStatusString = if (allVersions.isNotEmpty()) {
            buildVersionStatusString(versionsWithEvent, versionsWithoutEvent, allVersions)
        } else {
            ""
        }

        val (message, severity) = when {
            errorMessage != null -> {
                "Event '$eventName' - $errorMessage" to ProblemHighlightType.ERROR
            }
            versionsWithEvent.isEmpty() -> {
                "Event '$eventName' is not used in any version $versionStatusString" to ProblemHighlightType.ERROR
            }
            versionsWithoutEvent.isNotEmpty() -> {
                "Event '$eventName' missing in ${versionsWithoutEvent.size} version(s) $versionStatusString" to ProblemHighlightType.ERROR
            }
            else -> {
                "Event '$eventName' implemented in all versions $versionStatusString" to ProblemHighlightType.INFORMATION
            }
        }

        // Create problem descriptor for the class
        val classProblem = manager.createProblemDescriptor(
            aClass.nameIdentifier ?: aClass,
            message,
            null as LocalQuickFix?,
            severity,
            false
        )

        // Create problem descriptor for the file (this will show in project tree)
        val fileProblem = manager.createProblemDescriptor(
            aClass.containingFile,
            "Event file: $message",
            null as LocalQuickFix?,
            severity,
            false
        )

        // Report both problems
        val refManager = globalContext.refManager
        processor.addProblemElement(refManager.getReference(aClass), classProblem)
        processor.addProblemElement(refManager.getReference(aClass.containingFile), fileProblem)
    }

    private fun isUEventSubclass(aClass: PsiClass): Boolean {
        // Skip abstract classes, interfaces, and generic type parameters
        if (aClass.isInterface || aClass.hasModifierProperty(PsiModifier.ABSTRACT) || aClass is PsiTypeParameter) {
            return false
        }

        // Skip classes that are just generic bounds (like T extends UEvent)
        if (aClass.name?.length == 1) { // Single letter names are likely generic type parameters
            return false
        }

        // Check if class extends UEvent directly
        val superClass = aClass.superClass
        if (superClass?.qualifiedName == "kr.syeyoung.modapi.event.UEvent") {
            return true
        }

        // Check inheritance chain for UEvent
        var current = superClass
        while (current != null && current.qualifiedName != "java.lang.Object") {
            if (current.qualifiedName == "kr.syeyoung.modapi.event.UEvent") {
                return true
            }
            current = current.superClass
        }

        return false
    }

    private fun buildVersionStatusString(versionsWithEvent: Set<String>, versionsWithoutEvent: Set<String>, allVersions: List<String>): String {
        return buildString {
            append("[ ")
            allVersions.sorted().forEach { version ->
                val status = if (versionsWithEvent.contains(version)) "✅" else "❌"
                append("$status$version ")
            }
            append("]")
        }
    }

    private fun getVersionDirectories(project: Project): List<String> {
        val projectPath = project.basePath ?: return emptyList()
        val versionsDir = File(projectPath, "versions")

        return if (versionsDir.exists()) {
            versionsDir.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: emptyList()
        } else {
            listOf("1.8.9", "1.21.11", "1.21.5")
        }
    }

    private fun createVersionScope(project: Project, version: String): GlobalSearchScope {
        val projectPath = project.basePath ?: return GlobalSearchScope.EMPTY_SCOPE
        val versionDir = File(projectPath, "versions/$version")

        if (!versionDir.exists()) {
            return GlobalSearchScope.EMPTY_SCOPE
        }

        // Create a scope that includes only files under the version directory
        val versionVirtualDir = project.baseDir?.findChild("versions")?.findChild(version)

        return if (versionVirtualDir != null) {
            GlobalSearchScope.filesScope(project, collectVirtualFiles(versionVirtualDir))
        } else {
            GlobalSearchScope.EMPTY_SCOPE
        }
    }

    private fun collectVirtualFiles(dir: VirtualFile): List<VirtualFile> {
        val files = mutableListOf<VirtualFile>()

        fun collectRecursively(currentDir: VirtualFile) {
            currentDir.children.forEach { child ->
                if (child.isDirectory) {
                    collectRecursively(child)
                } else {
                    files.add(child)
                }
            }
        }

        collectRecursively(dir)
        return files
    }


    override fun getDisplayName() = "Event Version Consistency"
    override fun getShortName() = "EventVersionConsistency"
    override fun getGroupDisplayName() = "Event Warner"
    override fun isEnabledByDefault() = true
}

// Local inspection for real-time analysis
class EventVersionConsistencyLocalInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : JavaElementVisitor() {
            override fun visitClass(aClass: PsiClass) {
                super.visitClass(aClass)

                // Only analyze UEvent subclasses
                if (isUEventSubclass(aClass)) {
                    val eventName = aClass.name ?: return
                    analyzeEventUsageLocal(holder, aClass, eventName)
                }
            }
        }

    private fun isUEventSubclass(aClass: PsiClass): Boolean {
        // Skip abstract classes, interfaces, and generic type parameters
        if (aClass.isInterface || aClass.hasModifierProperty(PsiModifier.ABSTRACT) || aClass is PsiTypeParameter) {
            return false
        }

        // Skip classes that are just generic bounds (like T extends UEvent)
        if (aClass.name?.length == 1) { // Single letter names are likely generic type parameters
            return false
        }

        // Check if class extends UEvent directly
        val superClass = aClass.superClass
        if (superClass?.qualifiedName == "kr.syeyoung.modapi.event.UEvent") {
            return true
        }

        // Check inheritance chain for UEvent
        var current: PsiClass? = superClass
        while (current != null && current?.qualifiedName != "java.lang.Object") {
            if (current?.qualifiedName == "kr.syeyoung.modapi.event.UEvent") {
                return true
            }
            current = current?.superClass
        }

        return false
    }

    private fun analyzeEventUsageLocal(holder: ProblemsHolder, aClass: PsiClass, eventName: String) {
        val project = aClass.project

        try {
            // Get version directories
            val versionDirectories = getVersionDirectories(project)

            if (versionDirectories.isEmpty()) {
                val fallbackVersions = listOf("1.8.9", "1.21.11", "1.21.5")
                val versionStatusString = buildVersionStatusString(emptySet(), fallbackVersions.toSet(), fallbackVersions)

                holder.registerProblem(
                    aClass.nameIdentifier ?: aClass,
                    "Event '$eventName' - no version directories found $versionStatusString",
                    ProblemHighlightType.WEAK_WARNING,
                    EventVersionTooltipQuickFix(eventName, emptySet(), fallbackVersions.toSet(), fallbackVersions)
                )
                return
            }

            // Search for references to this specific event class
            val references = ReferencesSearch.search(aClass, GlobalSearchScope.projectScope(project)).findAll()
            val versionsWithEvent = mutableSetOf<String>()

            // Group references by version directory
            versionDirectories.forEach { version ->
                val versionScope = createVersionScope(project, version)
                val referencesInVersion = references.filter { ref ->
                    val file = ref.element.containingFile?.virtualFile
                    file != null && versionScope.contains(file)
                }

                if (referencesInVersion.isNotEmpty()) {
                    versionsWithEvent.add(version)
                }
            }

            val versionsWithoutEvent = versionDirectories.toSet() - versionsWithEvent
            val versionStatusString = buildVersionStatusString(versionsWithEvent, versionsWithoutEvent, versionDirectories)

            // Log analysis results asynchronously
            ApplicationManager.getApplication().invokeLater {
                console.print("[Local] Analyzed '$eventName': Found in ${versionsWithEvent.size}/${versionDirectories.size} versions $versionStatusString\n",
                    Console.guessContentTypeOf("Local analysis result"))
                console.print("is physiucal? ${aClass.isPhysical}", Console.guessContentTypeOf("Local analysis detail"))
            }

            // Register problems based on usage
            when {
                versionsWithEvent.isEmpty() -> {
                    holder.registerProblem(
                        aClass.nameIdentifier ?: aClass,
                        "Event '$eventName' is not used in any version $versionStatusString",
                        ProblemHighlightType.GENERIC_ERROR,
                        EventVersionTooltipQuickFix(eventName, versionsWithEvent, versionsWithoutEvent, versionDirectories)
                    )
                }
                versionsWithoutEvent.isNotEmpty() -> {
                    val message = "Event '$eventName' missing in ${versionsWithoutEvent.size} version(s) $versionStatusString"
                    holder.registerProblem(
                        aClass.nameIdentifier ?: aClass,
                        message,
                        ProblemHighlightType.GENERIC_ERROR,
                        EventVersionTooltipQuickFix(eventName, versionsWithEvent, versionsWithoutEvent, versionDirectories)
                    )
                }
                else -> {
                    // All versions have it - show success
                    holder.registerProblem(
                        aClass.nameIdentifier ?: aClass,
                        "Event '$eventName' implemented in all versions $versionStatusString",
                        ProblemHighlightType.INFORMATION,
                        EventVersionTooltipQuickFix(eventName, versionsWithEvent, versionsWithoutEvent, versionDirectories)
                    )
                }
            }

        } catch (e: Exception) {
            ApplicationManager.getApplication().invokeLater {
                console.print("[Local] Error analyzing '$eventName': ${e.message}\n",
                    Console.guessContentTypeOf("Local analysis error"))
            }

            holder.registerProblem(
                aClass.nameIdentifier ?: aClass,
                "Event '$eventName' - local analysis failed: ${e.message}",
                ProblemHighlightType.ERROR,
                EventVersionTooltipQuickFix(eventName, emptySet(), emptySet(), emptyList())
            )
        }
    }

    private fun buildVersionStatusString(versionsWithEvent: Set<String>, versionsWithoutEvent: Set<String>, allVersions: List<String>): String {
        return buildString {
            append("[ ")
            allVersions.sorted().forEach { version ->
                val status = if (versionsWithEvent.contains(version)) "✅" else "❌"
                append("$status$version ")
            }
            append("]")
        }
    }

    private fun getVersionDirectories(project: Project): List<String> {
        val projectPath = project.basePath ?: return emptyList()
        val versionsDir = File(projectPath, "versions")

        return if (versionsDir.exists()) {
            versionsDir.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: emptyList()
        } else {
            listOf("1.8.9", "1.21.11", "1.21.5")
        }
    }

    private fun createVersionScope(project: Project, version: String): GlobalSearchScope {
        val projectPath = project.basePath ?: return GlobalSearchScope.EMPTY_SCOPE
        val versionDir = File(projectPath, "versions/$version")

        if (!versionDir.exists()) {
            return GlobalSearchScope.EMPTY_SCOPE
        }

        val versionVirtualDir = project.baseDir?.findChild("versions")?.findChild(version)

        return if (versionVirtualDir != null) {
            GlobalSearchScope.filesScope(project, collectVirtualFiles(versionVirtualDir))
        } else {
            GlobalSearchScope.EMPTY_SCOPE
        }
    }

    private fun collectVirtualFiles(dir: VirtualFile): List<VirtualFile> {
        val files = mutableListOf<VirtualFile>()

        fun collectRecursively(currentDir: VirtualFile) {
            currentDir.children.forEach { child ->
                if (child.isDirectory) {
                    collectRecursively(child)
                } else {
                    files.add(child)
                }
            }
        }

        collectRecursively(dir)
        return files
    }

    override fun getDisplayName() = "Event Version Consistency (Local)"
    override fun getShortName() = "EventVersionConsistencyLocal"
    override fun getGroupDisplayName() = "Event Warner"
    override fun isEnabledByDefault() = true
}

class EventVersionTooltipQuickFix(
    private val eventName: String,
    private val versionsWithEvent: Set<String>,
    private val versionsWithoutEvent: Set<String>,
    private val allVersions: List<String>
) : LocalQuickFix {

    override fun getName(): String = "Show version status"
    override fun getFamilyName(): String = name

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val tooltipMessage = buildString {
            appendLine("=== Event Version Status: $eventName ===")
            appendLine()

            allVersions.sorted().forEach { version ->
                val status = if (versionsWithEvent.contains(version)) "✅" else "❌"
                val statusText = if (versionsWithEvent.contains(version)) "PRESENT" else "MISSING"
                appendLine("$status $version: $statusText")
            }

            appendLine()
            appendLine("Summary:")
            appendLine("✅ Present in: ${versionsWithEvent.size}/${allVersions.size} versions")
            appendLine("❌ Missing in: ${versionsWithoutEvent.size}/${allVersions.size} versions")

            if (versionsWithoutEvent.isNotEmpty()) {
                appendLine()
                appendLine("🔧 Implementation needed in:")
                versionsWithoutEvent.sorted().forEach { version ->
                    appendLine("  - versions/$version/src/main/java/.../EventListener.java")
                }
            }
        }

        show(tooltipMessage)
        console.print(tooltipMessage, Console.guessContentTypeOf(tooltipMessage))
    }
}

class ShowEventUsageQuickFix(
    private val eventName: String,
    private val versionsWithEvent: Set<String>,
    private val versionsWithoutEvent: Set<String>
) : LocalQuickFix {

    override fun getName(): String = "Show detailed event usage analysis"
    override fun getFamilyName(): String = name

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val message = buildString {
            appendLine("=== Detailed Analysis: $eventName ===")
            appendLine()

            appendLine("📊 Version Status:")
            val allVersions = setOf("1.8.9", "1.21.11", "1.21.5")
            allVersions.sorted().forEach { version ->
                val status = if (versionsWithEvent.contains(version)) "✅ PRESENT" else "❌ MISSING"
                appendLine("  $version: $status")
            }
            appendLine()

            if (versionsWithEvent.isNotEmpty()) {
                appendLine("✅ Versions with implementation:")
                versionsWithEvent.sorted().forEach { version ->
                    appendLine("  - $version")
                    appendLine("    Check: versions/$version/src/main/java/.../EventListener.java")
                    appendLine("    Check: versions/$version/src/main/java/.../PacketListener.java")
                }
                appendLine()
            }

            if (versionsWithoutEvent.isNotEmpty()) {
                appendLine("❌ Versions missing implementation:")
                versionsWithoutEvent.sorted().forEach { version ->
                    appendLine("  - $version")
                }
                appendLine()

                appendLine("🔧 Implementation Guide:")
                if (versionsWithEvent.isNotEmpty()) {
                    appendLine("1. Study the implementation in: ${versionsWithEvent.first()}")
                    appendLine("2. Look for fireEvent() calls with $eventName")
                    appendLine("3. Identify the trigger conditions")
                    appendLine("4. Implement equivalent logic in missing versions")
                    appendLine("5. Handle platform differences:")
                    appendLine("   - Forge (1.8.9): Uses MinecraftForge events")
                    appendLine("   - Fabric (1.21.x): Uses Fabric API events")
                } else {
                    appendLine("1. Determine if this event should be implemented")
                    appendLine("2. Identify when/where it should be fired")
                    appendLine("3. Add fireEvent() calls in appropriate locations")
                }
                appendLine()

                appendLine("📁 Files to modify:")
                versionsWithoutEvent.sorted().forEach { version ->
                    appendLine("  - versions/$version/src/main/java/.../EventListener.java")
                    appendLine("  - versions/$version/src/main/java/.../PacketListener.java")
                }
            }

            if (versionsWithEvent.isEmpty()) {
                appendLine("⚠️ Event appears unused across all versions!")
                appendLine()
                appendLine("Possible reasons:")
                appendLine("1. New event that hasn't been implemented yet")
                appendLine("2. Event is fired conditionally and wasn't detected")
                appendLine("3. Event is used in a different way (reflection, etc.)")
                appendLine("4. Legacy event that should be removed")
                appendLine()
                appendLine("🔍 Manual verification needed:")
                appendLine("1. Search for '$eventName' across the entire codebase")
                appendLine("2. Check if it's used in configuration or reflection")
                appendLine("3. Verify if it's a new event that needs implementation")
            }
        }

        show(message)
        console.print(message, Console.guessContentTypeOf(message))
    }
}
