package org.orkg.licenses.input

import org.orkg.licenses.domain.LicenseInformation
import java.net.URI

interface RetrieveLicenseInformationUseCase {
    fun determineLicense(uri: URI): LicenseInformation
}
