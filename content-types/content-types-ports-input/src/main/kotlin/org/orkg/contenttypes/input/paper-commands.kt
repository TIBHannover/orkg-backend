package org.orkg.contenttypes.input

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.PublicationInfo
import org.orkg.contenttypes.input.ThingDefinitions.ListDefinition
import org.orkg.contenttypes.input.ThingDefinitions.LiteralDefinition
import org.orkg.contenttypes.input.ThingDefinitions.PredicateDefinition
import org.orkg.contenttypes.input.ThingDefinitions.ResourceDefinition
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.input.CreateObjectUseCase

interface CreatePaperUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val title: String,
        val researchFields: List<ThingId>,
        val identifiers: Map<String, List<String>>,
        val publicationInfo: PublicationInfo?,
        val authors: List<Author>,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>,
        val contents: PaperContents?,
        val extractionMethod: ExtractionMethod
    ) {
        data class PaperContents(
            override val resources: Map<String, ResourceDefinition> = emptyMap(),
            override val literals: Map<String, LiteralDefinition> = emptyMap(),
            override val predicates: Map<String, PredicateDefinition> = emptyMap(),
            override val lists: Map<String, ListDefinition> = emptyMap(),
            val contributions: List<ContributionDefinition>
        ) : ThingDefinitions
    }
}

interface UpdatePaperUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val paperId: ThingId,
        val contributorId: ContributorId,
        val title: String?,
        val researchFields: List<ThingId>?,
        val identifiers: Map<String, List<String>>?,
        val publicationInfo: PublicationInfo?,
        val authors: List<Author>?,
        val observatories: List<ObservatoryId>?,
        val organizations: List<OrganizationId>?
    )
}

interface PublishPaperUseCase {
    fun publish(command: PublishCommand)

    data class PublishCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val subject: String,
        val description: String,
        val authors: List<Author>
    )
}

interface LegacyCreatePaperUseCase {
    fun addPaperContent(
        request: LegacyCreatePaperRequest,
        mergeIfExists: Boolean,
        userUUID: UUID,
    ): ThingId

    /**
     * Main entry point, basic skeleton of a paper
     */
    data class LegacyCreatePaperRequest(
        val predicates: List<HashMap<String, String>>?,
        val paper: PaperDefinition
    )

    /**
     * Concrete paper holder class
     * contains meta-information of papers
     * and helper methods
     */
    data class PaperDefinition(
        val title: String,
        val doi: String?,
        val authors: List<Author>?,
        val publicationMonth: Int?,
        val publicationYear: Int?,
        val publishedIn: String?,
        val url: String?,
        val researchField: ThingId,
        val contributions: List<CreateObjectUseCase.NamedObject>?,
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
    ) {
        /**
         * Check if the paper has a published in venue
         */
        fun hasPublishedIn(): Boolean =
            publishedIn?.isNotEmpty() == true

        /**
         * Check if the paper has a DOI
         */
        fun hasDOI(): Boolean =
            doi?.isNotEmpty() == true

        /**
         * Check if the paper has a URL
         */
        fun hasUrl(): Boolean =
            url?.isNotEmpty() == true

        /**
         * Check if the paper has contributions
         */
        fun hasContributions() =
            !contributions.isNullOrEmpty()

        /**
         * Check if the paper has authors
         */
        fun hasAuthors() =
            !authors.isNullOrEmpty()

        /**
         * Check if the paper has a publication month value
         */
        fun hasPublicationMonth() =
            publicationMonth != null

        /**
         * Check if the paper has a publication year value
         */
        fun hasPublicationYear() =
            publicationYear != null
    }

    /**
     * Author class container
     */
    data class Author(
        val id: ThingId?,
        val label: String?,
        val orcid: String?
    ) {
        /**
         * Check if the author has a name (label)
         * and an ORCID at the same time
         */
        fun hasNameAndOrcid() =
            label != null && orcid != null

        /**
         * Check if the author is an existing Resource
         * i.e., the id of the author is not null
         */
        fun hasId() = id != null
    }
}
