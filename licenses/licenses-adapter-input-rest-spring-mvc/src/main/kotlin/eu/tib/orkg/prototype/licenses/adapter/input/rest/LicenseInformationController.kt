package eu.tib.orkg.prototype.licenses.adapter.input.rest

import eu.tib.orkg.prototype.licenses.api.RetrieveLicenseInformationUseCase
import eu.tib.orkg.prototype.licenses.domain.License
import java.net.URI
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

const val LICENSE_JSON_V1 = "application/vnd.orkg.license.v1+json"

@RestController
@RequestMapping(produces = [LICENSE_JSON_V1])
class LicenseInformationController(
    private val licenseInformationService: RetrieveLicenseInformationUseCase
) {
    @GetMapping("/api/licenses", consumes = [LICENSE_JSON_V1])
    fun fetchLicenseInformation(
        @RequestParam("uri") uri: URI
    ): LicenseInformationResponse =
        LicenseInformationResponse(licenseInformationService.determineLicense(uri).license)

    data class LicenseInformationResponse(
        val license: License
    )
}
