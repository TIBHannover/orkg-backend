package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.dsl.CypherQueryBuilder
import eu.tib.orkg.prototype.dsl.PagedQueryBuilder.countOver
import eu.tib.orkg.prototype.dsl.PagedQueryBuilder.mappedBy
import eu.tib.orkg.prototype.dsl.SingleQueryBuilder.fetchAs
import eu.tib.orkg.prototype.dsl.SingleQueryBuilder.mappedBy
import eu.tib.orkg.prototype.statements.api.RetrieveClassHierarchyUseCase.ChildClass
import eu.tib.orkg.prototype.statements.api.RetrieveClassHierarchyUseCase.ClassHierarchyEntry
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassSubclassRelation
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository
import eu.tib.orkg.prototype.statements.spi.ClassRelationRepository
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.neo4j.cypherdsl.core.Cypher.anyNode
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.parameter
import org.neo4j.cypherdsl.core.Cypher.unwind
import org.neo4j.cypherdsl.core.Functions.collect
import org.neo4j.cypherdsl.core.Functions.count
import org.neo4j.cypherdsl.core.Functions.id
import org.neo4j.cypherdsl.core.Functions.labels
import org.neo4j.cypherdsl.core.Predicates.any
import org.neo4j.cypherdsl.core.Predicates.exists
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

private const val SUBCLASS_OF = "SUBCLASS_OF"

@Component
class SpringDataNeo4jClassHierarchyAdapter(
    override val neo4jClient: Neo4jClient
) : SpringDataNeo4jAdapter(neo4jClient), ClassHierarchyRepository, ClassRelationRepository {

    override fun save(classRelation: ClassSubclassRelation) {
        CypherQueryBuilder(neo4jClient)
            .withQuery {
                val child = node("Class")
                val parent = node("Class")
                match(child)
                    .where(
                        child.property("id").eq(parameter("childId"))
                    ).match(parent).where(
                        parent.property("id").eq(parameter("parentId"))
                    ).create(
                        child.relationshipTo(parent, SUBCLASS_OF).withProperties(
                            "created_by", parameter("createdBy"),
                            "created_at", parameter("createdAt")
                        )
                    )
            }
            .withParameters(
                "childId" to classRelation.child.id.value,
                "parentId" to classRelation.parent.id.value,
                "createdBy" to classRelation.createdBy.value.toString(),
                "createdAt" to classRelation.createdAt.format(ISO_OFFSET_DATE_TIME)
            )
            .run()
    }

    override fun saveAll(classRelations: Set<ClassSubclassRelation>) {
        CypherQueryBuilder(neo4jClient)
            .withQuery {
                val child = node("Class")
                val parent = node("Class")
                val rows = parameter("rows")
                val row = name("row")
                val r = child.relationshipTo(parent, SUBCLASS_OF).named("r")
                unwind(rows).`as`(row)
                    .with(row)
                    .match(child).where(
                        child.property("id").eq(row.property("child_id"))
                    ).match(parent).where(
                        parent.property("id").eq(row.property("parent_id"))
                    ).create(
                        r.withProperties(
                            "created_by", row.property("created_by"),
                            "created_at", row.property("created_at")
                        )
                    ).returning(id(r))
            }
            .withParameters(
                "rows" to classRelations.map {
                    mapOf(
                        "child_id" to it.child.id.value,
                        "parent_id" to it.parent.id.value,
                        "created_by" to it.createdBy.value.toString(),
                        "created_at" to it.createdAt.format(ISO_OFFSET_DATE_TIME)
                    )
                }
            )
            .run()
    }

    override fun deleteByChildId(childId: ThingId) {
        CypherQueryBuilder(neo4jClient)
            .withQuery {
                val r = name("r")
                match(
                    node("Class")
                        .withProperties("id", parameter("childId"))
                        .relationshipTo(node("Class"), SUBCLASS_OF)
                        .named(r)
                ).delete(r)
            }
            .withParameters("childId" to childId.value)
            .run()
    }

    override fun deleteAll() {
        CypherQueryBuilder(neo4jClient)
            .withQuery {
                val r = name("r")
                match(
                    node("Class")
                        .relationshipTo(node("Class"), SUBCLASS_OF)
                        .named(r)
                ).delete(r)
            }
            .run()
    }

    override fun findChildren(id: ThingId, pageable: Pageable): Page<ChildClass> = CypherQueryBuilder(neo4jClient)
        .withCommonQuery {
            match(
                node("Class")
                    .named("c")
                    .relationshipTo(
                        node("Class")
                            .named("p")
                            .withProperties("id", parameter("id")),
                        SUBCLASS_OF
                    )
            )
        }
        .withQuery { commonQuery ->
            val c = name("c")
            val g =  node("Class").named("g")
            commonQuery
                .optionalMatch(g.relationshipTo(anyNode().named(c)))
                .returning(c, count(g).`as`("childCount"))
                .orderBy(c.property("id").ascending())
        }
        .countOver("c")
        .withParameters("id" to id.value)
        .mappedBy { _, record -> ChildClass(record["c"].asNode().toClass(), record["childCount"].asLong()) }
        .fetch(pageable)

    override fun findParent(id: ThingId): Optional<Class> = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val p = name("p")
            match(
                node("Class")
                    .withProperties("id", parameter("id"))
                    .relationshipTo(anyNode("Class").named(p), SUBCLASS_OF)
            ).returning(p)
        }
        .withParameters("id" to id.value)
        .mappedBy(ClassMapper("p"))
        .one()

    override fun findRoot(id: ThingId): Optional<Class> = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val r = name("r")
            val root = anyNode("Class")
                .named(r)
            match(
                node("Class")
                    .withProperties("id", parameter("id"))
                    .relationshipTo(root, SUBCLASS_OF)
                    .unbounded()
            ).where(
                root.relationshipTo(node("Class"), SUBCLASS_OF)
                    .asCondition()
                    .not()
            ).returning(root)
        }
        .withParameters("id" to id.value)
        .mappedBy(ClassMapper("r"))
        .one()

    override fun findAllRoots(pageable: Pageable): Page<Class> = CypherQueryBuilder(neo4jClient)
        .withCommonQuery {
            val root = node("Class").named("r")
            match(root)
                .where(
                    root.relationshipTo(node("Class"), SUBCLASS_OF)
                        .asCondition()
                        .not()
                )
        }
        .withQuery { commonQuery ->
            val r = name("r")
            commonQuery.with(r)
                .orderBy(r.property("id").ascending())
                .returning(r)
        }
        .countOver("r")
        .mappedBy(ClassMapper("r"))
        .fetch(pageable)

    override fun findClassHierarchy(id: ThingId, pageable: Pageable): Page<ClassHierarchyEntry> = CypherQueryBuilder(neo4jClient)
        .withCommonQuery {
            val p = name("p")
            val c = name("c")
            val classes = name("classes")
            val `class` = name("class")
            match(
                node("Class")
                    .named(c)
                    .withProperties("id", parameter("id"))
                    .relationshipTo(node("Class").named(p), SUBCLASS_OF)
                    .length(0, null)
            ).with(collect(p).add(collect(c)).`as`(classes))
                .unwind(classes)
                .`as`(`class`)
                .withDistinct(`class`)
        }
        .withQuery { commonQuery ->
            val p = name("p")
            val `class` = name("class")
            val parentId = name("parent_id")
            commonQuery
                .optionalMatch(
                    anyNode()
                        .named(`class`)
                        .relationshipTo(node("Class").named(p), SUBCLASS_OF)
                )
                .with(`class`.`as`(`class`), p.property("id").`as`(parentId))
                .orderBy(`class`.property("id").ascending())
                .returning(`class`, parentId)
        }
        .countOver("class")
        .withParameters("id" to id.value)
        .mappedBy { _, record -> ClassHierarchyEntry(record["class"].asNode().toClass(), record["parent_id"].toThingId()) }
        .fetch(pageable)

    override fun countClassInstances(id: ThingId): Long = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val r = name("r")
            val c = name("c")
            val ids = name("ids")
            val i = name("i")
            val label = name("label")
            val instance = node("Thing")
                .named(i)
            val idLiteral = parameter("id")
            match(
                node("Class")
                    .named(c)
                    .relationshipTo(
                        node("Class")
                            .named(r)
                            .withProperties("id", idLiteral),
                        SUBCLASS_OF
                    ).unbounded()
            ).with(collect(c.property("id")).add(idLiteral).`as`(ids))
                .match(instance)
                .where(any(label).`in`(labels(instance)).where(label.`in`(ids)))
                .returning(count(i))
        }
        .withParameters("id" to id.value)
        .fetchAs<Long>()
        .one()
        .orElse(0)

    override fun existsChild(id: ThingId, childId: ThingId): Boolean = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val c = node("Class")
                .named("c")
                .withProperties("id", literalOf<String>(childId.value))
            match(c)
                .returning(
                    exists(
                        c.relationshipTo(
                            node("Class")
                                .withProperties("id", literalOf<String>(id.value)),
                            SUBCLASS_OF
                        ).unbounded()
                    )
                )
        }
        .fetchAs<Boolean>()
        .one()
        .orElse(false)

    override fun existsChildren(id: ThingId): Boolean = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val c = node("Class")
                .named("c")
                .withProperties("id", literalOf<String>(id.value))
            match(c)
                .returning(exists(node("Class").relationshipTo(c, SUBCLASS_OF)))
        }
        .fetchAs<Boolean>()
        .one()
        .orElse(false)
}
