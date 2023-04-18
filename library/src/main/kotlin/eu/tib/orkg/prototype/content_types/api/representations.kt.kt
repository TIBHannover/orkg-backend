package eu.tib.orkg.prototype.content_types.api

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime

interface PaperRepresentation {
    val id: ThingId
    val title: String
    @get:JsonProperty("research_fields")
    val researchFields: List<ThingId>
    val identifiers: Map<String, String>
    @get:JsonProperty("publication_info")
    val publicationInfo: PublicationInfoRepresentation
    val authors: List<AuthorRepresentation>
    @get:JsonProperty("observatories")
    val observatories: List<ObservatoryId>
    @get:JsonProperty("organizations")
    val organizations: List<OrganizationId>
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime
    @get:JsonProperty("created_by")
    val createdBy: ContributorId
    val featured: Boolean
    val unlisted: Boolean
    val verified: Boolean
    val deleted: Boolean
}

interface PublicationInfoRepresentation {
    @get:JsonProperty("published_month")
    val publishedMonth: Int?
    @get:JsonProperty("published_year")
    val publishedYear: Long?
    @get:JsonProperty("published_in")
    val publishedIn: String?
    val url: String?
}

interface AuthorRepresentation {
    val id: ThingId?
    val name: String
    val identifiers: Map<String, String>
    val homepage: String?
}
