package org.orkg

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.orkg.common.json.CommonJacksonModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ContextConfiguration

@JsonTest // for ObjectMapper injection
@ContextConfiguration(classes = [CommonJacksonModule::class])
class InitialDataJsonTest {
    private val configuredFilename: String = "data/required_entities.json"

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `initial data is free of errors`() {
        val input: InputStream = this::class.java.classLoader.getResource(configuredFilename)?.openStream()
            ?: throw IllegalStateException("Filename was configured, but file was not found")

        assertDoesNotThrow {
            objectMapper.readValue(input, InitialDataSetup.CreateMainCommand::class.java)
        }
    }
}
