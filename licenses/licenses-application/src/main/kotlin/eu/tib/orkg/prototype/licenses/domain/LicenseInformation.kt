package eu.tib.orkg.prototype.licenses.domain

typealias License = String

data class LicenseInformation(
    /** The ID of the provider that determined the license. */
    val providerId: LicenseInformationProviderId,
    /** The license identifier. Should be an SPDX identifier. */
    val license: License
)
