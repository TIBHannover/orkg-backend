import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils

plugins {
    id("org.orkg.gradle.base")
}

@CacheableTask
abstract class GeneratePatchesTask : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    abstract val originalDirectory: DirectoryProperty

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    abstract val patchedDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun action() {
        val originalDirectory = originalDirectory.get().asFile
        val patchedDirectory = patchedDirectory.get().asFile
        val outputDirectory = outputDirectory.get().asFile
        originalDirectory.walk().forEach { file ->
            if (file.isDirectory) {
                return@forEach
            }
            val path = file.relativeTo(originalDirectory)
            val original = file.readLines()
            val patched = patchedDirectory.resolve(path).readLines()
            val diff = DiffUtils.diff(original, patched)
            val unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                path.invariantSeparatorsPath,
                path.invariantSeparatorsPath,
                original,
                diff,
                3,
            )
            if (unifiedDiff.isNotEmpty()) {
                val output = File(outputDirectory, path.toString() + ".patch")
                output.parentFile.mkdirs()
                output.writeText(unifiedDiff.joinToString("\n"))
            }
        }
    }
}

object PatchHelper {
    fun applyPatches(patchesDir: File, targetDir: File) {
        patchesDir.walk().forEach { file ->
            if (file.isDirectory) {
                return@forEach
            }
            val path = file.relativeTo(patchesDir)
            println("Patching $path")
            val patch = UnifiedDiffUtils.parseUnifiedDiff(file.readLines())
            val original = targetDir.resolve(path.toString().removeSuffix(".patch"))
            val patched = patch.applyFuzzy(original.readLines(), 5)
            original.writeText(patched.joinToString("\n"))
        }
    }
}
