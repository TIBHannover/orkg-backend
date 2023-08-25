package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.contenttypes.application.LabeledObjectRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.ObjectIdAndLabel
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface LabeledObjectRepresentationAdapter {

    fun List<ObjectIdAndLabel>.mapToLabeledObjectRepresentation() : List<LabeledObjectRepresentation> =
        map { it.toLabeledObjectRepresentation() }

    fun ObjectIdAndLabel.toLabeledObjectRepresentation() : LabeledObjectRepresentation =
        object : LabeledObjectRepresentation {
            override val id: ThingId = this@toLabeledObjectRepresentation.id
            override val label: String = this@toLabeledObjectRepresentation.label
        }
}
