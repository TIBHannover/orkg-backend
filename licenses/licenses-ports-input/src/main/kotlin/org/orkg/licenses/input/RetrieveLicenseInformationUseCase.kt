package org.orkg.licenses.input

import java.net.URI
import org.orkg.licenses.domain.LicenseInformation

interface RetrieveLicenseInformationUseCase {
    fun determineLicense(uri: URI): LicenseInformation
}
