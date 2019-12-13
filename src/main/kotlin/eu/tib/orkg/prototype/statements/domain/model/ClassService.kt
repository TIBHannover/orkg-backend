package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.CreateClassRequest
import java.util.Optional
import java.util.UUID

interface ClassService {
    /**
     * Create a new class with a given label.
     *
     * @return the newly created Class
     */
    fun create(label: String): Class

    /**
     * Create a new class with a given label belonging to a given user.
     *
     * @return the newly created Class
     */
    fun create(userId: UUID, label: String): Class

    /**
     * Create a new class from a request.
     */
    fun create(request: CreateClassRequest): Class

    /**
     * Create a new class from a request belonging to a given user.
     */
    fun create(userId: UUID, request: CreateClassRequest): Class

    /**
     * Find all resources.
     */
    fun findAll(): Iterable<Class>

    /**
     * Find a class by its ID.
     */
    fun findById(id: ClassId?): Optional<Class>

    /**
     * Find all resources matching a label.
     */
    fun findAllByLabel(label: String): Iterable<Class>

    /**
     * Find all resources matching a label partially.
     */
    fun findAllByLabelContaining(part: String): Iterable<Class>

    /**
     * Update a class.
     */
    fun update(`class`: Class): Class
}
