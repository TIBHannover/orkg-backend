package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreateClassRequest
import java.net.URI
import java.util.Optional
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ClassService : URIService<Class> {
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
    fun create(userId: ContributorId, label: String): Class

    /**
     * Create a new class from a request.
     */
    fun create(request: CreateClassRequest): Class

    /**
     * Create a new class from a request belonging to a given user.
     */
    fun create(userId: ContributorId, request: CreateClassRequest): Class

    /**
     * Find all classes.
     */
    fun findAll(): Iterable<Class>

    /**
     * Find all classes (paginated).
     */
    fun findAll(pageable: Pageable): Page<Class>

    /**
     * Find a class by its ID.
     */
    fun findById(id: ClassId?): Optional<Class>

    /**
     * Find all resources matching a label.
     */
    fun findAllByLabel(label: String): Iterable<Class>

    /**
     * Find all classes matching a label (paginated).
     */
    fun findAllByLabel(pageable: Pageable, label: String): Page<Class>

    /**
     * Find all resources matching a label partially.
     */
    fun findAllByLabelContaining(part: String): Iterable<Class>

    /**
     * Find all classes matching a label partially (paginated).
     */
    fun findAllByLabelContaining(pageable: Pageable, part: String): Page<Class>

    /**
     * Update a class.
     */
    fun update(`class`: Class): Class

    /**
     * Create if not exists a class
     */
    fun createIfNotExists(id: ClassId, label: String, uri: URI?)
}
