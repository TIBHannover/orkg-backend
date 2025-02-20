package org.orkg.licenses.domain

import org.orkg.licenses.input.RetrieveLicenseInformationUseCase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI
import java.util.Optional

@Service
class LicenseInformationService(
    private val providers: List<LicenseInformationProvider>,
) : RetrieveLicenseInformationUseCase {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    init {
        logger.info("Loaded license providers: {}", providers.joinToString { it.id })
    }

    override fun determineLicense(uri: URI): LicenseInformation =
        Optional.ofNullable(providers.firstOrNull { it.canProcess(uri) })
            .map { it.determineLicense(uri) ?: throw LicenseNotFound(uri) }
            .orElseThrow { throw UnsupportedURI(uri) }
}
