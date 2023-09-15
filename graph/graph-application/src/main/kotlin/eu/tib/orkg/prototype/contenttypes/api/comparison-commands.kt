package eu.tib.orkg.prototype.contenttypes.api

import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface PublishComparisonUseCase {
    fun publish(id: ThingId, subject: String, description: String)
}
