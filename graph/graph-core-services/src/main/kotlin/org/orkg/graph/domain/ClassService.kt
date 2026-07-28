package org.orkg.graph.domain

import dev.forkhandles.values.ofOrNull
import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UpdateClassUseCase
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.Optional

@Service
@TransactionalOnNeo4j
class ClassService(
    private val repository: ClassRepository,
    private val thingRepository: ThingRepository,
    private val unsafeClassUseCases: UnsafeClassUseCases,
) : ClassUseCases {
    override fun create(command: CreateClassUseCase.CreateCommand): ThingId {
        Label.ofOrNull(command.label) ?: throw InvalidLabel()
        command.uri?.also { uri ->
            if (!uri.isAbsolute) {
                throw URINotAbsolute(uri)
            }
            val things = thingRepository.findAll(PageRequests.SINGLE, uri = uri)
            if (!things.isEmpty) {
                throw URIAlreadyInUse(uri, things.single().id)
            }
        }
        command.id?.also { id ->
            if (id in reservedClassIds) {
                throw ReservedClassId(id)
            }
            thingRepository.findById(id).ifPresent {
                throw ThingAlreadyExists(id)
            }
        }
        return unsafeClassUseCases.create(command)
    }

    @TransactionalOnNeo4j(readOnly = true)
    override fun existsById(id: ThingId): Boolean = repository.existsById(id)

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        uri: IRI?,
    ): Page<Class> =
        repository.findAll(pageable, label, createdBy, createdAtStart, createdAtEnd, uri)

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
        if (command.extractionMethod != null && !`class`.extractionMethod.canBeChangedTo(command.extractionMethod!!)) {
            throw InvalidExtractionMethodChange(`class`.extractionMethod, command.extractionMethod!!)
        }
        command.uri?.also { newUri ->
            if (newUri == `class`.uri) {
                return@also
            }
            if (`class`.uri != null) {
                throw CannotResetURI(command.id)
            }
            if (!newUri.isAbsolute) {
                throw URINotAbsolute(newUri)
            }
            val things = thingRepository.findAll(PageRequests.SINGLE, uri = newUri)
            if (!things.isEmpty) {
                throw URIAlreadyInUse(newUri, things.single().id)
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
        if (command.extractionMethod != null && !`class`.extractionMethod.canBeChangedTo(command.extractionMethod!!)) {
            throw InvalidExtractionMethodChange(`class`.extractionMethod, command.extractionMethod!!)
        }
        command.uri?.also { newUri ->
            if (newUri == `class`.uri) {
                return@also
            }
            if (!newUri.isAbsolute) {
                throw URINotAbsolute(newUri)
            }
            val things = thingRepository.findAll(PageRequests.SINGLE, uri = newUri)
            if (!things.isEmpty) {
                throw URIAlreadyInUse(newUri, things.single().id)
            }
        }
        val updated = `class`.apply(command)
        if (updated != `class`) {
            repository.save(updated)
        }
    }

    override fun deleteAll() = repository.deleteAll()

    override fun findByURI(uri: IRI): Optional<Class> =
        repository.findByUri(uri.toString())
}
