package eu.tib.orkg.prototype.util.upload

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID
import org.apache.commons.io.FileUtils
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
        //java lib - randomly gen filename -  mktemp
        //try if you can gen UUID from byte stream
        val path = imageStoragePath + "\\" + uuidAsFileName
            .toCharArray()[0].toString() + "\\" + uuidAsFileName
            .toCharArray()[1].toString()

        File(path).mkdirs()

        val location = Paths.get(path).toAbsolutePath().normalize()
        val uploadLocationPath = location.resolve(uuidAsFileName)

        Files.copy(file.inputStream, uploadLocationPath, StandardCopyOption.REPLACE_EXISTING)

        return uploadLocationPath
    }

    override fun loadFileAsResource(fileName: String): Resource? {
        val files = FileUtils.listFiles(File(imageStoragePath), null, true)

        val iterator = files.iterator()
        //closure; for loop
        var uri = files.first {
            it.isFile && it.name.equals(fileName)
        }.toURI()

        //Collections
        //enforce a contract -> .single() -> exception -> throws an exception count > 1

        /*while (iterator.hasNext()) {
            val file = iterator.next()
            if (file.isFile && file.name.equals(fileName)) {
                uri = file.toURI().toString()
                break
            }
        }*/
            return UrlResource(uri)
        }

        return null
    }
}
