package eu.tib.orkg.prototype.content_types.api

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface CreatePaperUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val id: ThingId? = null,
        val contributorId: ContributorId,
        val title: String,
        val researchFields: List<ThingId>,
        val identifiers: Map<String, String>?,
        val publicationInfo: PublicationInfo?,
        val authors: List<Author>,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>,
        val extractionMethod: ExtractionMethod
    ) {
        data class PublicationInfo(
            val publishedMonth: Int?,
            val publishedYear: Long?,
            val publishedIn: String?,
            val url: String?
        )

        data class Author(
            val name: String?,
            val identifiers: Map<String, String>?,
            val homepage: String?
        )
    }
}
