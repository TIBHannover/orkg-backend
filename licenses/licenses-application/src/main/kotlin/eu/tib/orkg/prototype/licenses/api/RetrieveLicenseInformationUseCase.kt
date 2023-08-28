package eu.tib.orkg.prototype.licenses.api

import eu.tib.orkg.prototype.licenses.domain.LicenseInformation
import java.net.URI

interface RetrieveLicenseInformationUseCase {
    fun determineLicense(uri: URI): LicenseInformation
}
