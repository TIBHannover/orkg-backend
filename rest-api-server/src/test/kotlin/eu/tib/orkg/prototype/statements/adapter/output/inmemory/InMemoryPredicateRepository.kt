package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryPredicateRepository : PredicateRepository {
    override fun findAll(): Sequence<Predicate> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<Predicate> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabel(label: String, pageable: Pageable): Page<Predicate> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Predicate> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Predicate> {
        TODO("Not yet implemented")
    }

    override fun findByPredicateId(id: PredicateId?): Optional<Predicate> {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun save(predicate: Predicate) {
        TODO("Not yet implemented")
    }

    override fun nextIdentity(): PredicateId {
        TODO("Not yet implemented")
    }
}
