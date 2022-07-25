package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.PredicateRepresentation
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.UpdatePredicateUseCase
import eu.tib.orkg.prototype.statements.application.CreatePredicateRequest
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PredicateService(
    private val repository: PredicateRepository
) : PredicateUseCases {

    override fun create(label: String): PredicateRepresentation =
        create(ContributorId.createUnknownContributor(), label)

    override fun create(userId: ContributorId, label: String): PredicateRepresentation {
        val id = repository.nextIdentity()
        val predicate = Predicate(id, label, OffsetDateTime.now(), userId)
        repository.save(predicate)
        return repository.findByPredicateId(predicate.id).map(Predicate::toPredicateRepresentation).get()
    }

    override fun create(request: CreatePredicateRequest): PredicateRepresentation =
        create(ContributorId.createUnknownContributor(), request)

    override fun create(userId: ContributorId, request: CreatePredicateRequest): PredicateRepresentation {
        var id = request.id ?: repository.nextIdentity()

        // Should be moved to the Generator in the future
        while (repository.findByPredicateId(id).isPresent) {
            id = repository.nextIdentity()
        }

        val predicate = Predicate(label = request.label, id = id, createdBy = userId, createdAt = OffsetDateTime.now())
        repository.save(predicate)
        return predicate.toPredicateRepresentation()
    }

    override fun findAll(pageable: Pageable): Page<PredicateRepresentation> =
        repository.findAll(pageable).map(Predicate::toPredicateRepresentation)

    override fun findById(id: PredicateId?): Optional<PredicateRepresentation> =
        repository.findByPredicateId(id).map(Predicate::toPredicateRepresentation)

    override fun findAllByLabel(label: String, pageable: Pageable): Page<PredicateRepresentation> =
        repository.findAllByLabelMatchesRegex(label.toExactSearchString(), pageable)
            .map(Predicate::toPredicateRepresentation) // TODO: See declaration

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<PredicateRepresentation> =
        repository.findAllByLabelMatchesRegex(part.toSearchString(), pageable)
            .map(Predicate::toPredicateRepresentation) // TODO: See declaration

    override fun update(id: PredicateId, command: UpdatePredicateUseCase.ReplaceCommand) {
        var found = repository.findByPredicateId(id).get()

        // update all the properties
        found = found.copy(label = command.label)

        repository.save(found)
    }

    override fun createIfNotExists(id: PredicateId, label: String) {
        val oPredicate = repository.findByPredicateId(id)

        if (oPredicate.isEmpty) {
            val p = Predicate(
                label = label,
                id = id,
                createdBy = ContributorId.createUnknownContributor(),
                createdAt = OffsetDateTime.now()
            )
            repository.save(p)
        }
    }

    override fun removeAll() = repository.deleteAll()

    private fun String.toSearchString() =
        "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() =
        "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"
}

fun Predicate.toPredicateRepresentation(): PredicateRepresentation = object : PredicateRepresentation {
    override val id: PredicateId = this@toPredicateRepresentation.id!!
    override val label: String = this@toPredicateRepresentation.label
    override val description: String? = this@toPredicateRepresentation.description
    override val jsonClass: String = "predicate"
    override val createdAt: OffsetDateTime = this@toPredicateRepresentation.createdAt
    override val createdBy: ContributorId = this@toPredicateRepresentation.createdBy
}
