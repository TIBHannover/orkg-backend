package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreateClassRequest
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClass
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClassIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClassRepository
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import java.net.URI
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jClassService(
    private val neo4jClassRepository: Neo4jClassRepository,
    private val neo4jClassIdGenerator: Neo4jClassIdGenerator
) : ClassService {

    private val classCache: MutableMap<ClassId, Class> = ConcurrentHashMap(64)

    override fun create(label: String) = create(ContributorId.createUnknownContributor(), label)

    override fun create(userId: ContributorId, label: String): Class {
        val classId = neo4jClassIdGenerator.nextIdentity()
        return neo4jClassRepository
            .save(Neo4jClass(classId = classId, createdBy = userId, label = label, uri = null))
            .toClass()
    }

    override fun create(request: CreateClassRequest) = create(ContributorId.createUnknownContributor(), request)

    override fun create(userId: ContributorId, request: CreateClassRequest): Class {
        var id = request.id ?: neo4jClassIdGenerator.nextIdentity()

        // Should be moved to the Generator in the future
        while (neo4jClassRepository.findByClassId(id).isPresent) {
            id = neo4jClassIdGenerator.nextIdentity()
        }

        return neo4jClassRepository.save(
            Neo4jClass(classId = id, createdBy = userId, label = request.label, uri = request.uri)
        ).toClass()
    }

    override fun exists(id: ClassId): Boolean {
        // Instead of just asking the database an "exists query", we fetch the full object if it cannot be found.
        // This is a certain overhead but will keep the cache warm for use in the find() methods, of needed.
        return findClassCached(id) != null
    }

    override fun findAll() =
        neo4jClassRepository.findAll()
            .map(Neo4jClass::toClass)

    override fun findAll(pageable: Pageable): Page<Class> =
        neo4jClassRepository.findAll(pageable)
            .map(Neo4jClass::toClass)

    override fun findById(id: ClassId): Optional<Class> = Optional.ofNullable(findClassCached(id))

    override fun findAllByLabel(label: String): Iterable<Class> =
        neo4jClassRepository.findAllByLabelMatchesRegex(label.toExactSearchString()) // TODO: See declaration
            .map(Neo4jClass::toClass)

    override fun findAllByLabel(pageable: Pageable, label: String): Page<Class> =
        neo4jClassRepository.findAllByLabelMatchesRegex(label.toExactSearchString(), pageable) // TODO: See declaration
            .map(Neo4jClass::toClass)

    override fun findAllByLabelContaining(part: String): Iterable<Class> =
        neo4jClassRepository.findAllByLabelMatchesRegex(part.toSearchString()) // TODO: See declaration
            .map(Neo4jClass::toClass)

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Page<Class> =
        neo4jClassRepository.findAllByLabelMatchesRegex(part.toSearchString(), pageable) // TODO: See declaration
            .map(Neo4jClass::toClass)

    override fun update(`class`: Class): Class {
        // already checked by service
        val found = neo4jClassRepository.findByClassId(`class`.id).get()

        // update all the properties
        found.label = `class`.label

        return neo4jClassRepository.save(found).toClass()
    }

    override fun removeAll() = neo4jClassRepository.deleteAll()

    override fun findByURI(uri: URI): Optional<Class> =
        neo4jClassRepository
            .findByUri(uri.toString())
            .map(Neo4jClass::toClass)

    override fun createIfNotExists(id: ClassId, label: String, uri: URI?) {
        // Checking if URI is null
        if (uri == null) {
            // check only for ID
            val found = neo4jClassRepository.findByClassId(id)
            if (found.isEmpty) {
                neo4jClassRepository.save(Neo4jClass(classId = id, label = label, uri = uri))
            }
        } else {
            val oClassByURI = neo4jClassRepository.findByUri(uri.toString())
            val oClassById = neo4jClassRepository.findByClassId(id)
            when {
                oClassById.isEmpty && oClassByURI.isEmpty -> neo4jClassRepository.save(
                    Neo4jClass(
                        classId = id,
                        label = label,
                        uri = uri
                    )
                )
                // Throwing an exception if IDs are different
                oClassById.isPresent && oClassByURI.isPresent
                    && oClassById.get().id != oClassByURI.get().id -> throw Exception("ID mismatch for class ID: ${oClassById.get().id}")
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
        val persisted = neo4jClassRepository.findByClassId(`class`).map(Neo4jClass::toClass)
        if (persisted.isPresent) {
            val instance = persisted.get()
            classCache[`class`] = instance
            return instance
        }
        return null
    }
}
