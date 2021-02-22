package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.util.Optional

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
    fun create(userId: ContributorId, label: String, datatype: String = "xsd:string"): Literal

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
    /**
     * Update a literal.
     */
    fun update(literal: Literal): Literal
}
