
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.*
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import liveplugin.PluginUtil.showInConsole
import liveplugin.registerInspection
import liveplugin.show

// depends-on-plugin com.intellij.java

// See also com.siyeh.ig.fixes.MakeFieldFinalFix in IntelliJ sources.

registerInspection(EventInspection())
if (!isIdeStartup) {
    show("Loaded hello world inspection<br/>It replaces \"hello\" string literal in Java code with \"Hello world\"")
}
showInConsole("Hi", "Migration Helper", project!!)

fun PsiElement.isMod(): Boolean {
    val module = ModuleUtilCore.findModuleForPsiElement(this)
    if (module?.name != "Dungeons_Guide.mod.main") return false
    return true
}

fun PsiClass?.isBad(): Boolean {
    if (this == null) return false;
    val qualifiedName = this.qualifiedName ?: return false
    if (qualifiedName.startsWith("net.minecraft")) return true;
    if (qualifiedName.startsWith("org.lwjgl")) return true;
    return false;
}


fun addImportIfNeeded(file: PsiFile, qualifiedName: String) {
    if (file !is PsiJavaFile) return

    val clazz = JavaPsiFacade.getInstance(file.project).findClass(qualifiedName, file.resolveScope)!!;
    println(clazz)
    JavaCodeStyleManager.getInstance(project).addImport(file, clazz)
//    JavaCodeStyleManager.getInstance(project).optimizeImports(file)
}

//class MinecraftInspection: AbstractBaseJavaLocalInspectionTool() {
//    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object: JavaElementVisitor() {
//        override fun visitReferenceExpression(expression: PsiReferenceExpression) {
//            if (!expression.isMod()) return
//            val resolved = expression.resolve() ?: return
//            val bad = when(resolved) {
//                is PsiField -> resolved.containingClass.isBad()
//                is PsiMethod -> resolved.containingClass.isBad()
//                is PsiClass -> resolved.isBad()
//                else -> false
//            }
//
//            if (bad) {
//                holder.registerProblem(expression, "Direct call to Minecraft")
//            }
//        }
//    }
//
//    override fun getDisplayName() = "Encapsulate in API"
//    override fun getShortName() = "APIEncapsulationInspection"
//    override fun getGroupDisplayName() = "Migration Helper"
//    override fun isEnabledByDefault() = true
//}

class ReplaceEventBusPostProcessor222(
    val project: Project,
    val callsToReplace: Array<PsiMethodCallExpression>
): BaseRefactoringProcessor(project) {
    override fun findUsages(): Array<UsageInfo> = callsToReplace.map {  UsageInfo(it) }.toTypedArray()
    override fun createUsageViewDescriptor(p0: Array<out UsageInfo>): UsageViewDescriptor {
        println(p0)
        println(findUsages())
        return object : UsageViewDescriptor {
            override fun getElements() = callsToReplace
            override fun getProcessedElementsHeader() = "EventBus.post() calls to be replaced"
            override fun getCodeReferencesText(usagesCount: Int, filesCount: Int) =
                "Replacing $usagesCount calls in $filesCount files"
            override fun getCommentReferencesText(usagesCount: Int, filesCount: Int) = ""
        }
    }


    override fun performRefactoring(p0: Array<out UsageInfo>) {
        p0.forEach { usage ->
            val call = usage.element as? PsiMethodCallExpression ?: return@forEach
            val argsText = call.argumentList.text
            val file = call.containingFile
            val factory = JavaPsiFacade.getElementFactory(project)
            val newExpr = factory.createExpressionFromText("ModAPI.getAPI().getEventBus().fireEvent$argsText", call)
            call.replace(newExpr)

            addImportIfNeeded(file, "kr.syeyoung.modapi.ModAPI")
        }
    }

    override fun getCommandName(): String = "Replace EventBus.post() Calls with ModAPI EventBus.post Calls"

    override fun isPreviewUsages(): Boolean {
        return true;
    }

    override fun isPreviewUsages(usages: Array<out UsageInfo>): Boolean {
        return true;
    }

    override fun preprocessUsages(refUsages: Ref<Array<UsageInfo>>): Boolean {
        return true;
    }
}
class UseModAPIEventQuickFix : LocalQuickFix {
    override fun getName(): String = "Replace with modapi event"
    override fun getFamilyName(): String = name

    override fun applyFix(p0: Project, p1: ProblemDescriptor) {
        val psiClass = PsiTreeUtil.getParentOfType(p1.psiElement, PsiClass::class.java) ?: return
        val factory = JavaPsiFacade.getElementFactory(p0)

        val file = psiClass.containingFile as? PsiJavaFile ?: return
        val extendsList = psiClass.extendsList ?: return


        WriteCommandAction.runWriteCommandAction(p0) {
            for (ref in extendsList.referenceElements) {
                if (ref.qualifiedName == "net.minecraftforge.fml.common.eventhandler.Event") {
                    val newRef = factory.createReferenceFromText("UEvent", null)
                    ref.replace(newRef)
                }
            }

            // Remove old import if exists
            val oldImport = file.importList?.allImportStatements
                ?.find { it.importReference?.qualifiedName == "net.minecraftforge.fml.common.eventhandler.Event" }
            oldImport?.delete()

            // Add new import if missing
            val hasNewImport = file.importList?.allImportStatements
                ?.any { it.importReference?.qualifiedName == "kr.syeyoung.modapi.event.UEvent" } ?: false

            if (!hasNewImport) {
                val newImport =
                    factory.createImportStatementOnDemand("kr.syeyoung.modapi.event") // or use createImportStatement("com.example.NewBaseClass")
                file.importList?.add(newImport)
            }
        }

        // Find all eventbus posts.

        val scope = GlobalSearchScope.moduleWithLibrariesScope(ModuleUtilCore.findModuleForPsiElement(psiClass)!!)
        val psiFacade = JavaPsiFacade.getInstance(p0)
        val eventBusClass = psiFacade.findClass("net.minecraftforge.fml.common.eventhandler.EventBus", scope) ?: return


        val postMethod = eventBusClass.methods.filter { it.name == "post" }.first() ?: return;
        val ref = MethodReferencesSearch.search(
            postMethod,
            GlobalSearchScope.moduleScope(ModuleUtilCore.findModuleForPsiElement(psiClass)!!),
            false
        ).map { PsiTreeUtil.getParentOfType(it.element, PsiMethodCallExpression::class.java) }
            .filterNotNull()
            .filter {
                val clazz = PsiUtil.resolveClassInType(it.argumentList.expressions[0].type);
                clazz == psiClass || clazz?.isInheritor(psiClass, true) ?: false
            }
        show("Find post: $ref")

        ApplicationManager.getApplication().invokeLater {
            ReplaceEventBusPostProcessor222(p0, ref.toTypedArray()).run()
        }
        show("WTTT")
    }
}

class EventInspection: AbstractBaseJavaLocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object: JavaElementVisitor() {
        override fun visitMethod(method: PsiMethod) {
            super.visitMethod(method)
            if (!method.isMod()) return

            if (method.hasAnnotation("net.minecraftforge.fml.common.eventhandler.SubscribeEvent")) {
                holder.registerProblem(method, "Migrate away from forge events")
            }
        }

        override fun visitClass(aClass: PsiClass) {
            super.visitClass(aClass)
            if (!aClass.isMod()) return

            if (aClass.superClass?.qualifiedName == "net.minecraftforge.fml.common.eventhandler.Event") {
                holder.registerProblem(aClass.extendsList!!, "Migrate away from forge events", UseModAPIEventQuickFix())
            }
        }
    }

    override fun getDisplayName() = "Encapsulate in API"
    override fun getShortName() = "APIEncapsulationInspection"
    override fun getGroupDisplayName() = "Migration Helper"
    override fun isEnabledByDefault() = true

}
