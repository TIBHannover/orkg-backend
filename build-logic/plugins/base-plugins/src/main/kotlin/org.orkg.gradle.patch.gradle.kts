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
        // The following line might need to be moved into a doFirst block in future Gradle versions.
        // See https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1535#note_3686326991
        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()
        originalDirectory.walk().forEach { file ->
            if (file.isDirectory) {
                return@forEach
            }
            val path = file.relativeTo(originalDirectory)
            val originalLines = file.readLines()
            val patched = patchedDirectory.resolve(path)
            if (!patched.exists()) {
                println("Skipping file: $path")
                return@forEach
            }
            val patchedLines = patched.readLines()
            val diff = DiffUtils.diff(originalLines, patchedLines)
            val unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                path.invariantSeparatorsPath,
                path.invariantSeparatorsPath,
                originalLines,
                diff,
                3,
            )
            if (unifiedDiff.isNotEmpty()) {
                val output = File(outputDirectory, "$path.patch")
                output.parentFile.mkdirs()
                output.writeText(unifiedDiff.joinToString("\n"))
            }
        }
    }
}

@CacheableTask
abstract class ApplyPatchesTask : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    abstract val originalDirectory: DirectoryProperty

    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    abstract val patchesDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun action() {
        val originalDirectory = originalDirectory.get().asFile
        val patchesDirectory = patchesDirectory.get().asFile
        val outputDirectory = outputDirectory.get().asFile
        // The following two lines could be converted into a SyncSpec.
        // See https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1535#note_3686327058
        outputDirectory.deleteRecursively()
        originalDirectory.copyRecursively(outputDirectory)
        if (!patchesDirectory.exists() || !patchesDirectory.isDirectory) {
            return
        }
        patchesDirectory.walk().forEach { file ->
            if (file.isDirectory) {
                return@forEach
            }
            val path = file.relativeTo(patchesDirectory)
            println("Applying patch $path")
            val patch = UnifiedDiffUtils.parseUnifiedDiff(file.readLines())
            val original = outputDirectory.resolve(path.toString().removeSuffix(".patch"))
            val patched = patch.applyFuzzy(original.readLines(), 5)
            original.writeText(patched.joinToString("\n"))
        }
    }
}
