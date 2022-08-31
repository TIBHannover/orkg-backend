package eu.tib.orkg.prototype.statements.api

import dev.forkhandles.result4k.Result
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreateClassRequest
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import java.net.URI

interface CreateClassUseCase {
    // legacy methods:
    fun create(label: String): ClassRepresentation
    fun create(userId: ContributorId, label: String): ClassRepresentation
    fun create(request: CreateClassRequest): ClassRepresentation
    fun create(userId: ContributorId, request: CreateClassRequest): ClassRepresentation
    fun createIfNotExists(id: ClassId, label: String, uri: URI?)
}

interface UpdateClassUseCase {
    fun replace(id: ClassId, command: ReplaceCommand): Result<Unit, ClassUpdateProblem>
    fun updateLabel(id: ClassId, newLabel: String): Result<Unit, ClassLabelUpdateProblem>
    fun updateURI(id: ClassId, with: String): Result<Unit, ClassURIUpdateProblem>

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
