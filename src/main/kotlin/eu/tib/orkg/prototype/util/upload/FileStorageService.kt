package eu.tib.orkg.prototype.util.upload

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path

interface FileStorageService {
    fun storeFile(file: MultipartFile): Path
    fun loadFileAsResource(fileName: String): Resource
}
