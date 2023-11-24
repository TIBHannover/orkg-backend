package org.orkg.graph.input

import dev.forkhandles.result4k.Result
import java.net.URI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class

interface CreateClassUseCase {
    fun create(command: CreateClassUseCase.CreateCommand): ThingId

    // legacy methods:
    fun create(label: String): Class
    fun create(userId: ContributorId, label: String): Class
    fun createIfNotExists(id: ThingId, label: String, uri: URI?)

    data class CreateCommand(
        val label: String,
        val id: String? = null,
        val contributorId: ContributorId? = null,
        val uri: URI? = null,
    )
}

interface UpdateClassUseCase {
    fun replace(id: ThingId, command: UpdateClassUseCase.ReplaceCommand): Result<Unit, ClassUpdateProblem>
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
