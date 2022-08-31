package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClass
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassRepository
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jClassAdapter(
    private val neo4jRepository: Neo4jClassRepository,
    private val neo4jClassIdGenerator: Neo4jClassIdGenerator,
) : ClassRepository {
    override fun save(c: Class): Class = neo4jRepository.save(c.toNeo4jClass()).toClass()

    override fun findAll(pageable: Pageable): Page<Class> = neo4jRepository.findAll(pageable).map(Neo4jClass::toClass)

    override fun exists(id: ClassId): Boolean = neo4jRepository.existsByClassId(id)

    override fun findByClassId(id: ClassId?): Optional<Class> =
        neo4jRepository.findByClassId(id).map(Neo4jClass::toClass)

    override fun findAllByLabel(label: String): Iterable<Class> =
        neo4jRepository.findAllByLabel(label).map(Neo4jClass::toClass)

    override fun findAllByLabel(label: String, pageable: Pageable): Page<Class> =
        neo4jRepository.findAllByLabel(label, pageable).map(Neo4jClass::toClass)

    override fun findAllByLabelMatchesRegex(label: String): Iterable<Class> =
        neo4jRepository.findAllByLabelMatchesRegex(label).map(Neo4jClass::toClass)

    override fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Class> =
        neo4jRepository.findAllByLabelMatchesRegex(label, pageable).map(Neo4jClass::toClass)

    override fun findAllByLabelContaining(part: String): Iterable<Class> =
        neo4jRepository.findAllByLabelContaining(part).map(Neo4jClass::toClass)

    override fun findByUri(uri: String): Optional<Class> = neo4jRepository.findByUri(uri).map(Neo4jClass::toClass)

    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun nextIdentity(): ClassId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: ClassId
        do {
            id = neo4jClassIdGenerator.nextIdentity()
        } while (neo4jRepository.existsByClassId(id))
        return id
    }

    private fun Class.toNeo4jClass(): Neo4jClass =
        neo4jRepository.findByClassId(id).orElse(Neo4jClass()).apply {
            classId = this@toNeo4jClass.id
            label = this@toNeo4jClass.label
            uri = this@toNeo4jClass.uri?.toString()
            createdBy = this@toNeo4jClass.createdBy
            createdAt = this@toNeo4jClass.createdAt
        }
}
