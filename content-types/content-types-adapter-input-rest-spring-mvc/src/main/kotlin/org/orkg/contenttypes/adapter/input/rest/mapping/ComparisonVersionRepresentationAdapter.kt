package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.contenttypes.adapter.input.rest.ComparisonVersionRepresentation
import org.orkg.contenttypes.domain.ComparisonVersion
import org.springframework.data.domain.Page

interface ComparisonVersionRepresentationAdapter {

    fun Optional<ComparisonVersion>.mapToComparisonVersionRepresentation(): Optional<ComparisonVersionRepresentation> =
        map { it.toComparisonVersionRepresentation() }

    fun Page<ComparisonVersion>.mapToComparisonVersionRepresentation(): Page<ComparisonVersionRepresentation> =
        map { it.toComparisonVersionRepresentation() }

    fun ComparisonVersion.toComparisonVersionRepresentation(): ComparisonVersionRepresentation =
        ComparisonVersionRepresentation(id, label, createdAt)
}
