package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.FileParser
import eu.tib.orkg.prototype.configuration.OrkgConfiguration
import java.io.FileReader
import org.junit.Test
import org.junit.jupiter.api.Assertions

class ConfigurationFileParserTest {

    @Test
    fun testParseInitialData() {
        val fileParser = FileParser(FileReader(
            this::class.java.classLoader.getResource(OrkgConfiguration().Storage().InitialImportData().initialSetupFile).file))
        Assertions.assertDoesNotThrow(fileParser::parseInitialData)
    }
}
