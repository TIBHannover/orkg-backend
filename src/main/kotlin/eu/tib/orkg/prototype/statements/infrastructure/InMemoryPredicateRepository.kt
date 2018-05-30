package eu.tib.orkg.prototype.statements.infrastructure

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class InMemoryPredicateRepository : PredicateRepository {
    private val predicates = mutableMapOf<PredicateId, Predicate>()
    private var counter = 0L

    override fun findById(id: PredicateId): Optional<Predicate> {
        val predicate = predicates[id]
        if (predicate != null) {
            return Optional.of(predicate)
        }
        return Optional.empty()
    }

    override fun findAll(): Iterable<Predicate> =
        predicates.values.toSet()

    override fun findByLabel(searchString: String): Iterable<Predicate> =
        predicates.filter {
            it.value.label.contains(searchString, true)
        }.values.toSet()

    override fun add(predicate: Predicate) {
        if (predicate.id != null)
            predicates[predicate.id] = predicate
    }

    override fun nextIdentity(): PredicateId {
        counter++
        return PredicateId("P$counter")
    }
}
