package org.orkg

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.orkg.common.json.CommonJacksonModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ContextConfiguration

@JsonTest // We need application.yaml loaded for @Value to work. This application context will do, as it is small.
@ContextConfiguration(classes = [CommonJacksonModule::class])
class InitialDataJsonTest {
    @Value("\${orkg.init.setup.entities-file:#{null}}")
    private var configuredFilename: String? = null

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun verifyInitialDataCanBeLoadedFromConfigurationAndIsFreeOfErrors() {
        if (configuredFilename == null) throw IllegalStateException("Filename was not provided or could not be read")
        val input: InputStream = this::class.java.classLoader.getResource(configuredFilename)?.openStream()
            ?: throw IllegalStateException("Filename was configured, but file was not found")

        assertDoesNotThrow {
            objectMapper.readValue(input, InitialDataSetup.CreateMainCommand::class.java)
        }
    }
}
