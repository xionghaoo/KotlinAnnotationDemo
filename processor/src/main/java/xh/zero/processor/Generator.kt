package xh.zero.processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class Generator : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        note("getSupportedAnnotationTypes")
        return mutableSetOf(GenName::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(p0: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        note("process")
        roundEnv?.getElementsAnnotatedWith(GenName::class.java)
            ?.forEach {
                val className = it.simpleName.toString()
                note("Processing: $className")
                val pack = processingEnv.elementUtils.getPackageOf(it).toString()
                generateClass(className, pack)
            }
        return true
    }

    private fun generateClass(className: String, pack: String) {
        val fileName = "Generated_$className"
        val file = FileSpec.builder(pack, fileName)
            .addType(
                TypeSpec.classBuilder(fileName)
                .addFunction(
                    FunSpec.builder("getName")
                    .addStatement("return \"World\"")
                    .build())
                .build())
            .build()

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
    }

    private fun note(msg: String) {
//        processingEnv.messager?.printMessage(Diagnostic.Kind.NOTE, msg)
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}