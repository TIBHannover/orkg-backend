package eu.tib.orkg.prototype.statements.domain.model

import java.util.*

interface LiteralService {
    /**
     * Create a new literal with a given label.
     *
     * @return the newly created literal
     */
    fun create(label: String): Literal

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

    /**
     * Update a literal.
     */
    fun update(literal: Literal): Literal
}
