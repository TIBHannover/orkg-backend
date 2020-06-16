package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.IndexService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jIndexRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jIndexService(
    private val neo4jIndexRepository: Neo4jIndexRepository
) : IndexService {

    override fun createRequiredUniqueConstraints() {
        neo4jIndexRepository.createUniqueConstraint( "Class", "class_id")
        neo4jIndexRepository.createUniqueConstraint( "Literal", "literal_id")
        neo4jIndexRepository.createUniqueConstraint( "Predicate", "predicate_id")
        neo4jIndexRepository.createUniqueConstraint("Resource", "resource_id")
        neo4jIndexRepository.createUniqueConstraint( "Thing", "class_id")
        neo4jIndexRepository.createUniqueConstraint("Thing", "literal_id")
        neo4jIndexRepository.createUniqueConstraint("Thing", "predicate_id")
        neo4jIndexRepository.createUniqueConstraint("Thing", "resource_id")
    }

    override fun createRequiredPropertyIndices() {
        neo4jIndexRepository.createPropertyIndex( "Literal", "label")
        /*neo4jIndexRepository.createPropertyIndex("Predicate", "label")
        neo4jIndexRepository.createPropertyIndex( "Resource", "label")
        neo4jIndexRepository.createPropertyIndex("Class", "label")*/
    }
}
