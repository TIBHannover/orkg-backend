package org.orkg.contenttypes.domain.actions

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.EmptyContribution
import org.orkg.contenttypes.domain.InvalidStatementSubject
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.ThingIsNotAPredicate
import org.orkg.contenttypes.input.CreateContributionCommandPart
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.CreatePredicateCommandPart
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class ContributionValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()

    private val contributionValidator = ContributionValidator(thingRepository)

    @Test
    fun `Given a paper create command, when validating its contributions, it returns success`() {
        val command = createPaperCommand()
        val resource = createResource(id = ThingId("R3003"))
        val tempIds = setOf("#temp1", "#temp2", "#temp3", "#temp4")
        val validatedIds = mapOf<String, Either<String, Thing>>(
            "R3003" to Either.right(resource),
            "#temp1" to Either.left("#temp1"),
            "#temp2" to Either.left("#temp2"),
            "#temp3" to Either.left("#temp3"),
            "#temp4" to Either.left("#temp4")
        )

        val template = createClass(ThingId("C123"))
        val `class` = createClass(ThingId("R3004"))
        val hasResearchProblem = createPredicate(Predicates.hasResearchProblem)
        val hasEvaluation = createPredicate(Predicates.hasEvaluation)

        every { thingRepository.findById(`class`.id) } returns Optional.of(`class`)
        every { thingRepository.findById(template.id) } returns Optional.of(template)
        every { thingRepository.findById(Predicates.hasResearchProblem) } returns Optional.of(hasResearchProblem)
        every { thingRepository.findById(Predicates.hasEvaluation) } returns Optional.of(hasEvaluation)

        val result = contributionValidator.validate(
            tempIds = tempIds,
            validatedIdsIn = validatedIds,
            thingsCommand = command.contents!!,
            contributionCommands = command.contents!!.contributions,
        )

        result.asClue {
            it.validatedIds shouldBe validatedIds + mapOf(
                "C123" to Either.right(template),
                "R3003" to Either.right(resource),
                "R3004" to Either.right(`class`),
                Predicates.hasEvaluation.value to Either.right(hasEvaluation),
                Predicates.hasResearchProblem.value to Either.right(hasResearchProblem)
            )
            it.bakedStatements shouldBe setOf(
                BakedStatement("^0", Predicates.hasResearchProblem.value, "R3003"),
                BakedStatement("^0", Predicates.hasEvaluation.value, "#temp1"),
                BakedStatement("^1", Predicates.hasResearchProblem.value, "R3003"),
                BakedStatement("^1", Predicates.hasEvaluation.value, "#temp1"),
                BakedStatement("^1", Predicates.hasEvaluation.value, "R3004"),
                BakedStatement("R3004", "#temp3", "R3003"),
                BakedStatement("R3004", "#temp3", "#temp2"),
                BakedStatement("R3004", "#temp4", "#temp1")
            )
        }

        verify(exactly = 1) { thingRepository.findById(`class`.id) }
        verify(exactly = 1) { thingRepository.findById(template.id) }
        verify(exactly = 1) { thingRepository.findById(Predicates.hasResearchProblem) }
        verify(exactly = 1) { thingRepository.findById(Predicates.hasEvaluation) }
    }

    @Test
    fun `Given a paper create command, when class of contribution does not exist, it throws an exception`() {
        val command = createPaperCommand()
        val resource = createResource(id = ThingId("R3003"))
        val tempIds = setOf("#temp1", "#temp2", "#temp3", "#temp4")
        val validatedIds = mapOf<String, Either<String, Thing>>(
            "R3003" to Either.right(resource),
            "#temp1" to Either.left("#temp1"),
            "#temp2" to Either.left("#temp2"),
            "#temp3" to Either.left("#temp3"),
            "#temp4" to Either.left("#temp4")
        )
        val template = createClass(ThingId("C123"))

        every { thingRepository.findById(template.id) } returns Optional.empty()

        assertThrows<ThingNotFound> {
            contributionValidator.validate(
                tempIds = tempIds,
                validatedIdsIn = validatedIds,
                thingsCommand = command.contents!!,
                contributionCommands = command.contents!!.contributions,
            )
        }

        verify(exactly = 1) { thingRepository.findById(template.id) }
    }

    @Test
    fun `Given a paper create command, when class of contribution is not a class, it throws an exception`() {
        val command = createPaperCommand()
        val resource = createResource(id = ThingId("R3003"))
        val tempIds = setOf("#temp1", "#temp2", "#temp3", "#temp4")
        val validatedIds = mapOf<String, Either<String, Thing>>(
            "R3003" to Either.right(resource),
            "#temp1" to Either.left("#temp1"),
            "#temp2" to Either.left("#temp2"),
            "#temp3" to Either.left("#temp3"),
            "#temp4" to Either.left("#temp4")
        )
        val template = createResource(id = ThingId("C123"))

        every { thingRepository.findById(template.id) } returns Optional.of(template)

        assertThrows<ThingIsNotAClass> {
            contributionValidator.validate(
                tempIds = tempIds,
                validatedIdsIn = validatedIds,
                thingsCommand = command.contents!!,
                contributionCommands = command.contents!!.contributions,
            )
        }

        verify(exactly = 1) { thingRepository.findById(template.id) }
    }

    @Test
    fun `Given a paper create command, when contribution does not contain any statements, it throws an exception`() {
        val command = createPaperCommand().copy(
            contents = CreatePaperUseCase.CreateCommand.PaperContents(
                contributions = listOf(
                    CreateContributionCommandPart(
                        label = "Contribution",
                        statements = emptyMap()
                    )
                )
            )
        )
        val exception = EmptyContribution()

        assertThrows<EmptyContribution> {
            contributionValidator.validate(
                tempIds = emptySet(),
                validatedIdsIn = emptyMap(),
                thingsCommand = command.contents!!,
                contributionCommands = command.contents!!.contributions,
            )
        }.asClue {
            it.message shouldBe exception.message
        }
    }

    @Test
    fun `Given paper contents, when predicate could not be found, it throws an exception`() {
        val statements = mapOf(
            "P32" to listOf(CreateContributionCommandPart.StatementObject("R3003"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(
                CreateContributionCommandPart(
                    label = "Contribution 1",
                    statements = statements
                )
            )
        )

        every { thingRepository.findById(Predicates.hasResearchProblem) } returns Optional.empty()

        assertThrows<ThingNotFound> {
            contributionValidator.bakeStatements(
                subject = "#temp1",
                statementCommands = statements,
                tempIds = setOf("#temp1", "#temp2", "#temp3", "#temp4"),
                thingsCommand = contents,
                contributionCommands = contents.contributions,
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
            id.value to listOf(CreateContributionCommandPart.StatementObject("R3003"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(
                CreateContributionCommandPart(
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
                statementCommands = statements,
                tempIds = emptySet(),
                thingsCommand = contents,
                contributionCommands = contents.contributions,
                validatedIds = mutableMapOf(),
                destination = mutableSetOf()
            )
        }

        verify(exactly = 1) { thingRepository.findById(id) }
    }

    @Test
    fun `Given paper contents, when specified temp id does not resolve to a predicate, it throws an exception`() {
        val statements = mapOf(
            "#temp1" to listOf(CreateContributionCommandPart.StatementObject("R3003"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(
                CreateContributionCommandPart(
                    label = "Contribution 1",
                    statements = statements
                )
            )
        )

        assertThrows<ThingIsNotAPredicate> {
            contributionValidator.bakeStatements(
                subject = "#temp1",
                statementCommands = statements,
                tempIds = emptySet(),
                thingsCommand = contents,
                contributionCommands = contents.contributions,
                validatedIds = mutableMapOf("#temp1" to Either.left("#temp1")),
                destination = mutableSetOf()
            )
        }
    }

    @Test
    fun `Given paper contents, when object could not be found, it throws an exception`() {
        val statements = mapOf(
            "#temp1" to listOf(CreateContributionCommandPart.StatementObject("R3003"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            predicates = mapOf(
                "#temp1" to CreatePredicateCommandPart(
                    label = "predicate"
                )
            ),
            contributions = listOf(
                CreateContributionCommandPart(
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
                statementCommands = statements,
                tempIds = emptySet(),
                thingsCommand = contents,
                contributionCommands = contents.contributions,
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
                CreateContributionCommandPart.StatementObject(
                    id = literalId,
                    statements = mapOf(
                        Predicates.hasEvaluation.value to listOf(
                            CreateContributionCommandPart.StatementObject("R3003")
                        )
                    )
                )
            )
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(
                CreateContributionCommandPart(
                    label = "Contribution 1",
                    statements = statements
                )
            )
        )

        assertThrows<InvalidStatementSubject> {
            contributionValidator.bakeStatements(
                subject = "#temp1",
                statementCommands = statements,
                tempIds = setOf("#temp1"),
                thingsCommand = contents,
                contributionCommands = contents.contributions,
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
                CreateContributionCommandPart.StatementObject(
                    id = "#temp1",
                    statements = mapOf(
                        Predicates.hasEvaluation.value to listOf(
                            CreateContributionCommandPart.StatementObject("R3003")
                        )
                    )
                )
            )
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            literals = mapOf(
                "#temp1" to CreateLiteralCommandPart("label")
            ),
            contributions = listOf(
                CreateContributionCommandPart(
                    label = "Contribution 1",
                    statements = statements
                )
            )
        )

        assertThrows<InvalidStatementSubject> {
            contributionValidator.bakeStatements(
                subject = "#temp1",
                statementCommands = statements,
                tempIds = setOf("#temp1"),
                thingsCommand = contents,
                contributionCommands = contents.contributions,
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
                CreateContributionCommandPart(
                    label = "\n",
                    statements = mapOf("P32" to listOf(CreateContributionCommandPart.StatementObject("R3003")))
                )
            )
        )

        assertThrows<InvalidLabel> {
            contributionValidator.validate(
                validatedIdsIn = emptyMap(),
                tempIds = emptySet(),
                thingsCommand = contents,
                contributionCommands = contents.contributions
            )
        }
    }
}
