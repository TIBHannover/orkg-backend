package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.community.api.OrganizationUseCases
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.files.domain.model.ImageData
import java.io.File
import javax.activation.MimeType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class MigrateImagesRunner(
    private val organizationService: OrganizationUseCases,
    private val env: Environment
) : ApplicationRunner {

    private val logger: Logger = LogManager.getLogger()

    override fun run(args: ApplicationArguments?) {
        val imagePath = env.getProperty("orkg.storage.images.dir") ?: return

        File(imagePath).listFiles()?.forEach {
            try {
                val (organizationId, mimeType) = it.name.split(".")
                organizationService.updateLogo(
                    id = OrganizationId(organizationId),
                    imageData = ImageData(it.readBytes()),
                    mimeType = MimeType("image/$mimeType"),
                    contributor = ContributorId.createUnknownContributor()
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
