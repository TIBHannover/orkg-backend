package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryLiteralRepository : InMemoryRepository<LiteralId, Literal>(
    compareBy(Literal::createdAt)
), LiteralRepository {
    override fun nextIdentity(): LiteralId {
        var id = LiteralId(entities.size.toLong())
        while(id in entities) {
            id = LiteralId(id.value.toLong() + 1)
        }
        return id
    }

    override fun save(literal: Literal) {
        entities[literal.id!!] = literal
    }

    override fun deleteAll() {
        entities.clear()
    }

    override fun findByLiteralId(id: LiteralId?) = Optional.ofNullable(entities[id!!])

    override fun findAllByLabel(value: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label == value }

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Literal> {
        val regex = Regex(label)
        return findAllFilteredAndPaged(pageable) { it.label.matches(regex) }
    }

    override fun findAllByLabelContaining(part: String, pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { it.label.contains(part) }
}