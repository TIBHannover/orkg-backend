package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClass
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClassRepository
import eu.tib.orkg.prototype.statements.ports.ClassRepository
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

@Component
class ClassPersistenceAdapter(
    private val neo4jClassRepository: Neo4jClassRepository,
) : ClassRepository {

    private val classCache: MutableMap<ClassId, Class> = ConcurrentHashMap(64)

    override fun findAll() =
        neo4jClassRepository.findAll()
            .map(Neo4jClass::toClass)

    override fun findAll(pageable: Pageable): Page<Class> =
        neo4jClassRepository.findAll(pageable)
            .map(Neo4jClass::toClass)

    fun findAllById(ids: List<ClassId>): Iterable<Class> =
        neo4jClassRepository.findAllByClassIdIn(ids)
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
