package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.IndexService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jIndexInfo
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jIndexRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jIndexService(
    private val neo4jIndexRepository: Neo4jIndexRepository
) : IndexService {

    override fun createRequiredUniqueConstraints() {
        val existingConstraints = neo4jIndexRepository.getExistingIndicesAndConstraints()
        checkAndCreateConstraint(existingConstraints, "Class", "class_id", IndexType.UNIQUE)
        checkAndCreateConstraint(existingConstraints, "Literal", "literal_id", IndexType.UNIQUE)
        checkAndCreateConstraint(existingConstraints, "Predicate", "predicate_id", IndexType.UNIQUE)
        checkAndCreateConstraint(existingConstraints, "Resource", "resource_id", IndexType.UNIQUE)
        checkAndCreateConstraint(existingConstraints, "Thing", "class_id", IndexType.UNIQUE)
        checkAndCreateConstraint(existingConstraints, "Thing", "literal_id", IndexType.UNIQUE)
        checkAndCreateConstraint(existingConstraints, "Thing", "predicate_id", IndexType.UNIQUE)
        checkAndCreateConstraint(existingConstraints, "Thing", "resource_id", IndexType.UNIQUE)
    }

    override fun createRequiredPropertyIndices() {
        val existingConstraints = neo4jIndexRepository.getExistingIndicesAndConstraints()
        checkAndCreateConstraint(existingConstraints, "Literal", "label", IndexType.PROPERTY)
        checkAndCreateConstraint(existingConstraints, "Predicate", "label", IndexType.PROPERTY)
        checkAndCreateConstraint(existingConstraints, "Resource", "label", IndexType.PROPERTY)
        checkAndCreateConstraint(existingConstraints, "Class", "label", IndexType.PROPERTY)
    }

    private fun checkAndCreateConstraint(
        existingConstraints: Iterable<Neo4jIndexInfo>,
        label: String,
        property: String,
        indexType: IndexType
    ) {
        val found = existingConstraints
            .firstOrNull { it.label == label && it.property == property && it.type == indexType.value }
        if (found == null) {
            if (indexType == IndexType.PROPERTY)
                neo4jIndexRepository.createPropertyIndex(label, property)
            else
                neo4jIndexRepository.createUniqueConstraint(label, property)
        }
    }
}

private enum class IndexType(val value: String) {
    UNIQUE("node_unique_property"),
    PROPERTY("node_label_property")
}
