package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.EntityConfigurationParser
import java.io.FileReader
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Value

class ConfigurationFileParserTest {

    @Value("orkg.init.setup.entitiesFile")
    private var filename: String? = null

    @Test
    fun testParseInitialData() {
        val fileParser = EntityConfigurationParser(
            FileReader(
                this::class.java.classLoader.getResource(filename).file
            )
        )
        Assertions.assertDoesNotThrow(fileParser::parseInitialData)
    }
}
