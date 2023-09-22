package eu.tib.orkg.prototype.licenses.application

import eu.tib.orkg.prototype.licenses.api.RetrieveLicenseInformationUseCase
import eu.tib.orkg.prototype.licenses.domain.LicenseInformation
import eu.tib.orkg.prototype.licenses.domain.LicenseInformationProvider
import java.net.URI
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LicenseInformationService(
    private val providers: List<LicenseInformationProvider>
): RetrieveLicenseInformationUseCase {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    init {
        logger.info("Loaded license providers: {}", providers.joinToString { it.id })
    }

    override fun determineLicense(uri: URI): LicenseInformation =
        Optional.ofNullable(providers.firstOrNull { it.canProcess(uri) })
            .map { it.determineLicense(uri) ?: throw LicenseNotFound(uri) }
            .orElseThrow { throw UnsupportedURI(uri) }
}
