package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.PaperResourceWithPath
import org.orkg.graph.input.CreateObjectUseCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface LegacyPaperUseCases :
    LegacyRetrievePaperUseCase,
    LegacyCreatePaperUseCase

interface LegacyRetrievePaperUseCase {
    fun findAllPapersRelatedToResource(related: ThingId, pageable: Pageable): Page<PaperResourceWithPath>
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
        val paper: PaperDefinition,
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
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
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
        val orcid: String?,
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
