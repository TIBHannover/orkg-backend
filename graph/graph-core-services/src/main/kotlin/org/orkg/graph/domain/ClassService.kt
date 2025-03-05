package org.orkg.graph.domain

import dev.forkhandles.values.ofOrNull
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.UpdateClassUseCase
import org.orkg.graph.output.ClassRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.Optional

@Service
@TransactionalOnNeo4j
class ClassService(
    private val repository: ClassRepository,
    private val clock: Clock,
) : ClassUseCases {
    override fun create(command: CreateClassUseCase.CreateCommand): ThingId {
        val label = Label.ofOrNull(command.label)?.value ?: throw InvalidLabel()
        command.uri?.let { uri ->
            if (!uri.isAbsolute) {
                throw URINotAbsolute(uri)
            }
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
            createdBy = command.contributorId,
            modifiable = command.modifiable
        )
        repository.save(newClass)
        return newClass.id
    }

    @TransactionalOnNeo4j(readOnly = true)
    override fun existsById(id: ThingId): Boolean = repository.existsById(id)

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
    ): Page<Class> =
        repository.findAll(pageable, label, createdBy, createdAtStart, createdAtEnd)

    @Deprecated("For removal")
    override fun findAllById(ids: Iterable<ThingId>, pageable: Pageable): Page<Class> =
        repository.findAllById(ids, pageable)

    override fun findById(id: ThingId): Optional<Class> =
        repository.findById(id)

    override fun update(command: UpdateClassUseCase.UpdateCommand) {
        if (command.hasNoContents()) return
        command.label?.also { Label.ofOrNull(it) ?: throw InvalidLabel() }
        val `class` = repository.findById(command.id)
            .orElseThrow { ClassNotFound.withThingId(command.id) }
        if (!`class`.modifiable) {
            throw ClassNotModifiable(command.id)
        }
        command.uri?.also { newUri ->
            if (`class`.uri != null && command.uri != `class`.uri) {
                throw CannotResetURI(command.id)
            }
            findByURI(newUri).ifPresent {
                if (it.id != `class`.id) {
                    throw URIAlreadyInUse(newUri, it.id)
                }
            }
        }
        val updated = `class`.apply(command)
        if (updated != `class`) {
            repository.save(updated)
        }
    }

    override fun replace(command: UpdateClassUseCase.ReplaceCommand) {
        Label.ofOrNull(command.label) ?: throw InvalidLabel()
        val `class` = repository.findById(command.id)
            .orElseThrow { ClassNotFound.withThingId(command.id) }
        if (!`class`.modifiable) {
            throw ClassNotModifiable(command.id)
        }
        if (`class`.uri != null && command.uri != `class`.uri) {
            throw CannotResetURI(command.id)
        }
        command.uri?.also { newUri ->
            findByURI(newUri).ifPresent {
                if (it.id != `class`.id) {
                    throw URIAlreadyInUse(newUri, it.id)
                }
            }
        }
        val updated = `class`.apply(command)
        if (updated != `class`) {
            repository.save(updated)
        }
    }

    override fun deleteAll() = repository.deleteAll()

    override fun findByURI(uri: ParsedIRI): Optional<Class> =
        repository.findByUri(uri.toString())
}
