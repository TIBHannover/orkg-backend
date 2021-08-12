package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.toCypher
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThingRepository
import eu.tib.orkg.prototype.statements.ports.ThingRepository
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ThingPersistenceAdapter(
    private val client: Neo4jClient
): ThingRepository {

    override fun findAll(): Iterable<Thing> {
        val result = client
            .query("MATCH (node:Thing) RETURN node")
            .fetchAs<Neo4jThing>()
            .all()
        return result.map { it.toThing() }
    }

    override fun findAll(pageable: Pageable): Iterable<Thing> {
        val result = client
            .query("MATCH (node:Thing) RETURN node ${pageable.toCypher()}")
            .fetchAs<Neo4jThing>()
            .all()
        return PageImpl(result.map { it.toThing() })
    }

    override fun findById(id: String?): Optional<Thing> {
        val result = client
            .query("MATCH (node:Thing) WHERE node.`resource_id`=$ID OR node.`literal_id`=$ID OR node.`predicate_id`=$ID OR node.`class_id`=$ID RETURN node")
            .bind(id).to("id")
            .fetchAs<Neo4jThing>()
            .one()
        return Optional.ofNullable(result).map { it.toThing() }
    }
}

/** Simple helper string to make queries a bit more readable. */
private const val ID = "${'$'}id"
