package eu.tib.orkg.prototype.statements.infrastructure.neo4j.rdf

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.rdf.RdfService
import eu.tib.orkg.prototype.statements.domain.model.toNTriple
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.io.OutputStream
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jRdfService(
    private val statementRepository: StatementRepository,
    private val predicateRepository: PredicateRepository,
    private val resourceRepository: ResourceRepository,
    private val classesRepository: ClassRepository
) : RdfService {
    override fun dumpToNTriple(out: OutputStream) {
        val everything = sequenceOf<String>() + // Just for aligning the code below…
            classesRepository.findAll().map(Class::toNTriple) +
            predicateRepository.findAll().map(Predicate::toNTriple) +
            resourceRepository.findAll().map(Resource::toNTriple) +
            statementRepository.findAll().map(GeneralStatement::toNTriple)
        everything.forEach { out.write(it.toByteArray()) }
    }
}
