package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ClassRepository {

    fun findAll(): Iterable<Class>

    /**
     * Find all classes (paginated).
     */
    fun findAll(pageable: Pageable): Page<Class>

    /**
     * Find a class by its ID.
     */
    fun findById(id: ClassId): Optional<Class>

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
}
