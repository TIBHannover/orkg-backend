package org.orkg.graph.input

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.SearchString
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface ClassUseCases :
    CreateClassUseCase,
    RetrieveClassUseCase,
    UpdateClassUseCase,
    DeleteClassUseCase,
    URIService<Class>

interface UnsafeClassUseCases :
    CreateClassUseCase,
    UpdateClassUseCase,
    DeleteClassUseCase

interface RetrieveClassUseCase {
    fun existsById(id: ThingId): Boolean

    // legacy methods:
    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
    ): Page<Class>

    fun findById(id: ThingId): Optional<Class>
}

interface CreateClassUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val id: ThingId? = null,
        val contributorId: ContributorId,
        val label: String,
        val uri: ParsedIRI? = null,
        val modifiable: Boolean = true,
    )
}

interface UpdateClassUseCase {
    fun update(command: UpdateCommand)

    fun replace(command: ReplaceCommand)

    data class UpdateCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val label: String? = null,
        val uri: ParsedIRI? = null,
        val modifiable: Boolean? = null,
    )

    data class ReplaceCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val label: String,
        val uri: ParsedIRI? = null,
        val modifiable: Boolean? = null,
    )
}

interface DeleteClassUseCase {
    // legacy methods:
    fun deleteAll()
}
