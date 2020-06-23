package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import java.util.Optional
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jStatementRepository :
    Neo4jRepository<Neo4jStatement, Long> {

    override fun findAll(depth: Int): Iterable<Neo4jStatement>

    override fun findById(id: Long): Optional<Neo4jStatement>

    fun findByStatementId(id: StatementId): Optional<Neo4jStatement>

    @Query("MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Thing`) WHERE sub.`resource_id`={0} OR sub.`literal_id`={0} OR sub.`predicate_id`={0} OR sub.`class_id`={0} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllBySubject(subjectId: String, pagination: Pageable): Slice<Neo4jStatement>

    fun findAllByPredicateId(predicateId: PredicateId, pagination: Pageable): Slice<Neo4jStatement>

    @Query("MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Thing`) WHERE obj.`resource_id`={0} OR obj.`literal_id`={0} OR obj.`predicate_id`={0} OR obj.`class_id`={0} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllByObject(objectId: String, pagination: Pageable): Slice<Neo4jStatement>

    @Query("""MATCH (p:`Thing`)-[*]->() WHERE p.`resource_id`={0} OR p.`literal_id`={0} OR p.`predicate_id`={0} OR p.`class_id`={0} RETURN COUNT(p)""")
    fun countByIdRecursive(paperId: String): Int

    @Query("MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Thing`) WHERE (obj.`resource_id`={0} OR obj.`literal_id`={0} OR obj.`predicate_id`={0} OR obj.`class_id`={0}) AND rel.`predicate_id`={1} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Slice<Neo4jStatement>

    @Query("MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Thing`) WHERE (sub.`resource_id`={0} OR sub.`literal_id`={0} OR sub.`predicate_id`={0} OR sub.`class_id`={0}) AND rel.`predicate_id`={1} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Slice<Neo4jStatement>

    @Query("MATCH (template:`Resource`)-[rel:`RELATED` {predicate_id:'TemplateOfClass'}]->(res:`Class`) WHERE res.`class_id`={0} RETURN template LIMIT 1")
    fun findTemplate(classId: ClassId): Optional<Neo4jResource>

    @Query("""MATCH (temp:`Thing`)-[rel:`RELATED`]->(format:`Literal`) WHERE temp.`resource_id`={0} AND rel.`predicate_id`='TemplateLabelFormat' RETURN format LIMIT 1""")
    fun checkIfTemplateIsFormatted(templateId: ResourceId): Optional<Neo4jLiteral>
}
