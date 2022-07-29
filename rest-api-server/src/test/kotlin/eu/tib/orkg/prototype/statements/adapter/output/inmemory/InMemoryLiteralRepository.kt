package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import java.util.*

class InMemoryLiteralRepository : LiteralRepository {
    override fun nextIdentity(): LiteralId {
        TODO("Not yet implemented")
    }

    override fun save(literal: Literal) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun findAll(): Iterable<Literal> {
        TODO("Not yet implemented")
    }

    override fun findByLiteralId(id: LiteralId?): Optional<Literal> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabel(value: String): Iterable<Literal> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelMatchesRegex(label: String): Iterable<Literal> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelContaining(part: String): Iterable<Literal> {
        TODO("Not yet implemented")
    }

    override fun findDOIByContributionId(id: ResourceId): Optional<Literal> {
        TODO("Not yet implemented")
    }
}
