package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.contenttypes.api.LabeledObjectRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.ObjectIdAndLabel

interface LabeledObjectRepresentationAdapter {

    fun List<ObjectIdAndLabel>.mapToLabeledObjectRepresentation() : List<LabeledObjectRepresentation> =
        map { it.toLabeledObjectRepresentation() }

    fun ObjectIdAndLabel.toLabeledObjectRepresentation() : LabeledObjectRepresentation =
        LabeledObjectRepresentation(id, label)
}
