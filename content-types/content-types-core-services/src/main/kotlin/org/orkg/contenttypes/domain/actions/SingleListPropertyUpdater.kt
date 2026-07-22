package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateListUseCase

class SingleListPropertyUpdater(
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val listUseCases: ListUseCases,
) {
    internal fun updateLiteralListProperty(
        statements: Map<ThingId, List<GeneralStatement>>,
        contributorId: ContributorId,
        label: String,
        subjectId: ThingId,
        predicateId: ThingId,
        objects: List<String>,
        datatype: String = Literals.XSD.STRING.prefixedUri,
        extractionMethod: ExtractionMethod,
    ) {
        val listId = statements[subjectId].orEmpty()
            .filter { it.predicate.id == predicateId && it.`object` is Resource && Classes.list in (it.`object` as Resource).classes }
            .firstOrNull()
            ?.`object`?.id
        val elements by lazy {
            objects.map { value ->
                unsafeLiteralUseCases.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = value,
                        datatype = datatype,
                        extractionMethod = extractionMethod,
                    ),
                )
            }
        }
        if (listId != null) {
            val existingObjects = statements[listId].orEmpty()
                .filter { it.predicate.id == Predicates.hasListElement }
                .sortedBy { it.index }
                .map { it.`object`.label }
            if (objects != existingObjects) {
                listUseCases.update(
                    UpdateListUseCase.UpdateCommand(
                        id = listId,
                        contributorId = contributorId,
                        elements = elements,
                        extractionMethod = extractionMethod,
                    ),
                )
            }
        } else {
            val newListId = listUseCases.create(
                CreateListUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = label,
                    elements = elements,
                    extractionMethod = extractionMethod,
                ),
            )
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = predicateId,
                    objectId = newListId,
                    extractionMethod = extractionMethod,
                ),
            )
        }
    }

    internal fun updateListProperty(
        statements: Map<ThingId, List<GeneralStatement>>,
        contributorId: ContributorId,
        label: String,
        subjectId: ThingId,
        predicateId: ThingId,
        objectIds: List<ThingId>,
        extractionMethod: ExtractionMethod,
    ) {
        val listId = statements[subjectId].orEmpty()
            .filter { it.predicate.id == predicateId && it.`object` is Resource && Classes.list in (it.`object` as Resource).classes }
            .firstOrNull()
            ?.`object`?.id
        if (listId != null) {
            val existingObjectIds = statements[listId].orEmpty()
                .filter { it.predicate.id == Predicates.hasListElement }
                .sortedBy { it.index }
                .map { it.`object`.id }
            if (objectIds != existingObjectIds) {
                listUseCases.update(
                    UpdateListUseCase.UpdateCommand(
                        id = listId,
                        contributorId = contributorId,
                        elements = objectIds,
                        extractionMethod = extractionMethod,
                    ),
                )
            }
        } else {
            val newListId = listUseCases.create(
                CreateListUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = label,
                    elements = objectIds,
                    extractionMethod = extractionMethod,
                ),
            )
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = predicateId,
                    objectId = newListId,
                    extractionMethod = extractionMethod,
                ),
            )
        }
    }
}
