package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Certainty
import org.orkg.contenttypes.domain.RosettaStoneTemplate
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface RosettaStoneStatementUseCases :
    RetrieveRosettaStoneStatementUseCase,
    CreateRosettaStoneStatementUseCase,
    UpdateRosettaStoneStatementUseCase,
    DeleteRosettaStoneStatementUseCase

interface RetrieveRosettaStoneTemplateUseCase {
    fun findById(id: ThingId): Optional<RosettaStoneTemplate>

    fun findAll(
        searchString: SearchString? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
        pageable: Pageable,
    ): Page<RosettaStoneTemplate>
}

interface CreateRosettaStoneStatementUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val templateId: ThingId,
        val contributorId: ContributorId,
        val context: ThingId?,
        val subjects: List<String>,
        val objects: List<List<String>>,
        val certainty: Certainty,
        val negated: Boolean,
        val extractionMethod: ExtractionMethod,
        override val resources: Map<String, CreateResourceCommandPart> = emptyMap(),
        override val literals: Map<String, CreateLiteralCommandPart> = emptyMap(),
        override val predicates: Map<String, CreatePredicateCommandPart> = emptyMap(),
        override val classes: Map<String, CreateClassCommandPart> = emptyMap(),
        override val lists: Map<String, CreateListCommandPart> = emptyMap(),
        val observatories: List<ObservatoryId> = emptyList(),
        val organizations: List<OrganizationId> = emptyList(),
        val visibility: Visibility = Visibility.DEFAULT,
        val modifiable: Boolean = true,
    ) : CreateThingsCommand
}

interface UpdateRosettaStoneStatementUseCase {
    fun update(command: UpdateCommand): ThingId

    data class UpdateCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val subjects: List<String>,
        val objects: List<List<String>>,
        val certainty: Certainty,
        val negated: Boolean,
        val extractionMethod: ExtractionMethod,
        override val resources: Map<String, CreateResourceCommandPart> = emptyMap(),
        override val literals: Map<String, CreateLiteralCommandPart> = emptyMap(),
        override val predicates: Map<String, CreatePredicateCommandPart> = emptyMap(),
        override val classes: Map<String, CreateClassCommandPart> = emptyMap(),
        override val lists: Map<String, CreateListCommandPart> = emptyMap(),
        val observatories: List<ObservatoryId> = emptyList(),
        val organizations: List<OrganizationId> = emptyList(),
        val visibility: Visibility = Visibility.DEFAULT,
        val modifiable: Boolean = true,
    ) : CreateThingsCommand
}

interface DeleteRosettaStoneStatementUseCase {
    fun softDelete(id: ThingId, contributorId: ContributorId)

    fun delete(id: ThingId, contributorId: ContributorId)
}
