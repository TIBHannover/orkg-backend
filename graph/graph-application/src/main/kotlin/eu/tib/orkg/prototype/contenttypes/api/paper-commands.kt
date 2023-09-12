package eu.tib.orkg.prototype.contenttypes.api

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase.*
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId

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
        resources: Map<String, CreatePaperUseCase.CreateCommand.ResourceDefinition> = emptyMap(),
        literals: Map<String, CreatePaperUseCase.CreateCommand.LiteralDefinition> = emptyMap(),
        predicates: Map<String, CreatePaperUseCase.CreateCommand.PredicateDefinition> = emptyMap(),
        lists: Map<String, CreatePaperUseCase.CreateCommand.ListDefinition> = emptyMap(),
        contribution: CreatePaperUseCase.CreateCommand.Contribution
    ) : CreatePaperUseCase.CreateCommand.PaperContents(resources, literals, predicates, lists, listOf(contribution))
}
