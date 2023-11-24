package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.graph.domain.Predicate
import org.orkg.graph.input.PredicateRepresentation
import org.springframework.data.domain.Page

interface PredicateRepresentationAdapter {

    fun Optional<Predicate>.mapToPredicateRepresentation(): Optional<PredicateRepresentation> =
        map { it.toPredicateRepresentation() }

    fun Page<Predicate>.mapToPredicateRepresentation(): Page<PredicateRepresentation> =
        map { it.toPredicateRepresentation() }

    fun Predicate.toPredicateRepresentation(): PredicateRepresentation =
        PredicateRepresentation(id, label, description, createdAt, createdBy)
}
