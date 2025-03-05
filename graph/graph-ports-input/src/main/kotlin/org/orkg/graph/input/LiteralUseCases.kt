package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.SearchString
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface LiteralUseCases :
    CreateLiteralUseCase,
    RetrieveLiteralUseCase,
    UpdateLiteralUseCase,
    DeleteLiteralUseCase

interface RetrieveLiteralUseCase {
    fun existsById(id: ThingId): Boolean

    fun findById(id: ThingId): Optional<Literal>

    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
    ): Page<Literal>

    fun findDOIByContributionId(id: ThingId): Optional<Literal>
}

interface CreateLiteralUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val id: ThingId? = null,
        val contributorId: ContributorId,
        val label: String,
        val datatype: String = Literals.XSD.STRING.prefixedUri,
        val modifiable: Boolean = true,
    )
}

interface UpdateLiteralUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val label: String? = null,
        val datatype: String? = null,
        val modifiable: Boolean? = null,
    )
}

interface DeleteLiteralUseCase {
    // legacy methods:
    fun deleteAll()
}
