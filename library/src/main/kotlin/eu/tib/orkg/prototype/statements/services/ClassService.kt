package eu.tib.orkg.prototype.statements.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.values.ofOrNull
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.AlreadyInUse
import eu.tib.orkg.prototype.statements.api.ClassLabelUpdateProblem
import eu.tib.orkg.prototype.statements.api.ClassNotFound
import eu.tib.orkg.prototype.statements.api.ClassURIUpdateProblem
import eu.tib.orkg.prototype.statements.api.ClassUpdateProblem
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.CreateClassUseCase
import eu.tib.orkg.prototype.statements.api.InvalidLabel
import eu.tib.orkg.prototype.statements.api.InvalidURI
import eu.tib.orkg.prototype.statements.api.UpdateClassUseCase
import eu.tib.orkg.prototype.statements.api.UpdateNotAllowed
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.Clock
import eu.tib.orkg.prototype.statements.domain.model.Label
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.SystemClock
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import java.net.URI
import java.net.URISyntaxException
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ClassService(
    private val repository: ClassRepository,
    private val clock: Clock = SystemClock(),
) : ClassUseCases {
    override fun create(command: CreateClassUseCase.CreateCommand): ThingId {
        val id = if (command.id != null) ThingId(command.id) else repository.nextIdentity()
        val newClass = Class(
            id = id,
            label = Label.ofOrNull(command.label)?.value
                ?: throw IllegalArgumentException("Invalid label: ${command.label}"),
            uri = command.uri,
            createdAt = clock.now(),
            createdBy = command.contributorId ?: ContributorId.createUnknownContributor(),
        )
        repository.save(newClass)
        return newClass.id
    }

    override fun create(label: String): Class = create(ContributorId.createUnknownContributor(), label)

    override fun create(userId: ContributorId, label: String): Class {
        val newClassId = create(
            CreateClassUseCase.CreateCommand(
                label = label,
                contributorId = userId,
            )
        )
        return repository.findById(newClassId).get()
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
        if (found.label != label.value) repository.save(found.copy(label = label.value))
        return Success(Unit)
    }

    override fun updateURI(id: ThingId, with: String): Result<Unit, ClassURIUpdateProblem> {
        val uri = with.toURIOrNull() ?: return Failure(InvalidURI)
        val found = repository.findById(id).orElse(null) ?: return Failure(ClassNotFound)
        if (found.uri != null) return Failure(UpdateNotAllowed)
        val possiblyUsed = findByURI(uri).orElse(null)
        if (possiblyUsed != null && possiblyUsed.id != found.id) return Failure(AlreadyInUse)
        repository.save(found.copy(uri = uri))
        return Success(Unit)
    }

    override fun removeAll() = repository.deleteAll()

    override fun findByURI(uri: URI): Optional<Class> =
        repository.findByUri(uri.toString())

    override fun createIfNotExists(id: ThingId, label: String, uri: URI?) {
        // Checking if URI is null
        if (uri == null) {
            // check only for ID
            val found = repository.findById(id)
            if (found.isEmpty) {
                repository.save(
                    Class(
                        id = id,
                        label = label,
                        uri = uri,
                        createdAt = OffsetDateTime.now(),
                        createdBy = ContributorId.createUnknownContributor()
                    )
                )
            }
        } else {
            val oClassByURI = repository.findByUri(uri.toString())
            val oClassById = repository.findById(id)
            when {
                oClassById.isEmpty && oClassByURI.isEmpty -> repository.save(
                    Class(
                        id = id,
                        label = label,
                        uri = uri,
                        createdAt = OffsetDateTime.now(),
                        createdBy = ContributorId.createUnknownContributor()
                    )
                )
                // Throwing an exception if IDs are different
                oClassById.isPresent && oClassByURI.isPresent && oClassById.get().id != oClassByURI.get().id -> throw Exception(
                    "ID mismatch for class ID: ${oClassById.get().id}"
                )
            }
        }
    }

    private fun String.toURIOrNull(): URI? = try {
        URI(this)
    } catch (_: URISyntaxException) {
        null
    }
}
