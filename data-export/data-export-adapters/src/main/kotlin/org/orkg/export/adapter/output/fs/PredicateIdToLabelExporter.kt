package org.orkg.export.adapter.output.fs

import org.orkg.export.input.ExportPredicateIdToLabelUseCase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("production")
class PredicateIdToLabelExporter(
    private val service: ExportPredicateIdToLabelUseCase
) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Value("\${orkg.export.predicate-id-to-label.file-name:#{null}}")
    private val path: String? = null

    @Scheduled(cron = "\${orkg.export.predicate-id-to-label.schedule}")
    fun export() {
        logger.info("Starting predicate id to label export...")
        try {
            service.export(path)
            logger.info("Finished predicate id to label export")
        } catch (e: Exception) {
            logger.error("Error exporting predicate ids to label", e)
        }
    }
}
