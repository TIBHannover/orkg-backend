package org.orkg

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.orkg.InitialDataSetup.RequiredClassDefinition
import org.orkg.InitialDataSetup.RequiredPredicateDefinition
import org.orkg.InitialDataSetup.RequiredResearchFieldDefinition
import org.orkg.InitialDataSetup.RequiredResourceDefinition
import org.orkg.common.json.CommonJacksonModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ContextConfiguration

@JsonTest // We need application.yaml loaded for @Value to work. This application context will do, as it is small.
@ContextConfiguration(classes = [CommonJacksonModule::class])
class InitialDataJsonTest {
    @Value("\${orkg.init.setup.directory:#{null}}")
    private var directory: String? = null

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun verifyInitialDataCanBeLoadedFromConfigurationAndIsFreeOfErrors() {
        assertDoesNotThrow {
            readFile<List<RequiredPredicateDefinition>>("$directory/predicates.json")
            readFile<List<RequiredClassDefinition>>("$directory/classes.json")
            readFile<List<RequiredResourceDefinition>>("$directory/resources.json")
            readFile<RequiredResearchFieldDefinition>("$directory/research_fields.json")
        }
    }

    private inline fun <reified T> readFile(file: String): T =
        objectMapper.readValue<T>(this::class.java.classLoader.getResource(file)
            ?: throw IllegalStateException("Directory was configured, but file was not found"))
}
