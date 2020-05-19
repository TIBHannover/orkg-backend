package eu.tib.orkg.prototype.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
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
    }
}
