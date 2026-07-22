package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.ComparisonSearchProtocolRepresentation
import org.orkg.contenttypes.domain.ComparisonSearchProtocol
import org.springframework.data.domain.Page
import java.util.Optional

interface ComparisonSearchProtocolRepresentationAdapter : ThingReferenceRepresentationAdapter {
    fun Optional<ComparisonSearchProtocol>.mapToComparisonSearchProtocolRepresentation(): Optional<ComparisonSearchProtocolRepresentation> =
        map { it.toComparisonSearchProtocolRepresentation() }

    fun Page<ComparisonSearchProtocol>.mapToComparisonSearchProtocolRepresentation(): Page<ComparisonSearchProtocolRepresentation> =
        map { it.toComparisonSearchProtocolRepresentation() }

    fun List<ComparisonSearchProtocol>.mapToComparisonSearchProtocolRepresentation(): List<ComparisonSearchProtocolRepresentation> =
        map { it.toComparisonSearchProtocolRepresentation() }

    fun ComparisonSearchProtocol.toComparisonSearchProtocolRepresentation(): ComparisonSearchProtocolRepresentation =
        ComparisonSearchProtocolRepresentation(
            inclusionCriteria = inclusionCriteria,
            exclusionCriteria = exclusionCriteria,
            searchEngines = searchEngines.map { it.toThingReferenceRepresentation() },
            searchStrings = searchStrings,
            researchQuestions = researchQuestions,
            numberOfStudiesOriginallyReturned = numberOfStudiesOriginallyReturned,
            numberOfStudiesRetained = numberOfStudiesRetained,
        )
}
