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
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateListUseCase
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.util.UUID

internal class SingleListPropertyUpdaterUnitTest : MockkBaseTest {
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val listUseCases: ListUseCases = mockk()

    private val singleListPropertyUpdater = SingleListPropertyUpdater(
        unsafeLiteralUseCases,
        unsafeStatementUseCases,
        listUseCases,
    )

    @Test
    fun `Given a list of strings, when list already exists and elements are equal, it does nothing`() {
        val subjectId = ThingId("R123")
        val objects = listOf("1", "2")
        val list = createResource(id = ThingId("List"), classes = setOf(Classes.list))
        val statements = listOf(
            createStatement(
                subject = createResource(subjectId),
                predicate = createPredicate(Predicates.reference),
                `object` = list,
            ),
            *objects.toLiteralListObjectStatements(list).toTypedArray(),
        ).groupBy { it.subject.id }
        val contributorId = ContributorId(UUID.randomUUID())
        val extractionMethod = ExtractionMethod.MANUAL

        singleListPropertyUpdater.updateLiteralListProperty(
            statements = statements,
            contributorId = contributorId,
            label = "test",
            subjectId = subjectId,
            predicateId = Predicates.reference,
            objects = objects,
            extractionMethod = extractionMethod,
        )
    }

    @Test
    fun `Given a list of strings, when list already exists but elements differ, it updates the list elements`() {
        val subjectId = ThingId("R123")
        val oldObjects = listOf("1", "2")
        val newObjects = listOf("new")
        val list = createResource(id = ThingId("List"), classes = setOf(Classes.list))
        val statements = listOf(
            createStatement(
                subject = createResource(subjectId),
                predicate = createPredicate(Predicates.reference),
                `object` = list,
            ),
            *oldObjects.toLiteralListObjectStatements(list).toTypedArray(),
        ).groupBy { it.subject.id }
        val contributorId = ContributorId(UUID.randomUUID())
        val extractionMethod = ExtractionMethod.MANUAL
        val newLiteralId = ThingId("L159")

        every { unsafeLiteralUseCases.create(any()) } returns newLiteralId
        every { listUseCases.update(any()) } just runs

        singleListPropertyUpdater.updateLiteralListProperty(
            statements = statements,
            contributorId = contributorId,
            label = "test",
            subjectId = subjectId,
            predicateId = Predicates.reference,
            objects = newObjects,
            extractionMethod = extractionMethod,
        )

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = "new",
                    datatype = Literals.XSD.STRING.prefixedUri,
                    extractionMethod = extractionMethod,
                ),
            )
        }
        verify(exactly = 1) {
            listUseCases.update(
                UpdateListUseCase.UpdateCommand(
                    id = list.id,
                    contributorId = contributorId,
                    elements = listOf(newLiteralId),
                    extractionMethod = extractionMethod,
                ),
            )
        }
    }

    @Test
    fun `Given a list of strings, when list does not exist, it creates the list`() {
        val subjectId = ThingId("R123")
        val newObjects = listOf("new")
        val listId = ThingId("List")
        val contributorId = ContributorId(UUID.randomUUID())
        val extractionMethod = ExtractionMethod.MANUAL
        val newLiteralId = ThingId("L159")

        every { unsafeLiteralUseCases.create(any()) } returns newLiteralId
        every { listUseCases.create(any()) } returns listId
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S123")

        singleListPropertyUpdater.updateLiteralListProperty(
            statements = emptyMap(),
            contributorId = contributorId,
            label = "test",
            subjectId = subjectId,
            predicateId = Predicates.reference,
            objects = newObjects,
            extractionMethod = extractionMethod,
        )

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = "new",
                    datatype = Literals.XSD.STRING.prefixedUri,
                    extractionMethod = extractionMethod,
                ),
            )
        }
        verify(exactly = 1) {
            listUseCases.create(
                CreateListUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = "test",
                    elements = listOf(newLiteralId),
                    extractionMethod = extractionMethod,
                ),
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = listId,
                    extractionMethod = extractionMethod,
                ),
            )
        }
    }

    @Test
    fun `Given a list of object ids, when list already exists and elements are equal, it does nothing`() {
        val subjectId = ThingId("R123")
        val objectIds = listOf(ThingId("1"), ThingId("2"))
        val list = createResource(id = ThingId("List"), classes = setOf(Classes.list))
        val statements = listOf(
            createStatement(
                subject = createResource(subjectId),
                predicate = createPredicate(Predicates.reference),
                `object` = list,
            ),
            *objectIds.toListObjectStatements(list).toTypedArray(),
        ).groupBy { it.subject.id }
        val contributorId = ContributorId(UUID.randomUUID())
        val extractionMethod = ExtractionMethod.MANUAL

        singleListPropertyUpdater.updateListProperty(
            statements = statements,
            contributorId = contributorId,
            label = "test",
            subjectId = subjectId,
            predicateId = Predicates.reference,
            objectIds = objectIds,
            extractionMethod = extractionMethod,
        )
    }

    @Test
    fun `Given a list of object ids, when list already exists but elements differ, it updates the list elements`() {
        val subjectId = ThingId("R123")
        val oldObjectIds = listOf(ThingId("1"), ThingId("2"))
        val newObjectIds = listOf(ThingId("new"))
        val list = createResource(id = ThingId("List"), classes = setOf(Classes.list))
        val statements = listOf(
            createStatement(
                subject = createResource(subjectId),
                predicate = createPredicate(Predicates.reference),
                `object` = list,
            ),
            *oldObjectIds.toListObjectStatements(list).toTypedArray(),
        ).groupBy { it.subject.id }
        val contributorId = ContributorId(UUID.randomUUID())
        val extractionMethod = ExtractionMethod.MANUAL

        every { listUseCases.update(any()) } just runs

        singleListPropertyUpdater.updateListProperty(
            statements = statements,
            contributorId = contributorId,
            label = "test",
            subjectId = subjectId,
            predicateId = Predicates.reference,
            objectIds = newObjectIds,
            extractionMethod = extractionMethod,
        )

        verify(exactly = 1) {
            listUseCases.update(
                UpdateListUseCase.UpdateCommand(
                    id = list.id,
                    contributorId = contributorId,
                    elements = newObjectIds,
                    extractionMethod = extractionMethod,
                ),
            )
        }
    }

    @Test
    fun `Given a list of object ids, when list does not exist, it creates the list`() {
        val subjectId = ThingId("R123")
        val newObjectIds = listOf(ThingId("new"))
        val listId = ThingId("List")
        val contributorId = ContributorId(UUID.randomUUID())
        val extractionMethod = ExtractionMethod.MANUAL

        every { listUseCases.create(any()) } returns listId
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S123")

        singleListPropertyUpdater.updateListProperty(
            statements = emptyMap(),
            contributorId = contributorId,
            label = "test",
            subjectId = subjectId,
            predicateId = Predicates.reference,
            objectIds = newObjectIds,
            extractionMethod = extractionMethod,
        )

        verify(exactly = 1) {
            listUseCases.create(
                CreateListUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = "test",
                    elements = newObjectIds,
                    extractionMethod = extractionMethod,
                ),
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.reference,
                    objectId = listId,
                    extractionMethod = extractionMethod,
                ),
            )
        }
    }

    private fun Collection<String>.toLiteralListObjectStatements(subject: Resource): List<GeneralStatement> =
        mapIndexed { index, string ->
            createStatement(
                id = StatementId("S$index"),
                subject = subject,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createLiteral(ThingId("L$index"), string),
                index = index,
            )
        }

    private fun Collection<ThingId>.toListObjectStatements(subject: Resource): List<GeneralStatement> =
        mapIndexed { index, id ->
            createStatement(
                id = StatementId("S$index"),
                subject = subject,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createResource(id),
                index = index,
            )
        }
}
