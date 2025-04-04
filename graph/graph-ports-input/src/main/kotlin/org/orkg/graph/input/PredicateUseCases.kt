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

interface UnsafePredicateUseCases :
    CreatePredicateUseCase,
    UpdatePredicateUseCase,
    DeletePredicateUseCase

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
        val contributorId: ContributorId,
        val label: String,
        val modifiable: Boolean = true,
    )
}

interface UpdatePredicateUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val label: String? = null,
        val modifiable: Boolean? = null,
    )
}

interface DeletePredicateUseCase {
    // legacy methods:
    fun delete(id: ThingId, contributorId: ContributorId)

    fun deleteAll()
}
