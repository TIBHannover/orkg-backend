package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.LabeledComparisonPathRepresentation
import org.orkg.contenttypes.domain.LabeledComparisonPath
import org.springframework.data.domain.Page
import java.util.Optional

interface LabeledComparisonPathRepresentationAdapter {
    fun Optional<LabeledComparisonPath>.mapToLabeledComparisonPathRepresentation(): Optional<LabeledComparisonPathRepresentation> =
        map { it.toLabeledComparisonPathRepresentation() }

    fun Page<LabeledComparisonPath>.mapToLabeledComparisonPathRepresentation(): Page<LabeledComparisonPathRepresentation> =
        map { it.toLabeledComparisonPathRepresentation() }

    fun List<LabeledComparisonPath>.mapToLabeledComparisonPathRepresentation(): List<LabeledComparisonPathRepresentation> =
        map { it.toLabeledComparisonPathRepresentation() }

    fun LabeledComparisonPath.toLabeledComparisonPathRepresentation(): LabeledComparisonPathRepresentation =
        LabeledComparisonPathRepresentation(
            id = id,
            label = label,
            description = description,
            sources = sources,
            type = type,
            children = children.map { it.toLabeledComparisonPathRepresentation() },
        )
}
