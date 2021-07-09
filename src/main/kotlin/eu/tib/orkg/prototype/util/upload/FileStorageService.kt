package eu.tib.orkg.prototype.util.upload

import java.io.File
import java.nio.file.Path
import org.springframework.web.multipart.MultipartFile

interface FileStorageService {
    fun storeFile(file: MultipartFile): Path
    fun loadFileAsResource(fileName: String): File?
}
