package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.common.PageRequests
import org.orkg.graph.domain.Predicate
import org.orkg.graph.adapter.input.rest.PredicateRepresentation
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page

interface PredicateRepresentationAdapter {
    val statementService: StatementUseCases

    fun Optional<Predicate>.mapToPredicateRepresentation(): Optional<PredicateRepresentation> =
        map {
            it.toPredicateRepresentation(
                statementService.findAll(
                    pageable = PageRequests.SINGLE,
                    subjectId = it.id,
                    predicateId = Predicates.description,
                    objectClasses = setOf(Classes.literal)
                ).singleOrNull()?.`object`?.label
            )
        }

    fun Page<Predicate>.mapToPredicateRepresentation(): Page<PredicateRepresentation> {
        val descriptions = when {
            content.isNotEmpty() -> {
                val ids = content.mapTo(mutableSetOf()) { it.id }
                statementService.findAllDescriptions(ids)
            }
            else -> emptyMap()
        }
        return map { it.toPredicateRepresentation(descriptions[it.id]) }
    }

    fun Predicate.toPredicateRepresentation(
        description: String?
    ): PredicateRepresentation =
        PredicateRepresentation(id, label, description, createdAt, createdBy, modifiable)
}
