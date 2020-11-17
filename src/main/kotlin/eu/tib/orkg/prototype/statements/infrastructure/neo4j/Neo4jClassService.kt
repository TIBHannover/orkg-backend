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
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jClassService(
    private val neo4jClassRepository: Neo4jClassRepository,
    private val neo4jClassIdGenerator: Neo4jClassIdGenerator
) : ClassService {

    override fun create(label: String) = create(ContributorId.createUnknownContributor(), label)

    override fun create(userId: ContributorId, label: String): Class {
        val classId = neo4jClassIdGenerator.nextIdentity()
        return neo4jClassRepository
            .save(Neo4jClass(classId = classId, createdBy = userId.value, label = label, uri = null))
            .toClass()
    }

    override fun create(request: CreateClassRequest) = create(ContributorId.createUnknownContributor(), request)

    override fun create(userId: ContributorId, request: CreateClassRequest): Class {
        val id = request.id ?: neo4jClassIdGenerator.nextIdentity()
        return neo4jClassRepository.save(
            Neo4jClass(classId = id, createdBy = userId.value, label = request.label, uri = request.uri)
        ).toClass()
    }

    override fun findAll() =
        neo4jClassRepository.findAll()
            .map(Neo4jClass::toClass)

    override fun findAll(pageable: Pageable): Iterable<Class> =
        neo4jClassRepository.findAll(pageable)
            .content
            .map(Neo4jClass::toClass)

    override fun findById(id: ClassId?): Optional<Class> =
        neo4jClassRepository.findByClassId(id)
            .map(Neo4jClass::toClass)

    override fun findAllByLabel(label: String): Iterable<Class> =
        neo4jClassRepository.findAllByLabelMatchesRegex(label.toExactSearchString()) // TODO: See declaration
            .map(Neo4jClass::toClass)

    override fun findAllByLabel(pageable: Pageable, label: String): Iterable<Class> =
        neo4jClassRepository.findAllByLabelMatchesRegex(label.toExactSearchString(), pageable) // TODO: See declaration
            .content
            .map(Neo4jClass::toClass)

    override fun findAllByLabelContaining(part: String): Iterable<Class> =
        neo4jClassRepository.findAllByLabelMatchesRegex(part.toSearchString()) // TODO: See declaration
            .map(Neo4jClass::toClass)

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Iterable<Class> =
        neo4jClassRepository.findAllByLabelMatchesRegex(part.toSearchString(), pageable) // TODO: See declaration
            .content
            .map(Neo4jClass::toClass)

    override fun update(`class`: Class): Class {
        // already checked by service
        val found = neo4jClassRepository.findByClassId(`class`.id).get()

        // update all the properties
        found.label = `class`.label

        return neo4jClassRepository.save(found).toClass()
    }

    override fun findByURI(uri: URI): Optional<Class> =
        neo4jClassRepository
            .findByUri(uri.toString())
            .map(Neo4jClass::toClass)

    private fun String.toSearchString() = "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() = "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"
}
