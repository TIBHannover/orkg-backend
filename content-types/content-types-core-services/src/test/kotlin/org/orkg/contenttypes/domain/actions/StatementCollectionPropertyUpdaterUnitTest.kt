package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
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
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

internal class StatementCollectionPropertyUpdaterUnitTest : MockkBaseTest {
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val statementCollectionPropertyUpdater = StatementCollectionPropertyUpdater(literalService, statementService)

    @Test
    fun `Given set of objects, it replaces the all object statements`() {
        val subjectId = ThingId("R123")
        val objects = listOf(ThingId("R3"), ThingId("R4"))
        val oldObjectStatements = setOf(ThingId("R1"), ThingId("R2")).toReferenceStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldObjectStatements)
        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { statementService.add(any()) } returns StatementId("S1")

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, objects.toSet())

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(oldObjectStatements.map { it.id }.toSet()) }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[0]
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
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

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldObjectStatements)
        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { statementService.add(any()) } returns StatementId("S1")

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, objects.toSet())

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(setOf(oldObjectStatements[0].id)) }
        verify(exactly = 1) {
            statementService.add(
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

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldObjectStatements)

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, objects.toSet())

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        }
    }

    @Test
    fun `Given a set of objects, when new set of objects is empty, it removes all old object statements`() {
        val subjectId = ThingId("R123")
        val objects = emptySet<ThingId>()
        val oldObjectStatements = listOf(ThingId("R1"), ThingId("R2")).toReferenceStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldObjectStatements)
        every { statementService.delete(any<Set<StatementId>>()) } just runs

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, objects.toSet())

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(oldObjectStatements.map { it.id }.toSet()) }
    }

    @Test
    fun `Given a set of objects, when old set of objects is empty, it creates new objects statements`() {
        val subjectId = ThingId("R123")
        val objects = listOf(ThingId("R1"), ThingId("R2"))
        val contributorId = ContributorId(UUID.randomUUID())

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        } returns pageOf()
        every { statementService.add(any()) } returns StatementId("S1")

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, objects.toSet())

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[0]
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
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

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                objectClasses = setOf(Classes.literal),
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldLiteralStatements)
        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { statementService.add(any()) } returns StatementId("S1")
        every { literalService.create(any()) } returns ThingId("L1") andThen ThingId("L2")

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, literals.toSet())

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                objectClasses = setOf(Classes.literal),
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(oldLiteralStatements.map { it.id }.toSet()) }
        literals.forEach { literal ->
            verify(exactly = 1) {
                literalService.create(
                    CreateLiteralUseCase.CreateCommand(label = literal, contributorId = contributorId)
                )
            }
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = ThingId("L1")
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
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

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                objectClasses = setOf(Classes.literal),
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldLiteralStatements)
        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { statementService.add(any()) } returns StatementId("S1")
        every { literalService.create(any()) } returns ThingId("L1")

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, literals.toSet())

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                objectClasses = setOf(Classes.literal),
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(setOf(oldLiteralStatements[0].id)) }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(label = literals[1], contributorId = contributorId)
            )
        }
        verify(exactly = 1) {
            statementService.add(
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

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                objectClasses = setOf(Classes.literal),
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldLiteralStatements)

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, literals.toSet())

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                objectClasses = setOf(Classes.literal),
                pageable = PageRequests.ALL
            )
        }
    }

    @Test
    fun `Given a set of literals, when new set of literals is empty, it removes all old literal statements`() {
        val subjectId = ThingId("R123")
        val literals = emptySet<String>()
        val oldLiteralStatements = listOf("R1", "R2").toLiteralStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                objectClasses = setOf(Classes.literal),
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldLiteralStatements)
        every { statementService.delete(any<Set<StatementId>>()) } just runs

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, literals.toSet())

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                objectClasses = setOf(Classes.literal),
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(oldLiteralStatements.map { it.id }.toSet()) }
    }

    @Test
    fun `Given a set of literals, when old set of literals is empty, it creates new literals statements`() {
        val subjectId = ThingId("R123")
        val literals = listOf("R1", "R2")
        val contributorId = ContributorId(UUID.randomUUID())

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                objectClasses = setOf(Classes.literal),
                pageable = PageRequests.ALL
            )
        } returns pageOf()
        every { statementService.add(any()) } returns StatementId("S1")
        every { literalService.create(any()) } returns ThingId("L1") andThen ThingId("L2")

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, literals.toSet())

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                objectClasses = setOf(Classes.literal),
                pageable = PageRequests.ALL
            )
        }
        literals.forEach { literal ->
            verify(exactly = 1) {
                literalService.create(
                    CreateLiteralUseCase.CreateCommand(label = literal, contributorId = contributorId)
                )
            }
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = ThingId("L1")
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
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

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldObjectStatements)
        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { statementService.add(any()) } returns StatementId("S1")

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, objects)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(oldObjectStatements.map { it.id }.toSet()) }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[0]
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
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

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldObjectStatements)
        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { statementService.add(any()) } returns StatementId("S1")

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, objects)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(setOf(oldObjectStatements[0].id)) }
        verify(exactly = 1) {
            statementService.add(
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

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldObjectStatements)

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, objects)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        }
    }

    @Test
    fun `Given a list of objects, when new list of objects is empty, it removes all old object statements`() {
        val subjectId = ThingId("R123")
        val objects = emptySet<ThingId>()
        val oldObjectStatements = listOf(ThingId("R1"), ThingId("R2")).toReferenceStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldObjectStatements)
        every { statementService.delete(any<Set<StatementId>>()) } just runs

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, objects)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(oldObjectStatements.map { it.id }.toSet()) }
    }

    @Test
    fun `Given a list of objects, when old list of objects is empty, it creates new objects statements`() {
        val subjectId = ThingId("R123")
        val objects = listOf(ThingId("R1"), ThingId("R2"))
        val contributorId = ContributorId(UUID.randomUUID())

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        } returns pageOf()
        every { statementService.add(any()) } returns StatementId("S1")

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, objects)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = objects[0]
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
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

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldObjectStatements)
        every { statementService.delete(any<Set<StatementId>>()) } just runs

        statementCollectionPropertyUpdater.update(contributorId, subjectId, Predicates.reference, objects)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.reference,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(excessiveObjectStatements.map { it.id }.toSet()) }
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

        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { statementService.add(any()) } returns StatementId("S1")
        every { literalService.create(literalCreateCommand1) } returns literalId1
        every { literalService.create(literalCreateCommand2) } returns literalId2

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, literals)

        verify(exactly = 1) { statementService.delete(oldObjectStatements.map { it.id }.toSet()) }
        verify(exactly = 1) { literalService.create(literalCreateCommand1) }
        verify(exactly = 1) { literalService.create(literalCreateCommand2) }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = literalId1
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
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

        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { statementService.add(any()) } returns StatementId("S1")
        every { literalService.create(literalCreateCommand2) } returns literalId2

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, literals)

        verify(exactly = 1) { statementService.delete(setOf(oldObjectStatements[0].id)) }
        verify(exactly = 1) { literalService.create(literalCreateCommand2) }
        verify(exactly = 1) {
            statementService.add(
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

        every { statementService.delete(any<Set<StatementId>>()) } just runs

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, literals)

        verify(exactly = 1) { statementService.delete(oldObjectStatements.map { it.id }.toSet()) }
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

        every { statementService.add(any()) } returns StatementId("S1")
        every { literalService.create(literalCreateCommand1) } returns literalId1
        every { literalService.create(literalCreateCommand2) } returns literalId2

        statementCollectionPropertyUpdater.update(emptyList(), contributorId, subjectId, Predicates.reference, literals)

        verify(exactly = 1) { literalService.create(literalCreateCommand1) }
        verify(exactly = 1) { literalService.create(literalCreateCommand2) }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = literalId1
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
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

        every { statementService.delete(any<Set<StatementId>>()) } just runs

        statementCollectionPropertyUpdater.update(oldObjectStatements, contributorId, subjectId, Predicates.reference, literals)

        verify(exactly = 1) { statementService.delete(excessiveObjectStatements.map { it.id }.toSet()) }
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
