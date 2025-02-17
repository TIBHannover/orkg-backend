package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.InvalidStatementSubject
import org.orkg.contenttypes.domain.ThingIsNotAPredicate
import org.orkg.contenttypes.input.ContributionDefinition
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.PredicateDefinition
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource

internal class ContributionValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()

    private val contributionValidator = object : ContributionValidator(thingRepository) {}

    @Test
    fun `Given paper contents, when predicate could not be found, it throws an exception`() {
        val statements = mapOf(
            "P32" to listOf(ContributionDefinition.StatementObjectDefinition("R3003"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(
                ContributionDefinition(
                    label = "Contribution 1",
                    statements = statements
                )
            )
        )

        every { thingRepository.findById(Predicates.hasResearchProblem) } returns Optional.empty()

        assertThrows<ThingNotFound> {
            contributionValidator.bakeStatements(
                subject = "#temp1",
                definitions = statements,
                tempIds = setOf("#temp1", "#temp2", "#temp3", "#temp4"),
                thingDefinitions = contents,
                contributionDefinitions = contents.contributions,
                validatedIds = mutableMapOf(),
                destination = mutableSetOf()
            )
        }

        verify(exactly = 1) { thingRepository.findById(Predicates.hasResearchProblem) }
    }

    @Test
    fun `Given paper contents, when specified predicate id does not resolve to a predicate, it throws an exception`() {
        val id = ThingId("R123")
        val statements = mapOf(
            id.value to listOf(ContributionDefinition.StatementObjectDefinition("R3003"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(
                ContributionDefinition(
                    label = "Contribution 1",
                    statements = statements
                )
            )
        )
        val resource = createResource(id = id)

        every { thingRepository.findById(id) } returns Optional.of(resource)

        assertThrows<ThingIsNotAPredicate> {
            contributionValidator.bakeStatements(
                subject = "#temp1",
                definitions = statements,
                tempIds = emptySet(),
                thingDefinitions = contents,
                contributionDefinitions = contents.contributions,
                validatedIds = mutableMapOf(),
                destination = mutableSetOf()
            )
        }

        verify(exactly = 1) { thingRepository.findById(id) }
    }

    @Test
    fun `Given paper contents, when specified temp id does not resolve to a predicate, it throws an exception`() {
        val statements = mapOf(
            "#temp1" to listOf(ContributionDefinition.StatementObjectDefinition("R3003"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(
                ContributionDefinition(
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
                thingDefinitions = contents,
                contributionDefinitions = contents.contributions,
                validatedIds = mutableMapOf("#temp1" to Either.left("#temp1")),
                destination = mutableSetOf()
            )
        }
    }

    @Test
    fun `Given paper contents, when object could not be found, it throws an exception`() {
        val statements = mapOf(
            "#temp1" to listOf(ContributionDefinition.StatementObjectDefinition("R3003"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            predicates = mapOf(
                "#temp1" to PredicateDefinition(
                    label = "predicate"
                )
            ),
            contributions = listOf(
                ContributionDefinition(
                    label = "Contribution 1",
                    statements = statements
                )
            )
        )
        val id = ThingId("R3003")

        every { thingRepository.findById(id) } returns Optional.empty()

        assertThrows<ThingNotFound> {
            contributionValidator.bakeStatements(
                subject = "#temp1",
                definitions = statements,
                tempIds = emptySet(),
                thingDefinitions = contents,
                contributionDefinitions = contents.contributions,
                validatedIds = mutableMapOf("#temp1" to Either.left("#temp1")),
                destination = mutableSetOf()
            )
        }

        verify(exactly = 1) { thingRepository.findById(id) }
    }

    @Test
    fun `Given paper contents, when object is a literal but has further statements, it throws an exception`() {
        val literalId = "L8664"
        val statements = mapOf(
            Predicates.hasEvaluation.value to listOf(
                ContributionDefinition.StatementObjectDefinition(
                    id = literalId,
                    statements = mapOf(
                        Predicates.hasEvaluation.value to listOf(
                            ContributionDefinition.StatementObjectDefinition("R3003")
                        )
                    )
                )
            )
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(
                ContributionDefinition(
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
                thingDefinitions = contents,
                contributionDefinitions = contents.contributions,
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
                ContributionDefinition.StatementObjectDefinition(
                    id = "#temp1",
                    statements = mapOf(
                        Predicates.hasEvaluation.value to listOf(
                            ContributionDefinition.StatementObjectDefinition("R3003")
                        )
                    )
                )
            )
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            literals = mapOf(
                "#temp1" to LiteralDefinition("label")
            ),
            contributions = listOf(
                ContributionDefinition(
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
                thingDefinitions = contents,
                contributionDefinitions = contents.contributions,
                validatedIds = mutableMapOf(
                    "#temp1" to Either.left("#temp1"),
                    Predicates.hasEvaluation.value to Either.right(createPredicate(Predicates.hasEvaluation))
                ),
                destination = mutableSetOf()
            )
        }
    }

    @Test
    fun `Given paper contents, when contribution has an invalid label, it throws an exception`() {
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(
                ContributionDefinition(
                    label = "\n",
                    statements = mapOf("P32" to listOf(ContributionDefinition.StatementObjectDefinition("R3003")))
                )
            )
        )

        assertThrows<InvalidLabel> {
            contributionValidator.validate(
                bakedStatements = mutableSetOf(),
                validatedIds = mutableMapOf(),
                tempIds = emptySet(),
                thingDefinitions = contents,
                contributionDefinitions = contents.contributions
            )
        }
    }
}
