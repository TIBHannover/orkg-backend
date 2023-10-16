package eu.tib.orkg.prototype.export.shared.domain

import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import org.springframework.stereotype.Component

@Component
class FileExportService {
    fun writeToFile(path: String?, defaultPath: String, block: (Writer) -> Unit) {
        val filePath = resolveFilePath(path, defaultPath)
        if (filePath.absolute().parent.exists().not()) throw NoSuchFileException(
            file = filePath.toFile(),
            reason = "The directory ${filePath.parent} does not exist! Make sure it was created, and that permissions are correct.",
        )
        val temp = Files.createTempFile("", "", *fileAttributes())
        OutputStreamWriter(FileOutputStream(temp.toFile()), Charsets.UTF_8).use { block(it) }
        if (temp.exists()) {
            Files.move(temp, filePath, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    internal fun fileAttributes(): Array<FileAttribute<out Any>> {
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            return arrayOf(PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--")))
        }
        return emptyArray()
    }

    internal fun resolveFilePath(path: String?, defaultPath: String): Path {
        if (path == null) {
            return Paths.get(defaultPath)
        }
        val file = Paths.get(path)
        if (file.isDirectory()) {
            return file.resolve(defaultPath)
        }
        return file
    }
}
