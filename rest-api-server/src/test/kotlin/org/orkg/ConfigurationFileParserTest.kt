package org.orkg

import java.io.FileReader
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.json.JsonTest

@JsonTest // We need application.yaml loaded for @Value to work. This application context will do, as it is small.
class ConfigurationFileParserTest {

    @Value("\${orkg.init.setup.entities-file:#{null}}")
    private var configuredFilename: String? = null

    @Test
    fun verifyInitialDataCanBeLoadedFromConfigurationAndIsFreeOfErrors() {
        if (configuredFilename == null) throw IllegalStateException("Filename was not provided or could not be read")
        val filename: String = this::class.java.classLoader.getResource(configuredFilename)?.file
            ?: throw IllegalStateException("Filename was configured, but file was not found")

        val fileParser = EntityConfigurationParser(FileReader(filename))

        assertDoesNotThrow(fileParser::parseInitialData)
    }
}
