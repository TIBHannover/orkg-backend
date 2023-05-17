package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import java.util.*
import org.springframework.data.domain.Pageable

class InMemoryPredicateRepository : InMemoryRepository<ThingId, Predicate>(
    compareBy(Predicate::createdAt)
), PredicateRepository {
    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label.matches(labelSearchString) }

    override fun findByPredicateId(id: ThingId) = Optional.ofNullable(entities[id])

    override fun deleteByPredicateId(id: ThingId) {
        entities.remove(id)
    }

    override fun deleteAll() {
        entities.clear()
    }

    override fun save(predicate: Predicate) {
        entities[predicate.id] = predicate
    }

    override fun nextIdentity(): ThingId {
        var count = entities.size.toLong()
        var id = ThingId("P$count")
        while(id in entities) {
            id = ThingId("P${++count}")
        }
        return id
    }
}
