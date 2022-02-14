package eu.tib.orkg.prototype.statements.infrastructure.neo4j.rdf

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClass
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatement
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementRepository
import eu.tib.orkg.prototype.statements.domain.model.rdf.RdfService
import eu.tib.orkg.prototype.statements.domain.model.toNTriple
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jRdfService(
    private val statementRepository: Neo4jStatementRepository,
    private val predicateRepository: PredicateRepository,
    private val resourceRepository: Neo4jResourceRepository,
    private val classesRepository: Neo4jClassRepository
) : RdfService {
    override fun dumpToNTriple(): String {
        // dump classes
        var result = classesRepository.findAll().joinToString("\n", transform = Neo4jClass::toNTriple)
        result += "\n"
        // dump predicates
        var predicates: Page<Predicate> = predicateRepository.findAll(PageRequest.of(0, 1000))
        result += predicates.joinToString("\n", transform = Predicate::toNTriple)
        result += "\n"
        while (predicates.hasNext()) {
            predicates = predicateRepository.findAll(predicates.nextPageable())
            result += predicates.joinToString("\n", transform = Predicate::toNTriple)
            result += "\n"
        }
        // dump resources
        result += resourceRepository.findAll().joinToString("\n", transform = Neo4jResource::toNTriple)
        result += "\n"
        // dump statements
        result += statementRepository.findAll().joinToString("\n", transform = Neo4jStatement::toNTriple)
        result += "\n"
        return result
    }
}
