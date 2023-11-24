package org.orkg.graph.adapter.output.inmemory

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.LiteralRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

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
