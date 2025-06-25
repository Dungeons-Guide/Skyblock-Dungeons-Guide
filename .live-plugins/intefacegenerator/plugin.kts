
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.util.PackageUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.Ref
import com.intellij.polySymbols.utils.NameCaseUtils
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import liveplugin.PluginUtil.showInConsole
import liveplugin.registerInspection
import liveplugin.show
import kotlin.math.exp

// depends-on-plugin com.intellij.java

// See also com.siyeh.ig.fixes.MakeFieldFinalFix in IntelliJ sources.

registerInspection(MinecraftInspection())
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

var dgHelper = DGHelper();

class DGHelper() {
    fun findModule(name: String): Module {
        return ModuleManager.getInstance(project!!).findModuleByName(name) ?: throw IllegalArgumentException("Invalid module name $name");
    }

    fun tryCreateInterface(module: Module, name: String): PsiClass {
        val packageName = name.substringBeforeLast(".");
        val psiPackDir = PackageUtil.findOrCreateDirectoryForPackage(module, packageName,  null, false) ?: throw IllegalStateException("What")

        val clazz = JavaDirectoryService.getInstance().createInterface(psiPackDir, name.substringAfterLast("."))
        return clazz;
    }
    fun tryCreateClass(module: Module, name: String): PsiClass {
        val packageName = name.substringBeforeLast(".");
        val psiPackDir = PackageUtil.findOrCreateDirectoryForPackage(module, packageName,  null, false) ?: throw IllegalStateException("What")

        val clazz = JavaDirectoryService.getInstance().createClass(psiPackDir, name.substringAfterLast("."))
        clazz.methods.size
        return clazz;
    }
}

fun addImportIfNeeded(file: PsiFile, clazz: PsiClass) {
    if (file !is PsiJavaFile) return

//    val clazz = JavaPsiFacade.getInstance(file.project).findClass(qualifiedName, file.resolveScope)!!;
    JavaCodeStyleManager.getInstance(project).addImport(file, clazz)
}

//class ReplaceEventBusPostProcessor222(
//    val project: Project,
//    val callsToReplace: Array<PsiMethodCallExpression>
//): BaseRefactoringProcessor(project) {
//    override fun findUsages(): Array<UsageInfo> = callsToReplace.map {  UsageInfo(it) }.toTypedArray()
//    override fun createUsageViewDescriptor(p0: Array<out UsageInfo>): UsageViewDescriptor {
//        println(p0)
//        println(findUsages())
//        return object : UsageViewDescriptor {
//            override fun getElements() = callsToReplace
//            override fun getProcessedElementsHeader() = "EventBus.post() calls to be replaced"
//            override fun getCodeReferencesText(usagesCount: Int, filesCount: Int) =
//                "Replacing $usagesCount calls in $filesCount files"
//            override fun getCommentReferencesText(usagesCount: Int, filesCount: Int) = ""
//        }
//    }
//
//
//    override fun performRefactoring(p0: Array<out UsageInfo>) {
//        p0.forEach { usage ->
//            val call = usage.element as? PsiMethodCallExpression ?: return@forEach
//            val argsText = call.argumentList.text
//            val file = call.containingFile
//            val factory = JavaPsiFacade.getElementFactory(project)
//            val newExpr = factory.createExpressionFromText("ModAPI.getAPI().getEventBus().fireEvent$argsText", call)
//            call.replace(newExpr)
//
//            addImportIfNeeded(file, "kr.syeyoung.modapi.ModAPI")
//        }
//    }
//
//    override fun getCommandName(): String = "Replace EventBus.post() Calls with ModAPI EventBus.post Calls"
//
//    override fun isPreviewUsages(): Boolean {
//        return true;
//    }
//
//    override fun isPreviewUsages(usages: Array<out UsageInfo>): Boolean {
//        return true;
//    }
//
//    override fun preprocessUsages(refUsages: Ref<Array<UsageInfo>>): Boolean {
//        return true;
//    }
//}

fun createDelegateAndApi(expr: PsiReferenceExpression, clazz: PsiClass, impl: PsiClass) {
    val resolved = expr.resolve();
    var newName: String;
    var returnType: PsiType;
    var paramList: Array<PsiParameter> = arrayOf();
    var importList: MutableList<PsiClass> = arrayListOf();
    if (resolved is PsiField) {
        if (resolved.type.equalsToText("boolean")) {
            newName = "is"+resolved.name
            returnType = resolved.type
        } else {
            newName = "get"+resolved.name
            returnType = resolved.type
        }

        PsiUtil.resolveClassInType(resolved.type)?.let(importList::add)
    } else if (resolved is PsiMethod){
        newName = resolved.name
        returnType = resolved.returnType ?: throw IllegalArgumentException("What is this : $resolved");
        paramList = resolved.parameterList.parameters;

        PsiUtil.resolveClassInType(returnType)?.let(importList::add)
        for (parameter in paramList) {
            PsiUtil.resolveClassInType(parameter.type)?.let { importList.add(it) }
        }
    } else {
        throw IllegalArgumentException("WAHATT? $expr")
    }

    for (klass in importList) {
        addImportIfNeeded(impl.containingFile, klass)
        addImportIfNeeded(clazz.containingFile, klass)
    }

    newName = NameCaseUtils.toCamelCase(newName)

    val factory = JavaPsiFacade.getElementFactory(expr.project)
    val newMethod = factory.createMethod(newName, returnType)
    for (parameter in paramList) {
        newMethod.parameterList.add(factory.createParameter(parameter.name, parameter.type))
    } // copied method perfectly

    clazz.add(newMethod)


    val delegateMethod = newMethod.copy() as PsiMethod
    delegateMethod.addAfter(factory.createCodeBlock(), newMethod.parameterList)

    if (resolved is PsiMethod) {
        val paramNames = resolved.parameterList.parameters.joinToString(", ") { it.name ?: "arg" }
        val callText = if (resolved.returnType!!.equalsToText("void")) {
            "delegate.${resolved.name}($paramNames);"
        } else {
            "return delegate.${resolved.name}($paramNames);"
        }
        val body = factory.createCodeBlockFromText("{ $callText }", null)
        delegateMethod.body?.replace(body)
    } else if (resolved is PsiField) {
        val callText = "return delegate.${resolved.name};"
        val body = factory.createCodeBlockFromText("{ $callText }", null)
        delegateMethod.body?.replace(body)
    }
    impl.add(delegateMethod)

}

fun PsiClass.maybeSearchForInterfaceAndImpl(): Pair<PsiClass, PsiClass>? {
    val thiz: PsiClass = this
    var result: PsiClass? = null;
    val psiManager = PsiManager.getInstance(project)

    FileTypeIndex.processFiles(JavaFileType.INSTANCE, { file ->
        val psiFile = psiManager.findFile(file) as? PsiJavaFile ?: return@processFiles true

        for (psiClass in psiFile.classes) {
            val hasMatchingField = psiClass.fields.any { field ->
                field.name == "delegate" && PsiUtil.resolveClassInType(field.type) == thiz
            }
            if (hasMatchingField) result = psiClass
        }

        true
    }, GlobalSearchScope.moduleScope(dgHelper.findModule("Dungeons_Guide.1.8.9.main")))

    val resultReal = result;
    if (resultReal == null) return null;
    var interf = resultReal.implementsList?.referenceElements?.get(0)?.resolve() as? PsiClass
    if (interf == null) return null
    return Pair(resultReal, interf);
}

class WrapInInterfaceQuickFix(val expr: PsiReferenceExpression, val source: PsiClass) : LocalQuickFix {
    override fun getName(): String = "Wrap in interface and create delegate"
    override fun getFamilyName(): String = name

    override fun applyFix(p0: Project, p1: ProblemDescriptor) {
        // try searching.

        var res = source.maybeSearchForInterfaceAndImpl()

        if (res == null) {
            val className = Messages.showInputDialog(
                project,
                "Enter interface name:",
                "Interface Name Input",
                Messages.getQuestionIcon(),
                "kr.syeyoung.modapi.${source.qualifiedName?.replace("net.minecraft.", "")?.substringBeforeLast(".")}.U${source.name}",
                object : InputValidator {
                    override fun checkInput(p0: @NlsSafe String?): Boolean {
                        return true
                    }

                    override fun canClose(p0: @NlsSafe String?): Boolean {
                        return checkInput(p0)
                    }
                }
            ) ?: return
            val clazz = dgHelper.tryCreateInterface(dgHelper.findModule("Dungeons_Guide.modapi.main"), className)
            val impl = dgHelper.tryCreateClass(
                dgHelper.findModule("Dungeons_Guide.1.8.9.main"),
                className.replace("kr.syeyoung.modapi", "kr.syeyoung.modapi.v1_8_9") + "Impl"
            );

            val factory = JavaPsiFacade.getElementFactory(p0);
            addImportIfNeeded(impl.containingFile, source)
            addImportIfNeeded(impl.containingFile, clazz)

            impl.implementsList?.add(factory.createClassReferenceElement(clazz))

            impl.add(
                factory
                    .createField("delegate", PsiTypesUtil.getClassType(source))
                    .let {
                        it.modifierList?.setModifierProperty(PsiModifier.PRIVATE, true)
                        return@let it
                    }
            )

            var const = factory.createConstructor(className.substringAfterLast("."));
            const = impl.add(const) as PsiMethod
            const.parameterList.add(factory.createParameter("delegate", PsiTypesUtil.getClassType(source)))
            const.body?.add(factory.createStatementFromText("""this.delegate = delegate;""", const))


            res = Pair(clazz, impl)
        }

        createDelegateAndApi(expr, res.first, res.second)

        val codeStyleManager = CodeStyleManager.getInstance(p0)
        codeStyleManager.reformat(res.first.containingFile)
        codeStyleManager.reformat(res.second.containingFile)
    }
}

class MinecraftInspection: AbstractBaseJavaLocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object: JavaElementVisitor() {
        override fun visitReferenceExpression(expression: PsiReferenceExpression) {
            super.visitReferenceExpression(expression)

            if (!expression.isMod()) return
            val expr = expression.qualifierExpression ?: return
            val type = PsiUtil.resolveClassInType(expr.type) ?: return;

            if (!type.isBad()) return

            holder.registerProblem(expression.referenceNameElement ?: return,
                "Called Minecraft class directly",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                WrapInInterfaceQuickFix(expression, type))
        }
    }

    override fun getDisplayName() = "Encapsulate in API"
    override fun getShortName() = "APIEncapsulationInspection"
    override fun getGroupDisplayName() = "Migration Helper"
    override fun isEnabledByDefault() = true

}
