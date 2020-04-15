package eu.tib.orkg.prototype.statements.domain.model

import java.util.Optional
import java.util.UUID

interface LiteralService {
    /**
     * Create a new literal with a given label and datatype.
     *
     * @param label The label containing the value.
     * @param datatype The datatype of the value.
     *
     * @return The newly created literal.
     */
    fun create(label: String, datatype: String = "xsd:string"): Literal

    /**
     * Create a new literal with a given label belonging to a given user.
     *
     * @return the newly created literal
     */
    fun create(userId: UUID, label: String, datatype: String = "xsd:string"): Literal

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
