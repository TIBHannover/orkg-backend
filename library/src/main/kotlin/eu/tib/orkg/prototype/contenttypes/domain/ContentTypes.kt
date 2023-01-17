package eu.tib.orkg.prototype.contenttypes.domain

import eu.tib.orkg.prototype.statements.domain.model.Resource

typealias ContentTypeId = String

sealed interface ContentType {
    val id: ContentTypeId
}

class Contribution(override val id: ContentTypeId) : ContentType

class Paper(
    override val id: ContentTypeId,
    val resource: Resource // TODO temporary (to be removed)
) : ContentType

class ResearchProblem(override val id: ContentTypeId) : ContentType

class Visualization(override val id: ContentTypeId) : ContentType

