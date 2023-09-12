package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.application.ThingIsNotAClass
import eu.tib.orkg.prototype.dummyCreateContributionCommand
import eu.tib.orkg.prototype.dummyCreatePaperCommand
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.shared.Either
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.application.ThingNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
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

    private val thingDefinitionValidator = ThingDefinitionValidator(thingRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(thingRepository)
    }

    @Test
    fun `Given a paper create command, when validating its thing definitions, it returns success`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()

        val `class` = createClass(id = "R2000")

        every { thingRepository.findByThingId(`class`.id) } returns Optional.of(`class`)

        val result = thingDefinitionValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds shouldBe mapOf(`class`.id.value to Either.right(`class`))
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }

        verify(exactly = 1) { thingRepository.findByThingId(`class`.id) }
    }

    @Test
    fun `Given a paper create command, when no things are defined, it returns success`() {
        val command = dummyCreatePaperCommand().let {
            it.copy(
                contents = CreatePaperUseCase.CreateCommand.PaperContents(
                    resources = emptyMap(),
                    literals = emptyMap(),
                    predicates = emptyMap(),
                    lists = emptyMap(),
                    contributions = it.contents!!.contributions
                )
            )
        }
        val state = PaperState()

        val result = thingDefinitionValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }
    }

    @Test
    fun `Given a contribution create command, when validating its thing definitions, it returns success`() {
        val command = dummyCreateContributionCommand()
        val state = ContributionState()

        val `class` = createClass(id = "R2000")

        every { thingRepository.findByThingId(`class`.id) } returns Optional.of(`class`)

        val result = thingDefinitionValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds shouldBe mapOf(`class`.id.value to Either.right(`class`))
            it.bakedStatements.size shouldBe 0
            it.contributionId shouldBe null
        }

        verify(exactly = 1) { thingRepository.findByThingId(`class`.id) }
    }

    @Test
    fun `Given a contribution create command, when no things are defined, it returns success`() {
        val command = CreateContributionCommand(
            contributorId = ContributorId(UUID.randomUUID()),
            paperId = ThingId("R123"),
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            contribution = CreatePaperUseCase.CreateCommand.Contribution(
                label = "Contribution 1",
                statements = mapOf(
                    Predicates.hasResearchProblem.value to listOf(
                        CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003")
                    )
                )
            )
        )
        val state = ContributionState()

        val result = thingDefinitionValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.contributionId shouldBe null
        }
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
