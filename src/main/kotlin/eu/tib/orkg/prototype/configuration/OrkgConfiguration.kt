package eu.tib.orkg.prototype.configuration

import javax.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "orkg")
class OrkgConfiguration {
    private lateinit var storage: Storage

    inner class Storage {
        private lateinit var images: Images
        inner class Images {
            /**
             * Directory to store images, such as logos for observatories.
             */
            var dir: String? = "#{user.dir}/images"
        }
        inner class InitialImportData {
            /**
             * Path to initial import data for classes and predicates
             */
            var initialSetupFile: String? = "data/required_entities.json"

            /**
             * Path to sub research fields file
             */
            var subResearchFieldsFile: String? = "data/ResearchFields.json"
        }
    }
}

@ConfigurationProperties(prefix = "orkg.init.setup")
@ConstructorBinding
data class InputInjection(
    @get:NotBlank var entitiesFile: String,
    @get:NotBlank var subResearchFieldsFile: String
)
