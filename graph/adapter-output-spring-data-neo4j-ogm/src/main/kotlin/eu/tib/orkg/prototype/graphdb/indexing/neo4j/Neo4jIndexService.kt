package eu.tib.orkg.prototype.graphdb.indexing.neo4j

import eu.tib.orkg.prototype.graphdb.indexing.domain.model.FullTextIndex
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.IndexService
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.Neo4jIndex
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.PropertyIndex
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.UniqueIndex
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j.Neo4jIndexRepository
import org.neo4j.driver.exceptions.DatabaseException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jIndexService(
    private val neo4jIndexRepository: Neo4jIndexRepository
) : IndexService {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun verifyIndices() {
        val existingIndexes = this.getIndexes()
        listOf(
            UniqueIndex(
                "Class",
                "class_id"
            ),
            UniqueIndex(
                "Literal",
                "literal_id"
            ),
            UniqueIndex(
                "Predicate",
                "predicate_id"
            ),
            UniqueIndex(
                "Resource",
                "resource_id"
            ),
            UniqueIndex(
                "Thing",
                "class_id"
            ),
            UniqueIndex(
                "Thing",
                "literal_id"
            ),
            UniqueIndex(
                "Thing",
                "predicate_id"
            ),
            UniqueIndex(
                "Thing",
                "resource_id"
            ),
            PropertyIndex(
                "Literal",
                "label"
            ),
            PropertyIndex(
                "Predicate",
                "label"
            ),
            PropertyIndex(
                "Resource",
                "label"
            ),
            PropertyIndex(
                "Paper",
                "created_by"
            ),
            PropertyIndex(
                "Thing",
                "label"
            ),
            PropertyIndex(
                "Thing",
                "created_at"
            ),
            PropertyIndex(
                "Resource",
                "created_at"
            ),
            PropertyIndex(
                "Resource",
                "created_by"
            ),
            PropertyIndex(
                "Class",
                "label"
            ),
            // These need to be property indexes, since resource_id is already part of a unique constraint.
            PropertyIndex(
                "Paper",
                "resource_id"
            ),
            PropertyIndex(
                "Contribution",
                "resource_id"
            ),
            // Contributions are heavily used in SimComp, and sorted by date → index that!
            PropertyIndex(
                "Contribution",
                "created_at"
            ),
            PropertyIndex(
                "Comparison",
                "resource_id"
            ),
            PropertyIndex(
                "Visualization",
                "resource_id"
            ),
            PropertyIndex(
                "ResearchField",
                "resource_id"
            ),
            PropertyIndex(
                "Model",
                "resource_id"
            ),
            PropertyIndex(
                "Venue",
                "resource_id"
            ),
            PropertyIndex(
                "Dataset",
                "resource_id"
            ),
            PropertyIndex(
                "Evaluation",
                "resource_id"
            ),
            PropertyIndex(
                "Author",
                "resource_id"
            ),
        ).forEach { index ->
            try {
                checkAndCreateConstraint(existingIndexes, index)
            } catch (ex: DatabaseException) {
                logger.warn("Error while creating index or constraint in Neo4j, command was: {}", index.toCypherQuery())
            }
        }
    }

    override fun getIndexes(): Iterable<Neo4jIndex> = neo4jIndexRepository.getExistingIndicesAndConstraints().map {
        when (it.type) {
            "node_unique_property" -> UniqueIndex(
                it.label,
                it.property
            )
            "node_label_property" -> PropertyIndex(
                it.label,
                it.property
            )
            "node_fulltext" -> FullTextIndex(
                it.label,
                it.property
            )
            else -> throw IllegalArgumentException("Cannot determine type when fetching indexes from Neo4j, got: ${it.type}")
        }
    }

    private fun checkAndCreateConstraint(
        existingIndexes: Iterable<Neo4jIndex>,
        neo4jIndex: Neo4jIndex
    ) {
        if (!existingIndexes.contains(neo4jIndex))
            neo4jIndexRepository.createIndex(neo4jIndex)
    }
}
