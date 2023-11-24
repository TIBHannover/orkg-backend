package org.orkg.licenses.domain

import java.net.URI

typealias LicenseInformationProviderId = String

interface LicenseInformationProvider {
    val id: LicenseInformationProviderId
    val description: String
    fun canProcess(uri: URI): Boolean
    fun determineLicense(from: URI): LicenseInformation?
}
