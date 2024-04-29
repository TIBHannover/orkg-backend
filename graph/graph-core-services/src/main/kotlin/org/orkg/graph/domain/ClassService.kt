package org.orkg.graph.domain

import dev.forkhandles.values.ofOrNull
import java.net.URI
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.UpdateClassUseCase
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
                throw URIAlreadyInUse(uri, it.id)
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

    override fun update(command: UpdateClassUseCase.UpdateCommand) {
        if (command.label == null && command.uri == null) {
            return
        }
        command.label?.also { Label.ofOrNull(it) ?: throw InvalidLabel() }
        val found = repository.findById(command.id).orElseThrow { ClassNotFound.withThingId(command.id) }
        if (!found.modifiable) throw ClassNotModifiable(command.id)
        command.uri?.also { newUri ->
            if (found.uri != null && command.uri != found.uri)
                throw CannotResetURI(command.id)
            findByURI(newUri).ifPresent {
                if (it.id != found.id) throw URIAlreadyInUse(newUri, it.id)
            }
        }
        val label = command.label ?: found.label
        val uri = command.uri ?: found.uri
        if (found.label != label || found.uri != uri) {
            repository.save(found.copy(label = label, uri = uri))
        }
    }

    override fun replace(command: UpdateClassUseCase.ReplaceCommand) {
        val label = Label.ofOrNull(command.label)?.value ?: throw InvalidLabel()
        val found = repository.findById(command.id).orElseThrow { ClassNotFound.withThingId(command.id) }
        if (!found.modifiable) throw ClassNotModifiable(command.id)
        if (found.uri != null && command.uri != found.uri)
            throw CannotResetURI(command.id)
        command.uri?.also { newUri ->
            findByURI(newUri).ifPresent {
                if (it.id != found.id) throw URIAlreadyInUse(newUri, it.id)
            }
        }
        if (label != found.label || found.uri != command.uri) {
            repository.save(found.copy(label = label, uri = command.uri))
        }
    }

    override fun removeAll() = repository.deleteAll()

    override fun findByURI(uri: URI): Optional<Class> =
        repository.findByUri(uri.toString())
}
