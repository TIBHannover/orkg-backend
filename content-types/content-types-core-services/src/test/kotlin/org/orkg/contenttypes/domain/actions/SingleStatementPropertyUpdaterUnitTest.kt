package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateLiteralUseCase.UpdateCommand
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createStatement
import java.util.UUID

internal class SingleStatementPropertyUpdaterUnitTest : MockkBaseTest {
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator = mockk()

    private val singleStatementPropertyUpdater = SingleStatementPropertyUpdater(
        unsafeLiteralUseCases,
        statementService,
        unsafeStatementUseCases,
        singleStatementPropertyCreator
    )

    @Test
    fun `Given a new literal label and a list of statements, when a statement already exists, it updates the literal`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val description = "some description"
        val literal = createLiteral()
        val updateLiteralCommand = UpdateCommand(
            id = literal.id,
            contributorId = contributorId,
            label = description,
            datatype = Literals.XSD.INT.prefixedUri
        )
        val statements = listOf(
            createStatement(
                predicate = createPredicate(Predicates.description),
                `object` = literal
            )
        )

        every { unsafeLiteralUseCases.update(updateLiteralCommand) } just runs

        singleStatementPropertyUpdater.updateRequiredProperty(
            statements = statements,
            contributorId = contributorId,
            subjectId = subjectId,
            predicateId = Predicates.description,
            label = description,
            datatype = Literals.XSD.INT.prefixedUri
        )

        verify(exactly = 1) { unsafeLiteralUseCases.update(updateLiteralCommand) }
    }

    @Test
    fun `Given a new literal label and a list of statements, when no statements exist, it creates a new statement`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val description = "some description"
        val statements = emptyList<GeneralStatement>()

        every {
            singleStatementPropertyCreator.create(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = Predicates.description,
                label = description,
                datatype = Literals.XSD.INT.prefixedUri
            )
        } just runs

        singleStatementPropertyUpdater.updateRequiredProperty(
            statements = statements,
            contributorId = contributorId,
            subjectId = subjectId,
            predicateId = Predicates.description,
            label = description,
            datatype = Literals.XSD.INT.prefixedUri
        )

        verify(exactly = 1) {
            singleStatementPropertyCreator.create(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = Predicates.description,
                label = description,
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
    }

    @Test
    fun `Given a new literal label and a list of statements, when more than one statement already exist, it deletes all but one and updates the literal`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val description = "some description"
        val literal = createLiteral()
        val updateLiteralCommand = UpdateCommand(
            id = literal.id,
            contributorId = contributorId,
            label = description,
            datatype = Literals.XSD.INT.prefixedUri
        )
        val statements = listOf(
            createStatement(
                predicate = createPredicate(Predicates.description),
                `object` = literal
            ),
            createStatement(
                id = StatementId("S456"),
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(ThingId("L132"), label = "other")
            )
        )

        every { unsafeLiteralUseCases.update(updateLiteralCommand) } just runs
        every { statementService.deleteAllById(setOf(StatementId("S456"))) } just runs

        singleStatementPropertyUpdater.updateRequiredProperty(
            statements = statements,
            contributorId = contributorId,
            subjectId = subjectId,
            predicateId = Predicates.description,
            label = description,
            datatype = Literals.XSD.INT.prefixedUri
        )

        verify(exactly = 1) { unsafeLiteralUseCases.update(updateLiteralCommand) }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S456"))) }
    }

    @Test
    fun `Given a new optional literal label and a list of statements, when a statement already exists, it updates the literal`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val description = "some description"
        val literal = createLiteral()
        val updateLiteralCommand = UpdateCommand(
            id = literal.id,
            contributorId = contributorId,
            label = description,
            datatype = Literals.XSD.STRING.prefixedUri
        )
        val statements = listOf(
            createStatement(
                predicate = createPredicate(Predicates.description),
                `object` = literal
            )
        )

        every { unsafeLiteralUseCases.update(updateLiteralCommand) } just runs

        singleStatementPropertyUpdater.updateOptionalProperty(statements, contributorId, subjectId, Predicates.description, description)

        verify(exactly = 1) { unsafeLiteralUseCases.update(updateLiteralCommand) }
    }

    @Test
    fun `Given a new optional literal label and a list of statements, when no statements exist, it creates a new statement`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val description = "some description"
        val statements = emptyList<GeneralStatement>()

        every {
            singleStatementPropertyCreator.create(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = Predicates.description,
                label = description
            )
        } just runs

        singleStatementPropertyUpdater.updateOptionalProperty(statements, contributorId, subjectId, Predicates.description, description)

        verify(exactly = 1) {
            singleStatementPropertyCreator.create(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = Predicates.description,
                label = description
            )
        }
    }

    @Test
    fun `Given a new optional literal label and a list of statements, when more than one statement already exist, it deletes all but one and updates the literal`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val description = "some description"
        val literal = createLiteral()
        val updateLiteralCommand = UpdateCommand(
            id = literal.id,
            contributorId = contributorId,
            label = description,
            datatype = Literals.XSD.STRING.prefixedUri
        )
        val statements = listOf(
            createStatement(
                predicate = createPredicate(Predicates.description),
                `object` = literal
            ),
            createStatement(
                id = StatementId("S456"),
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(ThingId("L132"), label = "other")
            )
        )

        every { unsafeLiteralUseCases.update(updateLiteralCommand) } just runs
        every { statementService.deleteAllById(setOf(StatementId("S456"))) } just runs

        singleStatementPropertyUpdater.updateOptionalProperty(statements, contributorId, subjectId, Predicates.description, description)

        verify(exactly = 1) { unsafeLiteralUseCases.update(updateLiteralCommand) }
        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S456"))) }
    }

    @Test
    fun `Given a new optional literal label and a list of statements, when label is null, it deletes all matching statements`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val statements = listOf(
            createStatement(StatementId("S159")),
            createStatement(
                id = StatementId("S456"),
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(ThingId("L132"), label = "other")
            )
        )

        every { statementService.deleteAllById(setOf(StatementId("S456"))) } just runs

        singleStatementPropertyUpdater.updateOptionalProperty(statements, contributorId, subjectId, Predicates.description, null as String?)

        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S456"))) }
    }

    @Test
    fun `Given a new optional literal label and a list of statements, when label is null and statements are empty, it does nothing`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())

        singleStatementPropertyUpdater.updateOptionalProperty(emptyList(), contributorId, subjectId, Predicates.description, null as String?)
    }

    @Test
    fun `Given a new required object id and a list of statements, when no previous statements exist, it creates a new statement`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R456")
        val statements = emptyList<GeneralStatement>()

        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasContribution,
                    objectId = contributionId
                )
            )
        } returns StatementId("S1")

        singleStatementPropertyUpdater.updateRequiredProperty(statements, contributorId, subjectId, Predicates.hasContribution, contributionId)

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasContribution,
                    objectId = contributionId
                )
            )
        }
    }

    @Test
    fun `Given a new required object id and a list of statements, when previous statements exist, it deletes all matching statements matching the predicate and creates a new statement`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionId = ThingId("R456")
        val statements = listOf(
            createStatement(StatementId("S159")),
            createStatement(StatementId("S357"), predicate = createPredicate(Predicates.hasContribution))
        )

        every { statementService.deleteAllById(setOf(StatementId("S357"))) } just runs
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasContribution,
                    objectId = contributionId
                )
            )
        } returns StatementId("S1")

        singleStatementPropertyUpdater.updateRequiredProperty(statements, contributorId, subjectId, Predicates.hasContribution, contributionId)

        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S357"))) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasContribution,
                    objectId = contributionId
                )
            )
        }
    }

    @Test
    fun `Given a new optional object id and a list of statements, when no previous statements exist, it creates a new statement`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val objectId = ThingId("R456")
        val statements = emptyList<GeneralStatement>()

        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasContribution,
                    objectId = objectId
                )
            )
        } returns StatementId("S1")

        singleStatementPropertyUpdater.updateOptionalProperty(statements, contributorId, subjectId, Predicates.hasContribution, objectId)

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasContribution,
                    objectId = objectId
                )
            )
        }
    }

    @Test
    fun `Given a new optional object id and a list of statements, when previous statements exist, it deletes all statements matching the predicate and creates a new statement`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val objectId = ThingId("R456")
        val statements = listOf(
            createStatement(StatementId("S159")),
            createStatement(StatementId("S357"), predicate = createPredicate(Predicates.hasContribution))
        )

        every { statementService.deleteAllById(setOf(StatementId("S357"))) } just runs
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasContribution,
                    objectId = objectId
                )
            )
        } returns StatementId("S1")

        singleStatementPropertyUpdater.updateOptionalProperty(statements, contributorId, subjectId, Predicates.hasContribution, objectId)

        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S357"))) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasContribution,
                    objectId = objectId
                )
            )
        }
    }

    @Test
    fun `Given a new optional object id and a list of statements, when object id is null but previous statements exist, it deletes all statements matching the predicate`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val objectId = null
        val statements = listOf(
            createStatement(StatementId("S159")),
            createStatement(StatementId("S357"), predicate = createPredicate(Predicates.hasContribution))
        )

        every { statementService.deleteAllById(setOf(StatementId("S357"))) } just runs

        singleStatementPropertyUpdater.updateOptionalProperty(statements, contributorId, subjectId, Predicates.hasContribution, objectId as ThingId?)

        verify(exactly = 1) { statementService.deleteAllById(setOf(StatementId("S357"))) }
    }
}
