package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jClassRepository : Neo4jRepository<Neo4jClass, Long> {
    override fun findAll(): List<Neo4jClass>

    override fun findById(id: Long?): Optional<Neo4jClass>

    fun findAllByClassIdIn(ids: List<ClassId>): Iterable<Neo4jClass>

    fun findByClassId(id: ClassId?): Optional<Neo4jClass>

    fun findAllByLabel(label: String): Iterable<Neo4jClass>

    fun findAllByLabel(label: String, pageable: Pageable): Page<Neo4jClass>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String): Iterable<Neo4jClass>

    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Neo4jClass>

    fun findAllByLabelContaining(part: String): Iterable<Neo4jClass>

    fun findByUri(uri: String): Optional<Neo4jClass>
}
