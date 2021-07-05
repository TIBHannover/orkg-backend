package eu.tib.orkg.prototype.util.upload

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileStorageServiceImpl : FileStorageService {

    @Value("\${file.upload-dir}")
    var imageStoragePath: String? = null

    override fun storeFile(file: MultipartFile): Path {
        val fileExtension = file.originalFilename.split(".")[1]
        val uuidAsFileName = UUID.randomUUID().toString() + "." + fileExtension

        val path = imageStoragePath + "\\" + uuidAsFileName
            .toCharArray()[0].toString() + "\\" + uuidAsFileName
            .toCharArray()[1].toString()
        val dirName = File(path)

        if (!dirName.exists()) {
            dirName.mkdirs()
        }

        val location = Paths.get(path).toAbsolutePath().normalize()

        val uploadLocationPath = location.resolve(uuidAsFileName)

        Files.copy(file.inputStream, uploadLocationPath, StandardCopyOption.REPLACE_EXISTING)

        return uploadLocationPath
    }

    override fun loadFileAsResource(fileName: String): Resource {
        val fileObject = Files.walk(Paths.get(imageStoragePath)).filter(Files::isRegularFile).findFirst()
        val resource = UrlResource(fileObject.get().toRealPath().normalize().toUri())
        if (resource.exists()) {
            return resource
        } else {
            throw Exception("Resource does not exist")
        }
    }
}
