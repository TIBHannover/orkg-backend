package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.concurrent.atomic.AtomicLong

class InMemoryPredicateRepository : PredicateRepository {

    private val counter = AtomicLong(0)

    private val predicates: MutableSet<Predicate> = mutableSetOf()

    override fun nextIdentity(): PredicateId = PredicateId(counter.getAndIncrement())

    override fun save(predicate: Predicate) {
        predicates += predicate
    }

    override fun findAll(pageable: Pageable): Page<Predicate> {
        TODO("Not yet implemented")
    }

    override fun findById(id: PredicateId?): Optional<Predicate> =
        Optional.of(predicates.single { it.id == id })

    override fun findAllByLabelExactly(label: String, pageable: Pageable): Page<Predicate> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Predicate> {
        TODO("Not yet implemented")
    }
}
