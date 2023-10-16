package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.export.comparisons.api.ExportUnpublishedComparisonUseCase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("production")
class UnpublishedComparisonExporter(
    private val service: ExportUnpublishedComparisonUseCase
) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Value("\${orkg.export.unpublished-comparisons.file-name:#{null}}")
    private val path: String? = null

    @Scheduled(cron = "\${orkg.export.unpublished-comparisons.schedule}")
    fun export() {
        logger.info("Starting unpublished comparison export...")
        try {
            service.export(path)
            logger.info("Finished comparison export")
        } catch (e: Exception) {
            logger.error("Error exporting unpublished comparisons", e)
        }
    }
}
