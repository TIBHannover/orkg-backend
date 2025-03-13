package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Contribution
import org.orkg.graph.domain.ExtractionMethod
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ContributionUseCases :
    RetrieveContributionUseCase,
    CreateContributionUseCase

interface RetrieveContributionUseCase {
    fun findById(id: ThingId): Optional<Contribution>

    fun findAll(pageable: Pageable): Page<Contribution>
}

interface CreateContributionUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val paperId: ThingId,
        val extractionMethod: ExtractionMethod,
        override val resources: Map<String, CreateResourceCommandPart> = emptyMap(),
        override val literals: Map<String, CreateLiteralCommandPart> = emptyMap(),
        override val predicates: Map<String, CreatePredicateCommandPart> = emptyMap(),
        override val lists: Map<String, CreateListCommandPart> = emptyMap(),
        val contribution: CreateContributionCommandPart,
    ) : CreateThingsCommand {
        override val classes: Map<String, CreateClassCommandPart>
            get() = emptyMap()
    }
}
