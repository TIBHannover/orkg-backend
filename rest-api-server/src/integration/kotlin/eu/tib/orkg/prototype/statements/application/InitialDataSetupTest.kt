package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.EntityConfigurationParser
import java.io.StringReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@DisplayName("Initial Data Setup")
class InitialDataSetupTest {

    @Test
    fun testInitialData() {
        val strInput = """
            {
            	"classes": [
            		{
            			"id": "Paper",
            			"label": "Paper"
            		}
            	],
            	"predicates": [
            		{
            			"label": "Has evaluation",
            			"id": "HAS_EVALUATION"
            		}
            	]
            }
        """.trimIndent()
        val fileParser = EntityConfigurationParser(StringReader(strInput))

        assertDoesNotThrow(fileParser::parseInitialData)
    }

    @Test
    fun testInitialDataWithInvalidInput() {
        val strInput = """{
            "classes": [
                {
                    "id": "Paper"
                }
            ],
            "predicates": [
                {
                    "label": "Has evaluation",
                    "id": "HAS_EVALUATION"
                }
            ]
        }""".trimIndent()
        val fileParser = EntityConfigurationParser(StringReader(strInput))

        val exception = assertThrows<Exception>("A null value was found for label while importing classes") {
            fileParser.parseInitialData()
        }
        assertEquals("A null value was found for label while importing classes", exception.message)
    }
}
