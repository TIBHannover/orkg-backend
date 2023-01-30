package eu.tib.orkg.prototype.statements.api

import dev.forkhandles.result4k.Result
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreateClassRequest
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.net.URI

interface CreateClassUseCase {
    fun create(command: CreateCommand): ThingId

    // legacy methods:
    fun create(label: String): ClassRepresentation
    fun create(userId: ContributorId, label: String): ClassRepresentation
    fun create(request: CreateClassRequest): ClassRepresentation
    fun create(userId: ContributorId, request: CreateClassRequest): ClassRepresentation
    fun createIfNotExists(id: ThingId, label: String, uri: URI?)

    data class CreateCommand(
        val label: String,
        val id: String? = null,
        val contributorId: ContributorId? = null,
        val uri: URI? = null,
    )
}

interface UpdateClassUseCase {
    fun replace(id: ThingId, command: ReplaceCommand): Result<Unit, ClassUpdateProblem>
    fun updateLabel(id: ThingId, newLabel: String): Result<Unit, ClassLabelUpdateProblem>
    fun updateURI(id: ThingId, with: String): Result<Unit, ClassURIUpdateProblem>

    data class ReplaceCommand(
        val label: String,
        val uri: URI?,
    )
}

sealed interface ClassUpdateProblem

sealed interface ClassLabelUpdateProblem : ClassUpdateProblem

sealed interface ClassURIUpdateProblem : ClassUpdateProblem

object ClassNotFound : ClassLabelUpdateProblem, ClassURIUpdateProblem

object InvalidLabel : ClassLabelUpdateProblem

object InvalidURI : ClassURIUpdateProblem

object UpdateNotAllowed : ClassURIUpdateProblem

object AlreadyInUse : ClassURIUpdateProblem

interface DeleteClassUseCase {
    // legacy methods:
    fun removeAll()
}
