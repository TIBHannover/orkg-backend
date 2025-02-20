package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ClassDefinition
import org.orkg.contenttypes.input.ListDefinition
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.PredicateDefinition
import org.orkg.contenttypes.input.ResourceDefinition
import org.orkg.contenttypes.input.ThingDefinition
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.output.StatementRepository

internal val String.isTempId: Boolean get() = startsWith('#') || startsWith('^')

internal fun <T, S> List<Action<T, S>>.execute(command: T, initialState: S) =
    fold(initialState) { state, executor -> executor(command, state) }

internal fun Thing.toThingDefinition(statementRepository: StatementRepository? = null): ThingDefinition =
    when (this) {
        is Resource ->
            if (Classes.list in classes) {
                ListDefinition(label, emptyList())
            } else {
                ResourceDefinition(label, classes)
            }
        is Class -> ClassDefinition(label, uri)
        is Predicate -> PredicateDefinition(
            label = label,
            description = statementRepository?.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )?.singleOrNull()?.`object`?.label
        )
        is Literal -> LiteralDefinition(label, datatype)
    }

internal fun ResourceUseCases.tryDelete(id: ThingId, contributorId: ContributorId): Boolean {
    try {
        delete(id, contributorId)
    } catch (e: Exception) {
        return false
    }
    return true
}
