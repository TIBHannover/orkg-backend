package org.orkg.graph.adapter.output.neo4j

import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.neo4j.cypherdsl.core.Cypher.anyNode
import org.neo4j.cypherdsl.core.Cypher.collect
import org.neo4j.cypherdsl.core.Cypher.count
import org.neo4j.cypherdsl.core.Cypher.countDistinct
import org.neo4j.cypherdsl.core.Cypher.elementId
import org.neo4j.cypherdsl.core.Cypher.exists
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.parameter
import org.neo4j.cypherdsl.core.Cypher.unwind
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilderFactory
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.SingleQueryBuilder.fetchAs
import org.orkg.common.neo4jdsl.SingleQueryBuilder.mappedBy
import org.orkg.graph.domain.ChildClass
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.ClassHierarchyEntry
import org.orkg.graph.domain.ClassSubclassRelation
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRelationRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

private const val SUBCLASS_OF = "SUBCLASS_OF"
private const val INSTANCE_OF = "INSTANCE_OF"

@Component
@TransactionalOnNeo4j
class SpringDataNeo4jClassHierarchyAdapter(
    private val cypherQueryBuilderFactory: CypherQueryBuilderFactory,
) : ClassHierarchyRepository, ClassRelationRepository {

    override fun save(classRelation: ClassSubclassRelation) {
        cypherQueryBuilderFactory.newBuilder()
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
        cypherQueryBuilderFactory.newBuilder()
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
                    ).returning(elementId(r))
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
        cypherQueryBuilderFactory.newBuilder()
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
        cypherQueryBuilderFactory.newBuilder()
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

    override fun findChildren(id: ThingId, pageable: Pageable): Page<ChildClass> = cypherQueryBuilderFactory.newBuilder()
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
            val g = node("Class").named("g")
            commonQuery
                .optionalMatch(g.relationshipTo(anyNode().named(c)))
                .returning(c, count(g).`as`("childCount"))
                .orderBy(c.property("id").ascending())
        }
        .countOver("c")
        .withParameters("id" to id.value)
        .mappedBy { _, record -> ChildClass(record["c"].asNode().toClass(), record["childCount"].asLong()) }
        .fetch(pageable)

    override fun findParent(id: ThingId): Optional<Class> = cypherQueryBuilderFactory.newBuilder()
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

    override fun findRoot(id: ThingId): Optional<Class> = cypherQueryBuilderFactory.newBuilder()
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

    override fun findAllRoots(pageable: Pageable): Page<Class> = cypherQueryBuilderFactory.newBuilder()
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

    override fun findClassHierarchy(id: ThingId, pageable: Pageable): Page<ClassHierarchyEntry> = cypherQueryBuilderFactory.newBuilder()
        .withCommonQuery {
            val c = name("c")
            val classes = name("classes")
            val `class` = name("class")
            match(
                node("Class")
                    .withProperties("id", parameter("id"))
                    .relationshipTo(node("Class").named(c), SUBCLASS_OF)
                    .length(0, null)
            ).with(collect(c).`as`(classes))
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

    override fun countClassInstances(id: ThingId): Long = cypherQueryBuilderFactory.newBuilder()
        .withQuery {
            val node = node("Resource")
            match(
                node.relationshipTo(node("Class"), INSTANCE_OF)
                    .relationshipTo(node("Class").withProperties("id", parameter("id")), SUBCLASS_OF)
                    .length(0, null)
            ).returning(countDistinct(node))
        }
        .withParameters("id" to id.value)
        .fetchAs<Long>()
        .one()
        .orElse(0)

    override fun existsChild(id: ThingId, childId: ThingId): Boolean = cypherQueryBuilderFactory.newBuilder()
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

    override fun existsChildren(id: ThingId): Boolean = cypherQueryBuilderFactory.newBuilder()
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
