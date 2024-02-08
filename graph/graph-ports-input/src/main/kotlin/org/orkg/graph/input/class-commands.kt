package org.orkg.graph.input

import dev.forkhandles.result4k.Result
import java.net.URI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId

interface CreateClassUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val label: String,
        val id: ThingId? = null,
        val contributorId: ContributorId? = null,
        val uri: URI? = null,
        val modifiable: Boolean = true
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

data object ClassNotModifiableProblem : ClassUpdateProblem, ClassLabelUpdateProblem, ClassURIUpdateProblem

interface DeleteClassUseCase {
    // legacy methods:
    fun removeAll()
}
