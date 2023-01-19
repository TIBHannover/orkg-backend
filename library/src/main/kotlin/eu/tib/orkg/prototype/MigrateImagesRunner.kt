package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.community.api.OrganizationUseCases
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.files.domain.model.ImageData
import java.io.File
import javax.activation.MimeType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class MigrateImagesRunner(
    private val organizationService: OrganizationUseCases,
) : ApplicationRunner {

    private val logger: Logger = LogManager.getLogger()

    @Value("\${orkg.storage.images.dir}")
    private var imagePath: String? = null

    override fun run(args: ApplicationArguments?) {
        if (imagePath == null || !File(imagePath!!).exists()) return

        File(imagePath!!).listFiles()?.forEach {
            try {
                val (organizationId, mimeType) = it.name.split(".")
                organizationService.updateLogo(
                    id = OrganizationId(organizationId),
                    imageData = ImageData(it.readBytes()),
                    mimeType = MimeType("image/$mimeType"),
                    contributor = null
                )
                it.delete()
                logger.info("Successfully migrated organization image: ${it.name}")
            } catch (e: Exception) {
                logger.error("Failed to migrate organization image: ${it.name}")
                logger.error(e)
            }
        }
    }
}
