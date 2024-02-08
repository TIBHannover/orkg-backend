package org.orkg.graph.domain

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.values.ofOrNull
import java.net.URI
import java.net.URISyntaxException
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.input.AlreadyInUse
import org.orkg.graph.input.ClassLabelUpdateProblem
import org.orkg.graph.input.ClassNotFound
import org.orkg.graph.input.ClassNotModifiableProblem
import org.orkg.graph.input.ClassURIUpdateProblem
import org.orkg.graph.input.ClassUpdateProblem
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.InvalidLabel
import org.orkg.graph.input.InvalidURI
import org.orkg.graph.input.UpdateClassUseCase
import org.orkg.graph.input.UpdateNotAllowed
import org.orkg.graph.output.ClassRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ClassService(
    private val repository: ClassRepository,
    private val clock: Clock,
) : ClassUseCases {
    override fun create(command: CreateClassUseCase.CreateCommand): ThingId {
        val label = Label.ofOrNull(command.label)?.value ?: throw InvalidLabel()
        command.uri?.let { uri ->
            repository.findByUri(uri.toString()).ifPresent {
                throw DuplicateURI(uri, it.id)
            }
        }
        val id = command.id?.also { id ->
            if (id in reservedClassIds) {
                throw ClassNotAllowed(id)
            }
            repository.findById(id).ifPresent {
                throw ClassAlreadyExists(id)
            }
        } ?: repository.nextIdentity()
        val newClass = Class(
            id = id,
            label = label,
            uri = command.uri,
            createdAt = OffsetDateTime.now(clock),
            createdBy = command.contributorId ?: ContributorId.UNKNOWN,
            modifiable = command.modifiable
        )
        repository.save(newClass)
        return newClass.id
    }

    @Transactional(readOnly = true)
    override fun exists(id: ThingId): Boolean = repository.exists(id)

    override fun findAll(pageable: Pageable): Page<Class> =
        repository.findAll(pageable)

    override fun findAllById(ids: Iterable<ThingId>, pageable: Pageable): Page<Class> =
        repository.findAllById(ids, pageable)

    override fun findById(id: ThingId): Optional<Class> =
        repository.findById(id)

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Class> =
        repository.findAllByLabel(labelSearchString, pageable)

    override fun replace(id: ThingId, command: UpdateClassUseCase.ReplaceCommand): Result<Unit, ClassUpdateProblem> {
        val label = Label.ofOrNull(command.label) ?: return Failure(InvalidLabel)
        val found = repository.findById(id).orElse(null) ?: return Failure(ClassNotFound)
        if (!found.modifiable) return Failure(ClassNotModifiableProblem)
        if (found.uri != command.uri && found.uri != null) return Failure(UpdateNotAllowed)
        command.uri?.let {
            val possiblyUsed = findByURI(it).orElse(null)
            if (possiblyUsed != null && possiblyUsed.id != found.id) return Failure(AlreadyInUse)
        }
        repository.save(found.copy(id = id, label = label.value, uri = command.uri))
        return Success(Unit)
    }

    override fun updateLabel(id: ThingId, newLabel: String): Result<Unit, ClassLabelUpdateProblem> {
        val label = Label.ofOrNull(newLabel) ?: return Failure(InvalidLabel)
        val found = repository.findById(id).orElse(null) ?: return Failure(ClassNotFound)
        if (!found.modifiable) return Failure(ClassNotModifiableProblem)
        if (found.label != label.value) repository.save(found.copy(label = label.value))
        return Success(Unit)
    }

    override fun updateURI(id: ThingId, with: String): Result<Unit, ClassURIUpdateProblem> {
        val uri = with.toURIOrNull() ?: return Failure(InvalidURI)
        val found = repository.findById(id).orElse(null) ?: return Failure(ClassNotFound)
        if (!found.modifiable) return Failure(ClassNotModifiableProblem)
        if (found.uri != null) return Failure(UpdateNotAllowed)
        val possiblyUsed = findByURI(uri).orElse(null)
        if (possiblyUsed != null && possiblyUsed.id != found.id) return Failure(AlreadyInUse)
        repository.save(found.copy(uri = uri))
        return Success(Unit)
    }

    override fun removeAll() = repository.deleteAll()

    override fun findByURI(uri: URI): Optional<Class> =
        repository.findByUri(uri.toString())

    private fun String.toURIOrNull(): URI? = try {
        URI(this)
    } catch (_: URISyntaxException) {
        null
    }
}
