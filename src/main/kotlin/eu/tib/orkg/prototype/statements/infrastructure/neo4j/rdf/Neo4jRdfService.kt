package eu.tib.orkg.prototype.statements.infrastructure.neo4j.rdf

import eu.tib.orkg.prototype.statements.domain.model.neo4j.IdTriple
import eu.tib.orkg.prototype.statements.domain.model.neo4j.LiteralTriple
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClass
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClassRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicate
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicateRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementWithLiteralRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementWithResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.rdf.RdfService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jRdfService(
    private val resourceStatementRepository: Neo4jStatementWithResourceRepository,
    private val predicateRepository: Neo4jPredicateRepository,
    private val resourceRepository: Neo4jResourceRepository,
    private val classesRepository: Neo4jClassRepository,
    private val literalStatementRepository: Neo4jStatementWithLiteralRepository
) : RdfService {
    override fun dumpToNTriple(): String {
        // dump classes
        var result = classesRepository.findAll().joinToString("\n", transform = Neo4jClass::toNTripleWithPrefix)
        result += "\n"
        // dump predicates
        result += predicateRepository.findAll().joinToString("\n", transform = Neo4jPredicate::toNTripleWithPrefix)
        result += "\n"
        // dump resources
        result += resourceRepository.findAll().joinToString("\n", transform = Neo4jResource::toNTripleWithPrefix)
        result += "\n"
        // dump object statements
        result += resourceStatementRepository.listByIds().joinToString("\n", transform = IdTriple::toNTripleWithPrefix)
        result += "\n"
        // dump literal statements
        result += literalStatementRepository.listByIds().joinToString("\n", transform = LiteralTriple::toNTripleWithPrefix)
        result += "\n"
        return result
    }
}
