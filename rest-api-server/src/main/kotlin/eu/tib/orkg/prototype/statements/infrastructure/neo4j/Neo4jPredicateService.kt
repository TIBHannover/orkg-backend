package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreatePredicateRequest
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.ports.PredicateRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.Optional

@Service
@Transactional
class Neo4jPredicateService(
    private val predicateRepository: PredicateRepository
) : PredicateService {

    override fun create(label: String) = create(ContributorId.createUnknownContributor(), label)

    override fun create(
        userId: ContributorId,
        label: String
    ): Predicate {
        val id = predicateRepository.nextIdentity()
        val predicate = Predicate(
            id = id,
            label = label,
            createdBy = userId,
            createdAt = OffsetDateTime.now()
        )
        predicateRepository.save(predicate)
        return predicate
    }

    override fun create(request: CreatePredicateRequest) = create(ContributorId.createUnknownContributor(), request)

    override fun create(userId: ContributorId, request: CreatePredicateRequest): Predicate {
        var id = request.id ?: predicateRepository.nextIdentity()

        // Should be moved to the Generator in the future
        while (predicateRepository.findById(id).isPresent) {
            id = predicateRepository.nextIdentity()
        }
        val predicate = Predicate(
            id = id,
            label = request.label,
            createdBy = userId,
            createdAt = OffsetDateTime.now()
        )
        predicateRepository.save(predicate)
        return predicate
    }

    override fun findAll(pageable: Pageable): Page<Predicate> = predicateRepository.findAll(pageable)

    override fun findById(id: PredicateId?): Optional<Predicate> = predicateRepository.findById(id)

    override fun findAllByLabel(label: String, pageable: Pageable): Page<Predicate> =
        predicateRepository.findAllByLabelExactly(label, pageable)

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Predicate> =
        predicateRepository.findAllByLabelContaining(part, pageable)

    override fun update(predicate: Predicate): Predicate {
        // already checked by service
        var found = predicateRepository.findById(predicate.id).get()

        // update all the properties
        found = found.copy(label = predicate.label)

        predicateRepository.save(found)
        return found
    }

    override fun createIfNotExists(id: PredicateId, label: String) {
        val found = predicateRepository.findById(id)

        if (found.isEmpty) {
            predicateRepository.save(
                Predicate(
                    label = label,
                    id = id,
                    createdBy = ContributorId.createUnknownContributor(),
                    createdAt = OffsetDateTime.now()
                )
            )
        }
    }

    override fun removeAll() = Unit
}
