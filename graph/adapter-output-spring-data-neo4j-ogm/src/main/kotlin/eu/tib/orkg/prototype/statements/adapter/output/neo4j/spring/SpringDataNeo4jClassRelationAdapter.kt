package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassRelationRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassRepository
import eu.tib.orkg.prototype.statements.domain.model.ClassSubclassRelation
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ClassRelationRepository
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jClassRelationAdapter(
    private val neo4jRepository: Neo4jClassRelationRepository,
    private val neo4jClassRepository: Neo4jClassRepository
) : ClassRelationRepository {
    override fun save(classRelation: ClassSubclassRelation) {
        neo4jRepository.save(classRelation.toNeo4jClassRelation(neo4jRepository, neo4jClassRepository))
    }

    override fun saveAll(classRelations: Set<ClassSubclassRelation>) {
        neo4jRepository.saveAll(classRelations.map { it.toNeo4jClassRelation(neo4jRepository, neo4jClassRepository) })
    }

    override fun deleteByChildClassId(childId: ThingId) =
        neo4jRepository.removeByChildClassId(childId.toClassId())

    override fun deleteAll() = neo4jRepository.deleteAll()
}
