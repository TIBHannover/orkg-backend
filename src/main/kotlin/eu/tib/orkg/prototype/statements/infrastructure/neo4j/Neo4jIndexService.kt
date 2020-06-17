package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.IndexService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jIndexInfo
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jIndexRepository
import org.neo4j.driver.exceptions.DatabaseException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jIndexService(
    private val neo4jIndexRepository: Neo4jIndexRepository
) : IndexService {

    override fun createRequiredUniqueConstraints() {
        val existingConstraints = neo4jIndexRepository.getExistingIndicesAndConstraints()
        listOf(
            "Class" to "class_id",
            "Literal" to "literal_id",
            "Predicate" to "predicate_id",
            "Resource" to "resource_id",
            "Thing" to "class_id",
            "Thing" to "literal_id",
            "Thing" to "predicate_id",
            "Thing" to "resource_id"
        ).forEach { (nodeLabel, property) ->
            try {
                checkAndCreateConstraint(existingConstraints, nodeLabel, property, IndexType.UNIQUE)
            } catch (ex: DatabaseException) {
                println("Unique constrains :$nodeLabel($property), can't be created")
            }
        }
    }

    override fun createRequiredPropertyIndices() {
        val existingConstraints = neo4jIndexRepository.getExistingIndicesAndConstraints()
        listOf(
            "Literal" to "label",
            "Predicate" to "label",
            "Resource" to "label",
            "Class" to "label"
        ).forEach { (nodeLabel, property) ->
            try {
                checkAndCreateConstraint(existingConstraints, nodeLabel, property, IndexType.PROPERTY)
            } catch (ex: DatabaseException) {
                println("Property Index :$nodeLabel($property), can't be created.")
            }
        }
    }

    private fun checkAndCreateConstraint(
        existingConstraints: Iterable<Neo4jIndexInfo>,
        label: String,
        property: String,
        indexType: IndexType
    ) {
        val newConstraint = Neo4jIndexInfo(label, property, indexType.value)
        if (!existingConstraints.any { it == newConstraint }) {
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
