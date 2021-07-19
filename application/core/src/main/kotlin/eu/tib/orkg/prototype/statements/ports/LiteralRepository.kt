package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional

interface LiteralRepository {
    /**
     * Find all literals.
     */
    fun findAll(): Iterable<Literal>

    /**
     * Find a literal by its ID.
     */
    fun findById(id: LiteralId?): Optional<Literal>

    /**
     * Find all literals matching a label.
     */
    fun findAllByLabel(label: String): Iterable<Literal>

    /**
     * Find all literals matching a label partially.
     */
    fun findAllByLabelContaining(part: String): Iterable<Literal>

    fun findDOIByContributionId(id: ResourceId): Optional<Literal>
}
