package eu.tib.orkg.prototype.statements.api

import dev.forkhandles.result4k.Result
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreateClassRequest
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import java.net.URI

interface CreateClassUseCase {
    // legacy methods:
    fun create(label: String): Class
    fun create(userId: ContributorId, label: String): Class
    fun create(request: CreateClassRequest): Class
    fun create(userId: ContributorId, request: CreateClassRequest): Class
    fun createIfNotExists(id: ClassId, label: String, uri: URI?)
}

interface UpdateClassUseCase {
    fun replace(id: ClassId, with: Class): Result<Unit, ClassUpdateProblem>
    fun updateLabel(id: ClassId, newLabel: String): Result<Unit, ClassLabelUpdateProblem>
    fun updateURI(id: ClassId, with: String): Result<Unit, ClassURIUpdateProblem>
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
