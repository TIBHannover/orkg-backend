package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional

class InMemoryLiteralRepository : LiteralRepository {

    private val literals: MutableSet<Literal> = mutableSetOf()

    override fun save(literal: Literal) {
        literals += literal
    }

    override fun findAll(): Iterable<Literal> = literals

    override fun findById(id: LiteralId?): Optional<Literal> =
        Optional.of(literals.single { it.id == id })

    override fun findAllByLabel(label: String): Iterable<Literal> {
        TODO("Not yet implemented")
    }

    override fun findAllByLabelContaining(part: String): Iterable<Literal> {
        TODO("Not yet implemented")
    }

    override fun findDOIByContributionId(id: ResourceId): Optional<Literal> {
        TODO("Not yet implemented")
    }
}
