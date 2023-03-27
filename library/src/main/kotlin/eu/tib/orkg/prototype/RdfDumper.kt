package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.export.rdf.api.ExportRDFUseCase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("production")
class RdfDumper(
    private val rdfService: ExportRDFUseCase
) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Value("\${orkg.rdf.dump.file-name:#{null}}")
    private val path: String? = null

    @Scheduled(cron = "\${orkg.rdf.dump.schedule}")
    fun dumpRdf() {
        logger.info("Starting rdf dump...")
        try {
            rdfService.dumpToNTriple(path)
            logger.info("Finished rdf dump")
        } catch (e: Exception) {
            logger.error("Error creating rdf dump", e)
        }
    }
}
