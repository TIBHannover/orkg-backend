package org.orkg

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.orkg.InitialDataSetup.RequiredClassDefinition
import org.orkg.InitialDataSetup.RequiredPredicateDefinition
import org.orkg.InitialDataSetup.RequiredResearchFieldDefinition
import org.orkg.InitialDataSetup.RequiredResourceDefinition
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resources
import org.orkg.graph.domain.reservedClassIds
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ContextConfiguration
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@JsonTest // We need application.yaml loaded for @Value to work. This application context will do, as it is small.
@ContextConfiguration(classes = [CommonJacksonModule::class])
internal class InitialDataJsonTest {
    @Value("\${orkg.init.setup.directory:#{null}}")
    private var directory: String? = null

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun verifyInitialDataCanBeLoadedFromConfigurationAndIsFreeOfErrors() {
        assertDoesNotThrow {
            readDataFile<List<RequiredPredicateDefinition>>("predicates")
            readDataFile<List<RequiredClassDefinition>>("classes")
            readDataFile<List<RequiredResourceDefinition>>("resources")
            readDataFile<RequiredResearchFieldDefinition>("research_fields")
        }
    }

    @Test
    fun `verify all declared predicate id constants have a corresponding required predicate declaration`() {
        assertDoesNotThrow { verifyEntityDeclarations(Predicates::class, "predicates") }
    }

    @Test
    fun `verify all declared class id constants have a corresponding required class declaration`() {
        assertDoesNotThrow { verifyEntityDeclarations(Classes::class, "classes", reservedClassIds) }
    }

    @Test
    fun `verify all declared resource id constants have a corresponding required resource declaration`() {
        assertDoesNotThrow { verifyEntityDeclarations(Resources::class, "resources") }
    }

    private fun <T : Any> verifyEntityDeclarations(
        sourceClass: KClass<T>,
        entitiesFileName: String,
        whiteList: Set<ThingId> = emptySet(),
    ) {
        val declaredEntities = readDataFile<List<JsonNode>>(entitiesFileName).map { ThingId(it.path("id").textValue()) }
        val sourceInstance = sourceClass.objectInstance!!
        val declaredIds = sourceClass.memberProperties.flatMapTo(mutableSetOf()) {
            when (val value = it.get(sourceInstance)) {
                is ThingId -> listOf(value)
                is Iterable<*> -> value.filterIsInstance<ThingId>()
                else -> emptyList()
            }
        } - whiteList
        val missingDeclarations = declaredIds.filter { it !in declaredEntities }
        if (missingDeclarations.isNotEmpty()) {
            throw IllegalStateException("""There are missing entity declarations in "$entitiesFileName": [${missingDeclarations.joinToString { "\"$it\"" }}]""")
        }
    }

    private inline fun <reified T> readDataFile(entitiesFileName: String): T =
        objectMapper.readValue<T>(
            this::class.java.classLoader.getResource("$directory/$entitiesFileName.json")
                ?: throw IllegalStateException("Directory was configured, but file was not found")
        )
}
