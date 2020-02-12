package eu.tib.orkg.prototype.statements.infrastructure.neo4j.rdf

import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClass
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClassRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicate
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicateRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatement
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementRepository
import eu.tib.orkg.prototype.statements.domain.model.rdf.RdfService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jRdfService(
    private val statementRepository: Neo4jStatementRepository,
    private val predicateRepository: Neo4jPredicateRepository,
    private val resourceRepository: Neo4jResourceRepository,
    private val classesRepository: Neo4jClassRepository
) : RdfService {
    override fun dumpToNTriple(): String {
        // dump classes
        var result = classesRepository.findAll().joinToString("\n", transform = Neo4jClass::toNTriple)
        result += "\n"
        // dump predicates
        result += predicateRepository.findAll().joinToString("\n", transform = Neo4jPredicate::toNTriple)
        result += "\n"
        // dump resources
        result += resourceRepository.findAll().joinToString("\n", transform = Neo4jResource::toNTriple)
        result += "\n"
        // dump statements
        result += statementRepository.findAll().joinToString("\n", transform = Neo4jStatement::toNTriple)
        result += "\n"
        return result
    }
}
