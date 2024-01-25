package org.orkg.contenttypes.domain.actions

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.ThingDefinitions
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.domain.reservedClassIds
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createResource

@Nested
class ThingDefinitionValidatorUnitTest {
    private val thingRepository: ThingRepository = mockk()

    private val thingDefinitionValidator = object : ThingDefinitionValidator(thingRepository) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(thingRepository)
    }

    @Test
    fun `Given paper contents, when specified class id for resource is not resolvable, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to ThingDefinitions.ResourceDefinition(
                    label = "MOTO",
                    classes = setOf(ThingId("R2000"))
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        every { thingRepository.findByThingId(any()) } returns Optional.empty()

        assertThrows<ThingNotFound> {
            thingDefinitionValidator.validateIdsInDefinitions(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }

        verify(exactly = 1) { thingRepository.findByThingId(any()) }
    }

    @Test
    fun `Given paper contents, when specified class id for resource does not resolve to a class, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to ThingDefinitions.ResourceDefinition(
                    label = "MOTO",
                    classes = setOf(ThingId("R2000"))
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            contributions = emptyList()
        )
        val resource = createResource()

        every { thingRepository.findByThingId(any()) } returns Optional.of(resource)

        assertThrows<ThingIsNotAClass> {
            thingDefinitionValidator.validateIdsInDefinitions(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }

        verify(exactly = 1) { thingRepository.findByThingId(any()) }
    }

    @Test
    fun `Given paper contents, when specified class id for a resource is reserved, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to ThingDefinitions.ResourceDefinition(
                    label = "MOTO",
                    classes = setOf(reservedClassIds.first())
                )
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        assertThrows<ReservedClass> {
            thingDefinitionValidator.validateIdsInDefinitions(
                thingDefinitions = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }
    }
}
