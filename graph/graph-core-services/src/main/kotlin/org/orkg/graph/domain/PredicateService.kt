package org.orkg.graph.domain

import dev.forkhandles.values.ofOrNull
import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UpdatePredicateUseCase
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.Optional

@Service
@TransactionalOnNeo4j
class PredicateService(
    private val repository: PredicateRepository,
    private val thingRepository: ThingRepository,
    private val contributorRepository: ContributorRepository,
    private val unsafePredicateUseCases: UnsafePredicateUseCases,
) : PredicateUseCases {
    @TransactionalOnNeo4j(readOnly = true)
    override fun existsById(id: ThingId): Boolean = repository.existsById(id)

    override fun create(command: CreatePredicateUseCase.CreateCommand): ThingId {
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
        command.id?.also { id -> thingRepository.findById(id).ifPresent { throw ThingAlreadyExists(id) } }
        return unsafePredicateUseCases.create(command)
    }

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        uri: IRI?,
    ): Page<Predicate> =
        repository.findAll(pageable, label, createdBy, createdAtStart, createdAtEnd, uri)

    override fun findById(id: ThingId): Optional<Predicate> =
        repository.findById(id)

    override fun update(command: UpdatePredicateUseCase.UpdateCommand) {
        if (command.hasNoContents()) return
        val predicate = repository.findById(command.id)
            .orElseThrow { PredicateNotFound(command.id) }
        if (!predicate.modifiable) {
            throw PredicateNotModifiable(predicate.id)
        }
        if (command.extractionMethod != null && !predicate.extractionMethod.canBeChangedTo(command.extractionMethod!!)) {
            throw InvalidExtractionMethodChange(predicate.extractionMethod, command.extractionMethod!!)
        }
        command.label?.also { Label.ofOrNull(it) ?: throw InvalidLabel() }
        command.uri?.also { newUri ->
            if (newUri == predicate.uri) {
                return@also
            }
            if (predicate.uri != null) {
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
        val updated = predicate.apply(command)
        if (updated != predicate) {
            repository.save(updated)
        }
    }

    override fun delete(id: ThingId, contributorId: ContributorId) {
        val predicate = findById(id).orElseThrow { PredicateNotFound(id) }
        if (!predicate.modifiable) {
            throw PredicateNotModifiable(predicate.id)
        }

        if (repository.isInUse(predicate.id)) {
            throw PredicateInUse(predicate.id)
        }

        if (!predicate.isOwnedBy(contributorId)) {
            val contributor = contributorRepository.findById(contributorId)
                .orElseThrow { ContributorNotFound(contributorId) }
            if (!contributor.isCurator) throw NeitherOwnerNorCurator(predicate.createdBy, contributorId, predicate.id)
        }

        unsafePredicateUseCases.delete(predicate.id, contributorId)
    }

    override fun deleteAll() = repository.deleteAll()
}
