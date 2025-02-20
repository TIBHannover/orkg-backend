package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.LabeledObjectRepresentation
import org.orkg.contenttypes.domain.ObjectIdAndLabel

interface LabeledObjectRepresentationAdapter {
    fun List<ObjectIdAndLabel>.mapToLabeledObjectRepresentation(): List<LabeledObjectRepresentation> =
        map { it.toLabeledObjectRepresentation() }

    fun Set<ObjectIdAndLabel>.mapToLabeledObjectRepresentation(): Set<LabeledObjectRepresentation> =
        mapTo(mutableSetOf()) { it.toLabeledObjectRepresentation() }

    fun ObjectIdAndLabel.toLabeledObjectRepresentation(): LabeledObjectRepresentation =
        LabeledObjectRepresentation(id, label)
}
