package eu.tib.orkg.prototype.filestorage

import eu.tib.orkg.prototype.util.upload.FileResponse
import eu.tib.orkg.prototype.util.upload.FileStorageService
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/files")
class FileStorageController(
    private val storageService: FileStorageService
) {
    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): FileResponse {
        val fileName = storageService.storeFile(file)

        val downloadURI = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path(fileName.toAbsolutePath().normalize().toUri().toString())
            .toUriString()

        return FileResponse(fileName.toAbsolutePath().fileName.toString(), downloadURI, file.contentType, file.size)
    }

    @PostMapping("/upload-multiple")
    fun uploadMultipleFiles(@RequestParam("files") files: List<MultipartFile>): List<FileResponse> {
        return files.stream().map { file ->
            uploadFile(file)
        }.collect(Collectors.toList())
    }

    @GetMapping("/downloadFile/{filename:.+}")
    fun downloadFile(@PathVariable filename: String, request: HttpServletRequest): ResponseEntity<Resource> {
        val resource = storageService.loadFileAsResource(filename)

        if (resource != null) {
            var contentType: String? = request.servletContext.getMimeType(resource.file.absolutePath)

            if (contentType == null) {
                contentType = "application/octet-stream"
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource?.getFilename() + "\"")
                .body(resource)
        }

        return ResponseEntity<Resource>(null, HttpStatus.NOT_FOUND)
    }

    @GetMapping("/details/{file}")
    fun fileDetails(@PathVariable file: String, request: HttpServletRequest): ResponseEntity<FileResponse> {
        val resource = storageService.loadFileAsResource(file)
        if (resource != null) {
            val downloadURI = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(resource.uri.normalize().toString())
                .toUriString()

            return ResponseEntity<FileResponse>(
                FileResponse(resource.filename,
                    downloadURI,
                    resource.file.extension,
                    resource.contentLength()), HttpStatus.OK)
        }

        return ResponseEntity<FileResponse>(null, HttpStatus.BAD_REQUEST)
    }
}
