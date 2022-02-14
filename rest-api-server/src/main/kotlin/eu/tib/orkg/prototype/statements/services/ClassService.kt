package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.application.CreateClassRequest
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ClassService(
    private val repository: ClassRepository
) : ClassUseCases {

    private val classCache: MutableMap<ClassId, Class> = ConcurrentHashMap(64)

    override fun create(label: String) = create(ContributorId.createUnknownContributor(), label)

    override fun create(userId: ContributorId, label: String): Class {
        val classId = repository.nextIdentity()
        val newClass = Class(
            id = classId,
            label = label,
            createdAt = OffsetDateTime.now(),
            createdBy = userId,
            uri = null,
        )
        return repository.save(newClass)
    }

    override fun create(request: CreateClassRequest) = create(ContributorId.createUnknownContributor(), request)

    override fun create(userId: ContributorId, request: CreateClassRequest): Class {
        var id = request.id ?: repository.nextIdentity()

        // Should be moved to the Generator in the future
        while (repository.findByClassId(id).isPresent) {
            id = repository.nextIdentity()
        }

        val newClass = Class(
            id = id,
            label = request.label,
            createdAt = OffsetDateTime.now(),
            createdBy = userId,
            uri = request.uri,
        )
        return repository.save(newClass)
    }

    override fun exists(id: ClassId): Boolean {
        // Instead of just asking the database an "exists query", we fetch the full object if it cannot be found.
        // This is a certain overhead but will keep the cache warm for use in the find() methods, of needed.
        return findClassCached(id) != null
    }

    override fun findAll() = repository.findAll()

    override fun findAll(pageable: Pageable): Page<Class> = repository.findAll(pageable)

    override fun findById(id: ClassId): Optional<Class> = Optional.ofNullable(findClassCached(id))

    override fun findAllByLabel(label: String): Iterable<Class> =
        repository.findAllByLabelMatchesRegex(label.toExactSearchString()) // TODO: See declaration

    override fun findAllByLabel(pageable: Pageable, label: String): Page<Class> =
        repository.findAllByLabelMatchesRegex(label.toExactSearchString(), pageable) // TODO: See declaration

    override fun findAllByLabelContaining(part: String): Iterable<Class> =
        repository.findAllByLabelMatchesRegex(part.toSearchString()) // TODO: See declaration

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Page<Class> =
        repository.findAllByLabelMatchesRegex(part.toSearchString(), pageable) // TODO: See declaration

    override fun update(`class`: Class): Class {
        // already checked by service
        val found = repository.findByClassId(`class`.id).get()
        var updated = found
        // update all the properties
        updated = updated.copy(label = `class`.label)
        if (`class`.uri != null)
            updated = updated.copy(uri = `class`.uri)

        return repository.save(updated)
    }

    override fun removeAll() = repository.deleteAll()

    override fun findByURI(uri: URI): Optional<Class> = repository.findByUri(uri.toString())

    override fun createIfNotExists(id: ClassId, label: String, uri: URI?) {
        // Checking if URI is null
        if (uri == null) {
            // check only for ID
            val found = repository.findByClassId(id)
            if (found.isEmpty) {
                repository.save(
                    Class(
                        id = id,
                        label = label,
                        uri = uri,
                        createdAt = OffsetDateTime.now(),
                        createdBy = ContributorId.createUnknownContributor()
                    )
                )
            }
        } else {
            val oClassByURI = repository.findByUri(uri.toString())
            val oClassById = repository.findByClassId(id)
            when {
                oClassById.isEmpty && oClassByURI.isEmpty -> repository.save(
                    Class(
                        id = id,
                        label = label,
                        uri = uri,
                        createdAt = OffsetDateTime.now(),
                        createdBy = ContributorId.createUnknownContributor()
                    )
                )
                // Throwing an exception if IDs are different
                oClassById.isPresent && oClassByURI.isPresent &&
                    oClassById.get().id != oClassByURI.get().id -> throw Exception("ID mismatch for class ID: ${oClassById.get().id}")
            }
        }
    }

    private fun String.toSearchString() =
        "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() =
        "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"

    private fun findClassCached(`class`: ClassId): Class? {
        val cached = classCache[`class`]
        if (cached != null)
            return cached
        val persisted = repository.findByClassId(`class`)
        if (persisted.isPresent) {
            val instance = persisted.get()
            classCache[`class`] = instance
            return instance
        }
        return null
    }
}
