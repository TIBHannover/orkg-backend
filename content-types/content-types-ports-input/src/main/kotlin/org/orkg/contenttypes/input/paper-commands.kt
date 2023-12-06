package org.orkg.contenttypes.input

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.PublicationInfo
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.input.CreateObjectUseCase

interface CreatePaperUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val title: String,
        val researchFields: List<ThingId>,
        val identifiers: Map<String, String>,
        val publicationInfo: PublicationInfo?,
        val authors: List<Author>,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>,
        val contents: PaperContents?,
        val extractionMethod: ExtractionMethod
    ) {
        open class PaperContents(
            val resources: Map<String, ResourceDefinition> = emptyMap(),
            val literals: Map<String, LiteralDefinition> = emptyMap(),
            val predicates: Map<String, PredicateDefinition> = emptyMap(),
            val lists: Map<String, ListDefinition> = emptyMap(),
            val contributions: List<Contribution>
        )

        data class ResourceDefinition(
            val label: String,
            val classes: Set<ThingId> = emptySet()
        )

        data class LiteralDefinition(
            val label: String,
            val dataType: String = Literals.XSD.STRING.prefixedUri
        )

        data class PredicateDefinition(
            val label: String,
            val description: String? = null
        )

        data class ListDefinition(
            val label: String,
            val elements: List<String> = emptyList()
        )

        data class Contribution(
            val label: String,
            val classes: Set<ThingId> = emptySet(),
            val statements: Map<String, List<StatementObjectDefinition>>
        )

        data class StatementObjectDefinition(
            val id: String,
            val statements: Map<String, List<StatementObjectDefinition>>? = null
        )
    }
}

interface CreateContributionUseCase {
    fun createContribution(command: CreateCommand): ThingId

    class CreateCommand(
        val contributorId: ContributorId,
        val paperId: ThingId,
        val extractionMethod: ExtractionMethod,
        resources: Map<String, CreatePaperUseCase.CreateCommand.ResourceDefinition> = emptyMap(),
        literals: Map<String, CreatePaperUseCase.CreateCommand.LiteralDefinition> = emptyMap(),
        predicates: Map<String, CreatePaperUseCase.CreateCommand.PredicateDefinition> = emptyMap(),
        lists: Map<String, CreatePaperUseCase.CreateCommand.ListDefinition> = emptyMap(),
        contribution: CreatePaperUseCase.CreateCommand.Contribution
    ) : CreatePaperUseCase.CreateCommand.PaperContents(resources, literals, predicates, lists, listOf(contribution))
}

interface PublishPaperUseCase {
    fun publish(id: ThingId, contributorId: ContributorId, subject: String, description: String)
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
