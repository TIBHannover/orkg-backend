package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import java.io.File
import java.util.Base64
import java.util.UUID
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/organizations/")
@CrossOrigin(origins = ["*"])
class OrganizationController(private val service: OrganizationService) {

    @PostMapping("/")
    fun addOrganization(@RequestBody organization: CreateOrganizationRequest): OrganizationEntity {
        var response = (service.create(organization.organizationName, organization.organizationLogo))
        decoder(organization.organizationLogo, response.id)
        return response
    }
    @GetMapping("/")
    fun listOrganizations(): List<OrganizationEntity> {
        return service.listOrganizations()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): OrganizationResponse {
        var response = service.findById(id)
        var pathFile: String = System.getProperty("user.dir")
        var path: String = "$pathFile/images/"
        var logo = encoder(path, response.get().id.toString())

        return (
                OrganizationController.OrganizationResponse(
                organizationId = id,
                organizationName = response.get().name,
                organizationLogo = logo
            )
        )
    }

    data class CreateOrganizationRequest(
        val organizationName: String,
        var organizationLogo: String
    )

    data class OrganizationResponse(
        val organizationId: UUID,
        val organizationName: String?,
        var organizationLogo: String
    )

    fun decoder(base64Str: String, name: UUID?) {
        val strings: List<String> = base64Str.split(",")
        val extension: String
        extension = when (strings[0]) {
            "data:image/jpeg;base64" -> "jpeg"
            "data:image/png;base64" -> "png"
            else -> "jpg"
        }

        var pathFile: String = System.getProperty("user.dir")
        var path: String = "$pathFile/images/$name.$extension"
        var base64Image = strings[1]
        val imageByteArray = Base64.getDecoder().decode(base64Image)
        File(path).writeBytes(imageByteArray)
    }

    fun encoder(filePath: String, id: String): String {
        var file: String = ""
        var ext = ""
        File(filePath).walk().forEach {
            if (it.name.substringBeforeLast(".") == id) {
                ext = it.name.substringAfterLast(".")
                ext = when (ext) {
                    "jpeg" -> "data:image/jpeg;base64"
                    "png" -> "data:image/png;base64"
                    else -> "data:image/png;base64"
                }
                file = it.toString()
            }
        }

        if (file != "") {
            val bytes = File(file).readBytes()
            val base64 = Base64.getEncoder().encodeToString(bytes)
            return "$ext,$base64"
        } else
            return ""
    }
}
