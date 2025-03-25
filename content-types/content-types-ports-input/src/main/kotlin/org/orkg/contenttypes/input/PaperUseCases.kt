package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.Paper
import org.orkg.contenttypes.domain.PaperWithStatementCount
import org.orkg.contenttypes.domain.identifiers.DOI
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface PaperUseCases :
    RetrievePaperUseCase,
    CreatePaperUseCase,
    CreateContributionUseCase,
    UpdatePaperUseCase,
    PublishPaperUseCase

interface RetrievePaperUseCase {
    fun findById(id: ThingId): Optional<Paper>

    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        doi: String? = null,
        doiPrefix: String? = null,
        visibility: VisibilityFilter? = null,
        verified: Boolean? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
        researchField: ThingId? = null,
        includeSubfields: Boolean = false,
        sustainableDevelopmentGoal: ThingId? = null,
        mentionings: Set<ThingId>? = emptySet(),
    ): Page<Paper>

    fun findAllContributorsByPaperId(id: ThingId, pageable: Pageable): Page<ContributorId>

    fun countAllStatementsAboutPapers(pageable: Pageable): Page<PaperWithStatementCount>

    fun existsByDOI(doi: DOI): Optional<ThingId>

    fun existsByTitle(label: ExactSearchString): Optional<ThingId>
}

interface CreatePaperUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val title: String,
        val researchFields: List<ThingId>,
        val identifiers: Map<String, List<String>>,
        val publicationInfo: PublicationInfoCommand?,
        val authors: List<Author>,
        val sustainableDevelopmentGoals: Set<ThingId>,
        val mentionings: Set<ThingId>,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>,
        val contents: PaperContents?,
        val extractionMethod: ExtractionMethod,
    ) {
        data class PaperContents(
            override val resources: Map<String, CreateResourceCommandPart> = emptyMap(),
            override val literals: Map<String, CreateLiteralCommandPart> = emptyMap(),
            override val predicates: Map<String, CreatePredicateCommandPart> = emptyMap(),
            override val lists: Map<String, CreateListCommandPart> = emptyMap(),
            val contributions: List<CreateContributionCommandPart>,
        ) : CreateThingsCommand {
            override val classes: Map<String, CreateClassCommandPart>
                get() = emptyMap()
        }
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
        val publicationInfo: PublicationInfoCommand?,
        val authors: List<Author>?,
        val sustainableDevelopmentGoals: Set<ThingId>?,
        val mentionings: Set<ThingId>?,
        val observatories: List<ObservatoryId>?,
        val organizations: List<OrganizationId>?,
        val extractionMethod: ExtractionMethod?,
        val visibility: Visibility?,
        val verified: Boolean?,
    )
}

interface PublishPaperUseCase {
    fun publish(command: PublishCommand): ThingId

    data class PublishCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val subject: String,
        val description: String,
        val authors: List<Author>,
    )
}
