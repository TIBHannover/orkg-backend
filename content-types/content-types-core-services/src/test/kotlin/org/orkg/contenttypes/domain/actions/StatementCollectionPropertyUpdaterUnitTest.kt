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
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.time.OffsetDateTime
import java.util.UUID

internal class StatementCollectionPropertyUpdaterUnitTest : MockkBaseTest {
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()

    private val statementCollectionPropertyUpdater = StatementCollectionPropertyUpdater(
        unsafeLiteralUseCases,
        statementService,
        unsafeStatementUseCases
    )

    @Test
    fun `Given set of objects, it replaces the all object statements`() {
        val subjectId = ThingId("R123")
        val objects = listOf(ThingId("R3"), ThingId("R4"))
        val oldObjectStatements = setOf(ThingId("R1"), ThingId("R2")).toReferenceStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, objects.toSet())

        verify(exactly = 1) { statementService.deleteAllById(oldObjectStatements.map { it.id }.toSet()) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[0]
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[1]
                )
            )
        }
    }

    @Test
    fun `Given set of objects, when some objects are identical to old objects, it only updates changed objects`() {
        val subjectId = ThingId("R123")
        val objects = listOf(ThingId("R2"), ThingId("R3"))
        val oldObjectStatements = setOf(ThingId("R1"), ThingId("R2")).toReferenceStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, objects.toSet())

        verify(exactly = 1) { statementService.deleteAllById(setOf(oldObjectStatements[0].id)) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[1]
                )
            )
        }
    }

    @Test
    fun `Given a set of objects, when new objects are identical to old objects, it does not modify any statements`() {
        val subjectId = ThingId("R123")
        val objects = listOf(ThingId("R1"), ThingId("R2"))
        val oldObjectStatements = objects.toReferenceStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, objects.toSet())
    }

    @Test
    fun `Given a set of objects, when new set of objects is empty, it removes all old object statements`() {
        val subjectId = ThingId("R123")
        val objects = emptySet<ThingId>()
        val oldObjectStatements = listOf(ThingId("R1"), ThingId("R2")).toReferenceStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, objects.toSet())

        verify(exactly = 1) { statementService.deleteAllById(oldObjectStatements.map { it.id }.toSet()) }
    }

    @Test
    fun `Given a set of objects, when old set of objects is empty, it creates new objects statements`() {
        val subjectId = ThingId("R123")
        val objects = listOf(ThingId("R1"), ThingId("R2"))
        val contributorId = ContributorId(UUID.randomUUID())

        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")

        statementCollectionPropertyUpdater.update(emptyList(), contributorId, subjectId, Predicates.reference, objects.toSet())

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[0]
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[1]
                )
            )
        }
    }

    @Test
    fun `Given set of literals, it replaces the all literal statements`() {
        val subjectId = ThingId("R123")
        val literals = listOf("R3", "R4")
        val oldLiteralStatements = setOf("R1", "R2").toLiteralStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")
        every { unsafeLiteralUseCases.create(any()) } returns ThingId("L1") andThen ThingId("L2")

        statementCollectionPropertyUpdater.update(oldLiteralStatements, contributorId, subjectId, Predicates.reference, literals.toSet())

        verify(exactly = 1) { statementService.deleteAllById(oldLiteralStatements.map { it.id }.toSet()) }
        literals.forEach { literal ->
            verify(exactly = 1) {
                unsafeLiteralUseCases.create(
                    CreateLiteralUseCase.CreateCommand(label = literal, contributorId = contributorId)
                )
            }
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = ThingId("L1")
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = ThingId("L2")
                )
            )
        }
    }

    @Test
    fun `Given set of literals, when some literals are identical to old literals, it only updates changed literals`() {
        val subjectId = ThingId("R123")
        val literals = listOf("R2", "R3")
        val oldLiteralStatements = setOf("R1", "R2").toLiteralStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")
        every { unsafeLiteralUseCases.create(any()) } returns ThingId("L1")

        statementCollectionPropertyUpdater.update(oldLiteralStatements, contributorId, subjectId, Predicates.reference, literals.toSet())

        verify(exactly = 1) { statementService.deleteAllById(setOf(oldLiteralStatements[0].id)) }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(label = literals[1], contributorId = contributorId)
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = ThingId("L1")
                )
            )
        }
    }

    @Test
    fun `Given a set of literals, when new literals are identical to old literals, it does not modify any statements`() {
        val subjectId = ThingId("R123")
        val literals = listOf("R1", "R2")
        val oldLiteralStatements = literals.toLiteralStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        statementCollectionPropertyUpdater.update(oldLiteralStatements, contributorId, subjectId, Predicates.reference, literals.toSet())
    }

    @Test
    fun `Given a set of literals, when new set of literals is empty, it removes all old literal statements`() {
        val subjectId = ThingId("R123")
        val literals = emptySet<String>()
        val oldLiteralStatements = listOf("R1", "R2").toLiteralStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs

        statementCollectionPropertyUpdater.update(oldLiteralStatements, contributorId, subjectId, Predicates.reference, literals.toSet())

        verify(exactly = 1) { statementService.deleteAllById(oldLiteralStatements.map { it.id }.toSet()) }
    }

    @Test
    fun `Given a set of literals, when old set of literals is empty, it creates new literals statements`() {
        val subjectId = ThingId("R123")
        val literals = listOf("R1", "R2")
        val contributorId = ContributorId(UUID.randomUUID())

        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")
        every { unsafeLiteralUseCases.create(any()) } returns ThingId("L1") andThen ThingId("L2")

        statementCollectionPropertyUpdater.update(emptyList(), contributorId, subjectId, Predicates.reference, literals.toSet())

        literals.forEach { literal ->
            verify(exactly = 1) {
                unsafeLiteralUseCases.create(
                    CreateLiteralUseCase.CreateCommand(label = literal, contributorId = contributorId)
                )
            }
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = ThingId("L1")
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = ThingId("L2")
                )
            )
        }
    }

    @Test
    fun `Given list of objects, it replaces the all object statements`() {
        val subjectId = ThingId("R123")
        val objects = listOf(ThingId("R3"), ThingId("R4"))
        val oldObjectStatements = setOf(ThingId("R1"), ThingId("R2")).toReferenceStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, objects)

        verify(exactly = 1) { statementService.deleteAllById(oldObjectStatements.map { it.id }.toSet()) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[0]
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[1]
                )
            )
        }
    }

    @Test
    fun `Given list of objects, when some objects are identical to old objects, it reuses existing statements`() {
        val subjectId = ThingId("R123")
        val objects = listOf(ThingId("R2"), ThingId("R3"))
        val oldObjectStatements = setOf(ThingId("R1"), ThingId("R2")).toReferenceStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, objects)

        verify(exactly = 1) { statementService.deleteAllById(setOf(oldObjectStatements[0].id)) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[1]
                )
            )
        }
    }

    @Test
    fun `Given a list of objects, when new objects are identical to old objects, it does not modify any statements`() {
        val subjectId = ThingId("R123")
        val objects = listOf(ThingId("R1"), ThingId("R2"))
        val oldObjectStatements = objects.toReferenceStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, objects)
    }

    @Test
    fun `Given a list of objects, when new list of objects is empty, it removes all old object statements`() {
        val subjectId = ThingId("R123")
        val objects = emptySet<ThingId>()
        val oldObjectStatements = listOf(ThingId("R1"), ThingId("R2")).toReferenceStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, objects)

        verify(exactly = 1) { statementService.deleteAllById(oldObjectStatements.map { it.id }.toSet()) }
    }

    @Test
    fun `Given a list of objects, when old list of objects is empty, it creates new objects statements`() {
        val subjectId = ThingId("R123")
        val objects = listOf(ThingId("R1"), ThingId("R2"))
        val contributorId = ContributorId(UUID.randomUUID())

        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")

        statementCollectionPropertyUpdater.update(emptyList(), contributorId, subjectId, Predicates.reference, objects)

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[0]
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[1]
                )
            )
        }
    }

    @Test
    fun `Given a list of objects, when new objects are identical to old objects but list is shorter, it removes excessive statements`() {
        val subjectId = ThingId("R123")
        val objects = listOf(ThingId("R1"), ThingId("R2"))
        val excessiveObjectStatements = listOf(ThingId("R3")).toReferenceStatements(subjectId)
        val oldObjectStatements = objects.toReferenceStatements(subjectId) + excessiveObjectStatements
        val contributorId = ContributorId(UUID.randomUUID())

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, objects)

        verify(exactly = 1) { statementService.deleteAllById(excessiveObjectStatements.map { it.id }.toSet()) }
    }

    @Test
    fun `Given list of literals, it replaces the all literal statements`() {
        val subjectId = ThingId("R123")
        val literals = listOf("R3", "R4")
        val oldObjectStatements = setOf("R1", "R2").toLiteralStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())
        val literalId1 = ThingId("L1")
        val literalId2 = ThingId("L2")
        val literalCreateCommand1 = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = literals[0],
            datatype = Literals.XSD.STRING.prefixedUri
        )
        val literalCreateCommand2 = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = literals[1],
            datatype = Literals.XSD.STRING.prefixedUri
        )

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")
        every { unsafeLiteralUseCases.create(literalCreateCommand1) } returns literalId1
        every { unsafeLiteralUseCases.create(literalCreateCommand2) } returns literalId2

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, literals)

        verify(exactly = 1) { statementService.deleteAllById(oldObjectStatements.map { it.id }.toSet()) }
        verify(exactly = 1) { unsafeLiteralUseCases.create(literalCreateCommand1) }
        verify(exactly = 1) { unsafeLiteralUseCases.create(literalCreateCommand2) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = literalId1
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = literalId2
                )
            )
        }
    }

    @Test
    fun `Given list of literals, when some literals are identical to old literals, it reuses existing statements`() {
        val subjectId = ThingId("R123")
        val literals = listOf("R2", "R3")
        val oldObjectStatements = setOf("R1", "R2").toLiteralStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())
        val literalId2 = ThingId("L2")
        val literalCreateCommand2 = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = literals[1],
            datatype = Literals.XSD.STRING.prefixedUri
        )

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")
        every { unsafeLiteralUseCases.create(literalCreateCommand2) } returns literalId2

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, literals)

        verify(exactly = 1) { statementService.deleteAllById(setOf(oldObjectStatements[0].id)) }
        verify(exactly = 1) { unsafeLiteralUseCases.create(literalCreateCommand2) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = literalId2
                )
            )
        }
    }

    @Test
    fun `Given a list of literals, when new literals are identical to old literals, it does not modify any statements`() {
        val subjectId = ThingId("R123")
        val literals = listOf("R1", "R2")
        val oldObjectStatements = literals.toLiteralStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, literals)
    }

    @Test
    fun `Given a list of literals, when new list of literals is empty, it removes all old literal statements`() {
        val subjectId = ThingId("R123")
        val literals = emptySet<String>()
        val oldObjectStatements = listOf("R1", "R2").toLiteralStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, literals)

        verify(exactly = 1) { statementService.deleteAllById(oldObjectStatements.map { it.id }.toSet()) }
    }

    @Test
    fun `Given a list of literals, when old list of literals is empty, it creates new objects statements`() {
        val subjectId = ThingId("R123")
        val literals = listOf("R1", "R2")
        val contributorId = ContributorId(UUID.randomUUID())
        val literalId1 = ThingId("L1")
        val literalId2 = ThingId("L2")
        val literalCreateCommand1 = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = literals[0],
            datatype = Literals.XSD.STRING.prefixedUri
        )
        val literalCreateCommand2 = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = literals[1],
            datatype = Literals.XSD.STRING.prefixedUri
        )

        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")
        every { unsafeLiteralUseCases.create(literalCreateCommand1) } returns literalId1
        every { unsafeLiteralUseCases.create(literalCreateCommand2) } returns literalId2

        statementCollectionPropertyUpdater.update(emptyList(), contributorId, subjectId, Predicates.reference, literals)

        verify(exactly = 1) { unsafeLiteralUseCases.create(literalCreateCommand1) }
        verify(exactly = 1) { unsafeLiteralUseCases.create(literalCreateCommand2) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = literalId1
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = literalId2
                )
            )
        }
    }

    @Test
    fun `Given a list of literals, when new literals are identical to old literals but list is shorter, it removes excessive statements`() {
        val subjectId = ThingId("R123")
        val literals = listOf("R1", "R2")
        val excessiveObjectStatements = listOf("R3").toLiteralStatements(subjectId)
        val oldObjectStatements = literals.toLiteralStatements(subjectId) + excessiveObjectStatements
        val contributorId = ContributorId(UUID.randomUUID())

        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, literals)

        verify(exactly = 1) { statementService.deleteAllById(excessiveObjectStatements.map { it.id }.toSet()) }
    }

    private fun Collection<ThingId>.toReferenceStatements(subjectId: ThingId): List<GeneralStatement> = mapIndexed { index, id ->
        createStatement(
            id = StatementId("S$id"),
            subject = createResource(subjectId, createdAt = OffsetDateTime.now(fixedClock).plusHours(index.toLong())),
            predicate = createPredicate(Predicates.reference),
            `object` = createResource(id, classes = setOf(Classes.paper))
        )
    }

    private fun Collection<String>.toLiteralStatements(subjectId: ThingId): List<GeneralStatement> = mapIndexed { index, string ->
        createStatement(
            id = StatementId("S$index"),
            subject = createResource(subjectId),
            predicate = createPredicate(Predicates.reference),
            `object` = createLiteral(ThingId("L$index"), string)
        )
    }
}
