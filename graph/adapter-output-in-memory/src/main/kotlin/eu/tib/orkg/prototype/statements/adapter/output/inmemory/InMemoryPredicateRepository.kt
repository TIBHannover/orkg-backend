package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryPredicateRepository : InMemoryRepository<PredicateId, Predicate>(
    compareBy(Predicate::createdAt)
), PredicateRepository {
    override fun findAllByLabel(label: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label == label }

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Predicate> {
        val regex = Regex(label)
        return findAllFilteredAndPaged(pageable) { it.label.matches(regex) }
    }

    override fun findAllByLabelContaining(part: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label.contains(part) }

    override fun findByPredicateId(id: PredicateId?) = Optional.ofNullable(entities[id!!])

    override fun deleteByPredicateId(id: PredicateId) {
        entities.remove(id)
    }

    override fun deleteAll() {
        entities.clear()
    }

    override fun save(predicate: Predicate) {
        entities[predicate.id!!] = predicate
    }

    override fun nextIdentity(): PredicateId {
        var id = PredicateId(entities.size.toLong())
        while(id in entities) {
            id = PredicateId(id.value.toLong() + 1)
        }
        return id
    }

    override fun usageCount(id: PredicateId): Long {
        TODO("This method should be moved to the StatementRepository")
    }
}
