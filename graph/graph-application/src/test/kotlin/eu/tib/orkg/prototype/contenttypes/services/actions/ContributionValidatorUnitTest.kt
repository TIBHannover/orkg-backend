package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.application.EmptyContribution
import eu.tib.orkg.prototype.contenttypes.application.InvalidStatementSubject
import eu.tib.orkg.prototype.contenttypes.application.ThingIsNotAClass
import eu.tib.orkg.prototype.contenttypes.application.ThingIsNotAPredicate
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreatePaperCommand
import eu.tib.orkg.prototype.shared.Either
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.application.ThingNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import eu.tib.orkg.prototype.statements.testing.fixtures.createClass
import eu.tib.orkg.prototype.statements.testing.fixtures.createLiteral
import eu.tib.orkg.prototype.statements.testing.fixtures.createPredicate
import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ContributionValidatorUnitTest {
    private val thingRepository: ThingRepository = mockk()

    private val contributionValidator = object : ContributionValidator(thingRepository) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(thingRepository)
    }

    @Test
    fun `Given paper contents, when predicate could not be found, it throws an exception`() {
        val statements = mapOf(
            "P32" to listOf(CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(
                CreatePaperUseCase.CreateCommand.Contribution(
                    label = "Contribution 1",
                    statements = statements
                )
            )
        )

        every { thingRepository.findByThingId(Predicates.hasResearchProblem) } returns Optional.empty()

        assertThrows<ThingNotFound> {
            contributionValidator.bakeStatements(
                subject = "#temp1",
                definitions = statements,
                tempIds = setOf("#temp1", "#temp2", "#temp3", "#temp4"),
                contents = contents,
                validatedIds = mutableMapOf(),
                destination = mutableSetOf()
            )
        }

        verify(exactly = 1) { thingRepository.findByThingId(Predicates.hasResearchProblem) }
    }

    @Test
    fun `Given paper contents, when specified predicate id does not resolve to a predicate, it throws an exception`() {
        val id = ThingId("R123")
        val statements = mapOf(
            id.value to listOf(CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(
                CreatePaperUseCase.CreateCommand.Contribution(
                    label = "Contribution 1",
                    statements = statements
                )
            )
        )
        val resource = createResource(id = id)

        every { thingRepository.findByThingId(id) } returns Optional.of(resource)

        assertThrows<ThingIsNotAPredicate> {
            contributionValidator.bakeStatements(
                subject = "#temp1",
                definitions = statements,
                tempIds = emptySet(),
                contents = contents,
                validatedIds = mutableMapOf(),
                destination = mutableSetOf()
            )
        }

        verify(exactly = 1) { thingRepository.findByThingId(id) }
    }

    @Test
    fun `Given paper contents, when specified temp id does not resolve to a predicate, it throws an exception`() {
        val statements = mapOf(
            "#temp1" to listOf(CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(
                CreatePaperUseCase.CreateCommand.Contribution(
                    label = "Contribution 1",
                    statements = statements
                )
            )
        )

        assertThrows<ThingIsNotAPredicate> {
            contributionValidator.bakeStatements(
                subject = "#temp1",
                definitions = statements,
                tempIds = emptySet(),
                contents = contents,
                validatedIds = mutableMapOf("#temp1" to Either.left("#temp1")),
                destination = mutableSetOf()
            )
        }
    }

    @Test
    fun `Given paper contents, when object could not be found, it throws an exception`() {
        val statements = mapOf(
            "#temp1" to listOf(CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            predicates = mapOf(
                "#temp1" to CreatePaperUseCase.CreateCommand.PredicateDefinition(
                    label = "predicate"
                )
            ),
            contributions = listOf(
                CreatePaperUseCase.CreateCommand.Contribution(
                    label = "Contribution 1",
                    statements = statements
                )
            )
        )
        val id = ThingId("R3003")

        every { thingRepository.findByThingId(id) } returns Optional.empty()

        assertThrows<ThingNotFound> {
            contributionValidator.bakeStatements(
                subject = "#temp1",
                definitions = statements,
                tempIds = emptySet(),
                contents = contents,
                validatedIds = mutableMapOf("#temp1" to Either.left("#temp1")),
                destination = mutableSetOf()
            )
        }

        verify(exactly = 1) { thingRepository.findByThingId(id) }
    }

    @Test
    fun `Given paper contents, when object is a literal but has further statements, it throws an exception`() {
        val literalId = "L8664"
        val statements = mapOf(
            Predicates.hasEvaluation.value to listOf(
                CreatePaperUseCase.CreateCommand.StatementObjectDefinition(
                    id = literalId,
                    statements = mapOf(
                        Predicates.hasEvaluation.value to listOf(
                            CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003")
                        )
                    )
                )
            )
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(
                CreatePaperUseCase.CreateCommand.Contribution(
                    label = "Contribution 1",
                    statements = statements
                )
            )
        )

        assertThrows<InvalidStatementSubject> {
            contributionValidator.bakeStatements(
                subject = "#temp1",
                definitions = statements,
                tempIds = setOf("#temp1"),
                contents = contents,
                validatedIds = mutableMapOf(
                    literalId to Either.right(createLiteral(ThingId(literalId))),
                    Predicates.hasEvaluation.value to Either.right(createPredicate(Predicates.hasEvaluation))
                ),
                destination = mutableSetOf()
            )
        }
    }

    @Test
    fun `Given paper contents, when temp id is a literal but has further statements, it throws an exception`() {
        val statements = mapOf(
            Predicates.hasEvaluation.value to listOf(
                CreatePaperUseCase.CreateCommand.StatementObjectDefinition(
                    id = "#temp1",
                    statements = mapOf(
                        Predicates.hasEvaluation.value to listOf(
                            CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003")
                        )
                    )
                )
            )
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            literals = mapOf(
                "#temp1" to CreatePaperUseCase.CreateCommand.LiteralDefinition("label")
            ),
            contributions = listOf(
                CreatePaperUseCase.CreateCommand.Contribution(
                    label = "Contribution 1",
                    statements = statements
                )
            )
        )

        assertThrows<InvalidStatementSubject> {
            contributionValidator.bakeStatements(
                subject = "#temp1",
                definitions = statements,
                tempIds = setOf("#temp1"),
                contents = contents,
                validatedIds = mutableMapOf(
                    "#temp1" to Either.left("#temp1"),
                    Predicates.hasEvaluation.value to Either.right(createPredicate(Predicates.hasEvaluation))
                ),
                destination = mutableSetOf()
            )
        }
    }
}
