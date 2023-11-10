package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.application.ThingIsNotAClass
import eu.tib.orkg.prototype.statements.application.ThingNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
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
                "#temp1" to CreatePaperUseCase.CreateCommand.ResourceDefinition(
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
                contents = contents,
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
                "#temp1" to CreatePaperUseCase.CreateCommand.ResourceDefinition(
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
                contents = contents,
                tempIds = emptySet(),
                validatedIds = mutableMapOf()
            )
        }

        verify(exactly = 1) { thingRepository.findByThingId(any()) }
    }
}
