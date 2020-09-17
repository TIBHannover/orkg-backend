package eu.tib.orkg.prototype.graphdb.indexing.neo4j

import eu.tib.orkg.prototype.graphdb.indexing.domain.model.IndexService
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j.FulltextIndex
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j.Neo4jIndex
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j.Neo4jIndexRepository
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j.PropertyIndex
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j.UniqueIndex
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
            PropertyIndex("Class", "label")
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
            "node_fulltext" -> FulltextIndex(it.label, it.property)
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
