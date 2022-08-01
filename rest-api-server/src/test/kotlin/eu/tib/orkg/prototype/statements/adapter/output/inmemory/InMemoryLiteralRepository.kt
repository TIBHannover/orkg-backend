package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.services.toExactSearchString
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class InMemoryLiteralRepository : LiteralRepository {

    private val idCounter = AtomicLong(1)

    private val entities = mutableMapOf<LiteralId, Literal>()

    override fun nextIdentity(): LiteralId = LiteralId(idCounter.getAndIncrement())

    override fun save(literal: Literal) {
        entities[literal.id!!] = literal.copy()
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun findAll(): Iterable<Literal> {
        TODO("Not yet implemented")
    }

    override fun findByLiteralId(id: LiteralId?): Optional<Literal> = Optional.ofNullable(entities[id])

    override fun findAllByLabel(value: String): Iterable<Literal> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelMatchesRegex(label: String): Iterable<Literal> =
        entities
            .filter { (id, entry) -> entry.label.matches(label.toExactSearchString().toRegex()) }
            .map { (id, entry) -> entry }

    override fun findAllByLabelContaining(part: String): Iterable<Literal> {
        TODO("Not yet implemented")
    }

    override fun findDOIByContributionId(id: ResourceId): Optional<Literal> {
        TODO("Not yet implemented")
    }
}
