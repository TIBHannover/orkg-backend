package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jClassRepository : Neo4jRepository<Neo4jClass, Long> {
    fun existsByClassId(id: ClassId): Boolean

    fun existsAllByClassId(ids: Iterable<ClassId>): Boolean

    fun findByClassId(id: ClassId?): Optional<Neo4jClass>

    fun findAllByLabel(label: String): Iterable<Neo4jClass>

    fun findAllByLabel(label: String, pageable: Pageable): Page<Neo4jClass>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String): Iterable<Neo4jClass>

    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Neo4jClass>

    fun findAllByLabelContaining(part: String): Iterable<Neo4jClass>

    fun findByUri(uri: String): Optional<Neo4jClass>
}
