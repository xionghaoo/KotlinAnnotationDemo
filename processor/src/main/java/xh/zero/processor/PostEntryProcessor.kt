package xh.zero.processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic
import kotlin.reflect.KClass

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class PostEntryProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        note("PostEntryProcessor getSupportedAnnotationTypes")
        return mutableSetOf(PostEntry::class.java.name)
    }

    override fun process(set: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        note("PostEntryProcessor process")
        roundEnv?.getElementsAnnotatedWith(PostEntry::class.java)?.forEach { element ->
            val methodName = element.simpleName.toString()
            note("PostEntryProcessor methodName: $methodName")
            val packageName = processingEnv.elementUtils.getPackageOf(element).toString()
            note("PostEntryProcessor package: $packageName")
            val value = element.getAnnotation(PostEntry::class.java).value
            note("annotation value: $value")

            val method = element as ExecutableElement
//            val parameterNames = ArrayList<String>()
//            method.parameters?.forEach { parameter ->
//                parameterNames.add(parameter.simpleName.toString())
//                note("parameter.simpleName: ${parameter.asType().asTypeName()}")
//                note("parameter.constantValue: ${parameter.constantValue}")
//            }

            generateClass(methodName, packageName, value, method.parameters)
        }
        return true
    }

    private fun generateClass(methodName: String, packageName: String, _className: String, parameters: List<VariableElement>) {
        val className = "${_className}RequestEntry"
        val file = FileSpec.builder(packageName, className)
            .addType(createType(className, parameters))
            .build()

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir, "$className.kt"))
    }

    private fun createType(className: String, parameterNames: List<VariableElement>) : TypeSpec {
        val type = TypeSpec.classBuilder(className)

        val func = FunSpec.constructorBuilder()
        parameterNames.forEach {
            if (it.asType().toString() == "java.lang.String") {
                func.addParameter(it.simpleName.toString(), String::class)
            } else {
                func.addParameter(it.simpleName.toString(), it.asType().asTypeName())
            }
        }
        type.primaryConstructor(func.build())

        parameterNames.forEach {
            if (it.asType().toString() == "java.lang.String") {
                type.addProperty(
                    PropertySpec.builder(it.simpleName.toString(), String::class)
                        .initializer(it.simpleName.toString())
                        .build()
                )
            } else {
                type.addProperty(
                    PropertySpec.builder(it.simpleName.toString(), it.asType().asTypeName())
                        .initializer(it.simpleName.toString())
                        .build()
                )
            }
        }

        return type.build()
    }

    private fun note(msg: String) {
        // NOTE级别的日志打印不出
        processingEnv.messager?.printMessage(Diagnostic.Kind.WARNING, msg)
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}