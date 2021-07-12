package eu.tib.orkg.prototype.util.upload

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.Date
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileStorageServiceImpl : FileStorageService {

    @Value("\${orkg.storage.images.file.dir}")
    var imageStoragePath: String? = null

    override fun storeFile(file: MultipartFile): Path {
        val uuidAsFileName = generateRandomFilename(file)

        val path = imageStoragePath + "\\" + uuidAsFileName
            .toCharArray()[0].toString() + "\\" + uuidAsFileName
            .toCharArray()[1].toString()

        File(path).mkdirs()

        val location = Paths.get(path).toAbsolutePath().normalize()
        val uploadLocationPath = location.resolve(uuidAsFileName)

        Files.copy(file.inputStream, uploadLocationPath, StandardCopyOption.REPLACE_EXISTING)

        return uploadLocationPath
    }

    override fun loadFileAsResource(fileName: String): File? =
        File(imageStoragePath).walk().filter { it.isFile }.filter { it.nameWithoutExtension == fileName }.firstOrNull()

    private fun generateRandomFilename(file: MultipartFile): String {
        val fileExtension = file.originalFilename.split(".")[1]
        // Variant-1 of filename creation
        val uuidAsString = UUID.randomUUID().toString()

        // Variant-2
        val partFilename = Date().time
        val fileName = uuidAsString + partFilename

        // Variant - 1
        // return "$uuidAsString.$fileExtension"

        // Variant - 2
        return "$fileName.$fileExtension"
    }
}
