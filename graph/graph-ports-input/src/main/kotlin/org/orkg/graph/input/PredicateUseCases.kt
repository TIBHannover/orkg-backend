package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.SearchString
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface PredicateUseCases :
    CreatePredicateUseCase,
    DeletePredicateUseCase,
    UpdatePredicateUseCase,
    RetrievePredicateUseCase

interface RetrievePredicateUseCase {
    fun existsById(id: ThingId): Boolean

    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
    ): Page<Predicate>

    fun findById(id: ThingId): Optional<Predicate>
}

interface CreatePredicateUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val id: ThingId? = null,
        val label: String,
        val contributorId: ContributorId? = null,
        val modifiable: Boolean = true,
    )
}

interface UpdatePredicateUseCase {
    fun update(id: ThingId, command: ReplaceCommand)

    data class ReplaceCommand(
        val label: String,
        val description: String? = null,
    )
}

interface DeletePredicateUseCase {
    // legacy methods:
    fun delete(predicateId: ThingId, contributorId: ContributorId)

    fun deleteAll()
}
