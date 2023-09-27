package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class InMemoryLiteralRepository : InMemoryRepository<ThingId, Literal>(
    compareBy(Literal::createdAt)
), LiteralRepository {
    override fun nextIdentity(): ThingId {
        var count = entities.size.toLong()
        var id = ThingId("L$count")
        while(id in entities) {
            id = ThingId("L${++count}")
        }
        return id
    }

    override fun save(literal: Literal) {
        entities[literal.id] = literal
    }

    override fun deleteAll() {
        entities.clear()
    }

    override fun findById(id: ThingId) = Optional.ofNullable(entities[id])

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable) =
        entities.values
            .filter { it.label.matches(labelSearchString) }
            .sortedWith(compareBy { it.label.length })
            .paged(pageable)

    override fun findAllWithFilters(
        createdBy: ContributorId?,
        createdAt: OffsetDateTime?,
        pageable: Pageable
    ): Page<Literal> = findAllFilteredAndPaged(pageable, pageable.sort.literalComparator) {
        (createdBy == null || createdBy == it.createdBy)
            && (createdAt == null || createdAt == it.createdAt)
    }
}
