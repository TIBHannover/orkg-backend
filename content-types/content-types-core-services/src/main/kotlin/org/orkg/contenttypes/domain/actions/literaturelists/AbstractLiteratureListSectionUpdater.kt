package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ListSection
import org.orkg.contenttypes.domain.TextSection
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.tryDelete
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.contenttypes.input.ListSectionDefinition
import org.orkg.contenttypes.input.TextSectionDefinition
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class AbstractLiteratureListSectionUpdater(
    private val statementService: StatementUseCases,
    private val resourceService: ResourceUseCases,
    private val abstractLiteratureListSectionCreator: AbstractLiteratureListSectionCreator,
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater
) {
    constructor(
        literalService: LiteralUseCases,
        resourceService: ResourceUseCases,
        statementService: StatementUseCases,
    ) : this(
        statementService,
        resourceService,
        AbstractLiteratureListSectionCreator(statementService, resourceService, literalService),
        SingleStatementPropertyUpdater(literalService, statementService)
    )

    /**
     * Note:
     * This algorithm tries to reuse as many entry nodes (see structure below) as possible, meaning that it only creates new entry nodes if there are not enough.
     * Otherwise, it tries to assign new "hasLink" statements to each entry node. This is done to reduce the required graph operations, since the ordering is
     * implied by the creation date of the hasEntry statement, which will not be modified in the process, preserving the order.
     *
     * Structure of a TextSection:
     *      (TextSection)-hasEntry->(Entry)
     *      (Entry)-hasLink->(Resource)
     *      (Entry)-description->(Literal)
     */
    internal fun updateListSection(
        contributorId: ContributorId,
        newSection: ListSectionDefinition,
        oldSection: ListSection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) {
        if (newSection.entries != oldSection.entries.map { ListSectionDefinition.Entry(it.value.id, it.description) }) {
            val connectionIterator = statements[oldSection.id].orEmpty()
                .filter { it.predicate.id == Predicates.hasEntry }
                .sortedBy { it.createdAt }
                .map { hasEntry ->
                    val entryStatements = statements[hasEntry.`object`.id]!!
                    EntryConnection(
                        hasEntry,
                        entryStatements.single { it.predicate.id == Predicates.hasLink },
                        entryStatements.wherePredicate(Predicates.description).singleOrNull()
                    )
                }
                .listIterator()
            val newEntriesIterator = newSection.entries.listIterator()
            val entryNodesToRemove = mutableSetOf<ThingId>()
            val statementsToRemove = mutableSetOf<StatementId>()
            var excessNodesCount = oldSection.entries.size - newSection.entries.size

            while (connectionIterator.hasNext()) {
                var connection = connectionIterator.next()
                if (newEntriesIterator.hasNext()) {
                    val newEntry = newEntriesIterator.next()
                    while (newEntry != connection.toEntry()) {
                        if (excessNodesCount > 0) {
                            // delete entry node, because we still have excess entry nodes left,
                            // possibly removing the need for an update of the next hasLink statement
                            statementsToRemove += connection.statementIds()
                            entryNodesToRemove += connection.hasEntryStatement.`object`.id
                            connection = connectionIterator.next()
                            excessNodesCount--
                        } else {
                            if (newEntry.id != connection.hasLinkStatement.`object`.id) {
                                statementsToRemove += connection.hasLinkStatement.id
                                statementService.add(
                                    userId = contributorId,
                                    subject = connection.hasEntryStatement.`object`.id,
                                    predicate = Predicates.hasLink,
                                    `object` = newEntry.id
                                )
                            }
                            singleStatementPropertyUpdater.updateOptionalProperty(
                                statements = listOfNotNull(connection.hasDescriptionStatement),
                                contributorId = contributorId,
                                subjectId = connection.hasEntryStatement.`object`.id,
                                predicateId = Predicates.description,
                                label = newEntry.description
                            )
                            break
                        }
                    }
                } else {
                    statementsToRemove += connection.statementIds()
                    entryNodesToRemove += connection.hasEntryStatement.`object`.id
                }
            }

            if (statementsToRemove.isNotEmpty()) {
                statementService.delete(statementsToRemove)
            }
            entryNodesToRemove.forEach { resourceService.tryDelete(it, contributorId) }

            while (newEntriesIterator.hasNext()) {
                val entryId = abstractLiteratureListSectionCreator.createListSectionEntry(contributorId, newEntriesIterator.next())
                statementService.add(
                    userId = contributorId,
                    subject = oldSection.id,
                    predicate = Predicates.hasEntry,
                    `object` = entryId
                )
            }
        }
    }

    internal fun updateTextSection(
        contributorId: ContributorId,
        newSection: TextSectionDefinition,
        oldSection: TextSection,
        statements: Map<ThingId, List<GeneralStatement>>
    ) {
        if (newSection.heading != oldSection.heading) {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldSection.id,
                    label = newSection.heading
                )
            )
        }
        if (newSection.headingSize != oldSection.headingSize) {
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasHeadingLevel,
                label = newSection.headingSize.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        if (newSection.text != oldSection.text) {
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = statements[oldSection.id].orEmpty(),
                contributorId = contributorId,
                subjectId = oldSection.id,
                predicateId = Predicates.hasContent,
                label = newSection.text
            )
        }
    }

    private data class EntryConnection(
        val hasEntryStatement: GeneralStatement,
        val hasLinkStatement: GeneralStatement,
        val hasDescriptionStatement: GeneralStatement?
    ) {
        fun toEntry(): ListSectionDefinition.Entry =
            ListSectionDefinition.Entry(
                hasLinkStatement.`object`.id,
                hasDescriptionStatement?.`object`?.label
            )

        fun statementIds(): Set<StatementId> =
            setOfNotNull(hasEntryStatement.id, hasLinkStatement.id, hasDescriptionStatement?.id)
    }
}
