package eu.tib.orkg.prototype.contenttypes.domain

typealias ContentTypeId = String

sealed interface ContentType {
    val id: ContentTypeId
}

class Contribution(override val id: ContentTypeId) : ContentType

class Paper(
    override val id: ContentTypeId,
    val title: ContentTypeId,
) : ContentType

class ResearchProblem(override val id: ContentTypeId) : ContentType

class Visualization(override val id: ContentTypeId) : ContentType
