package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.application.CreateClassRequest
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClass
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClassIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClassRepository
import java.util.Optional
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jClassService(
    private val neo4jClassRepository: Neo4jClassRepository,
    private val neo4jClassIdGenerator: Neo4jClassIdGenerator
) : ClassService {

    override fun create(label: String) = create(UUID(0, 0), label)

    override fun create(userId: UUID, label: String): Class {
        val classId = neo4jClassIdGenerator.nextIdentity()
        return neo4jClassRepository
            .save(Neo4jClass(label = label, classId = classId, createdBy = userId, uri = null))
            .toClass()
    }

    override fun create(request: CreateClassRequest) = create(UUID(0, 0), request)

    override fun create(userId: UUID, request: CreateClassRequest): Class {
        val id = request.id ?: neo4jClassIdGenerator.nextIdentity()
        return neo4jClassRepository.save(
            Neo4jClass(label = request.label, uri = request.uri, classId = id, createdBy = userId)
        ).toClass()
    }

    override fun findAll() =
        neo4jClassRepository.findAll()
            .map(Neo4jClass::toClass)

    override fun findById(id: ClassId?): Optional<Class> =
        neo4jClassRepository.findByClassId(id)
            .map(Neo4jClass::toClass)

    override fun findAllByLabel(label: String): Iterable<Class> =
        neo4jClassRepository.findAllByLabelMatchesRegex("(?i)^${escapeRegexString(label)}$") // TODO: See declaration
            .map(Neo4jClass::toClass)

    override fun findAllByLabelContaining(part: String): Iterable<Class> =
        neo4jClassRepository.findAllByLabelMatchesRegex("(?i).*${escapeRegexString(part)}.*") // TODO: See declaration
            .map(Neo4jClass::toClass)

    override fun update(`class`: Class): Class {
        // already checked by service
        val found = neo4jClassRepository.findByClassId(`class`.id).get()

        // update all the properties
        found.label = `class`.label

        return neo4jClassRepository.save(found).toClass()
    }
}
