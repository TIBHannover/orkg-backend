package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassSubclassRelation
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository.ChildClass
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository.ClassHierarchyEntry
import eu.tib.orkg.prototype.statements.spi.ClassRelationRepository
import java.time.format.DateTimeFormatter.*
import java.util.*
import org.neo4j.cypherdsl.core.Cypher.anyNode
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.parameter
import org.neo4j.cypherdsl.core.Cypher.unwind
import org.neo4j.cypherdsl.core.Functions.*
import org.neo4j.cypherdsl.core.Predicates.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.stereotype.Component

private const val SUBCLASS_OF = "SUBCLASS_OF"

@Component
class SpringDataNeo4jClassHierarchyAdapter(
    override val neo4jClient: Neo4jClient
) : SpringDataNeo4jAdapter(neo4jClient), ClassHierarchyRepository, ClassRelationRepository {

    override fun save(classRelation: ClassSubclassRelation) {
        val child = node("Class")
        val parent = node("Class")
        val query = match(child)
            .where(
                child.property("class_id").eq(literalOf<String>(classRelation.child.id.value))
            ).match(parent).where(
                parent.property("class_id").eq(literalOf<String>(classRelation.parent.id.value))
            ).create(
                child.relationshipTo(parent, SUBCLASS_OF).withProperties(
                    "created_by", literalOf<String>(classRelation.createdBy.value.toString()),
                    "created_at", literalOf<String>(classRelation.createdAt.format(ISO_OFFSET_DATE_TIME))
                )
            ).build()
        neo4jClient.query(query.cypher).run()
    }

    override fun saveAll(classRelations: Set<ClassSubclassRelation>) {
        val child = node("Class")
        val parent = node("Class")
        val rows = parameter("rows")
        val row = name("row")
        val r = child.relationshipTo(parent, SUBCLASS_OF).named("r")
        val query = unwind(rows).`as`(row)
            .with(row)
            .match(child).where(
                child.property("class_id").eq(row.property("child_id"))
            ).match(parent).where(
                parent.property("class_id").eq(row.property("parent_id"))
            ).create(
                r.withProperties(
                    "created_by", row.property("created_by"),
                    "created_at", row.property("created_at")
                )
            ).returning(id(r))
            .build()
        neo4jClient.query(query.cypher)
            .bindAll(mapOf(
                "rows" to classRelations.map {
                    mapOf<String, String>(
                        "child_id" to it.child.id.value,
                        "parent_id" to it.parent.id.value,
                        "created_by" to it.createdBy.value.toString(),
                        "created_at" to it.createdAt.format(ISO_OFFSET_DATE_TIME)
                    )
                }
            )).run()
    }

    override fun deleteByChildClassId(childId: ThingId) {
        val r = name("r")
        val query = match(
                node("Class")
                    .withProperties("class_id", literalOf<String>(childId.value))
                    .relationshipTo(node("Class"), SUBCLASS_OF)
                    .named(r)
            ).delete(r)
            .build()
        neo4jClient.query(query.cypher).run()
    }

    override fun deleteAll() {
        val r = name("r")
        val query = match(
                node("Class")
                    .relationshipTo(node("Class"), SUBCLASS_OF)
                    .named(r)
            ).delete(r)
            .build()
        neo4jClient.query(query.cypher).run()
    }

    override fun findChildren(id: ThingId, pageable: Pageable): Page<ChildClass> {
        val c = name("c")
        val p = name("p")
        val g =  node("Class")
            .named("g")
        val childCount = name("childCount")
        val match = match(
                node("Class")
                    .named(c)
                    .relationshipTo(
                        node("Class")
                            .named(p)
                            .withProperties("class_id", literalOf<String>(id.value)),
                        SUBCLASS_OF
                    )
            )
        val query = match
            .optionalMatch(
                g.relationshipTo(anyNode().named(c))
            ).returning(c, count(g).`as`(childCount))
            .orderBy(c.property("class_id").ascending())
            .build(pageable)
        val countQuery = match
            .returning(count(c))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(ChildClass::class.java)
            .mappedBy { _, record -> ChildClass(record[c].asNode().toClass(), record[childCount].asLong()) }
            .paged(pageable, countQuery)
    }

    override fun findParent(id: ThingId): Optional<Class> {
        val p = name("p")
        val query = match(
                node("Class")
                    .withProperties("class_id", literalOf<String>(id.value))
                    .relationshipTo(anyNode("Class").named(p), SUBCLASS_OF)
            ).returning(p)
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(Class::class.java)
            .mappedBy(ClassMapper(p))
            .one()
    }

    override fun findRoot(id: ThingId): Optional<Class> {
        val r = name("r")
        val root = anyNode("Class")
            .named(r)
        val query = match(
                node("Class")
                    .withProperties("class_id", literalOf<String>(id.value))
                    .relationshipTo(root, SUBCLASS_OF)
                    .unbounded()
            ).where(
                root.relationshipTo(node("Class"), SUBCLASS_OF)
                    .asCondition()
                    .not()
            ).returning(root)
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(Class::class.java)
            .mappedBy(ClassMapper(r))
            .one()
    }

    override fun findAllRoots(pageable: Pageable): Page<Class> {
        val r = name("r")
        val root = node("Class").named(r)
        val match = match(root)
            .where(
                root.relationshipTo(node("Class"), SUBCLASS_OF)
                    .asCondition()
                    .not()
            )
        val query = match
            .returning(root)
            .orderBy(root.property("class_id").ascending())
            .build(pageable)
        val countQuery = match
            .returning(count(root))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(Class::class.java)
            .mappedBy(ClassMapper(r))
            .paged(pageable, countQuery)
    }

    override fun findClassHierarchy(id: ThingId, pageable: Pageable): Page<ClassHierarchyEntry> {
        val p = name("p")
        val c = name("c")
        val classes = name("classes")
        val `class` = name("class")
        val parentId = name("parent_id")
        val match = match(
                node("Class")
                    .named(c)
                    .withProperties("class_id", literalOf<String>(id.value))
                    .relationshipTo(node("Class").named(p), SUBCLASS_OF)
                    .length(0, null)
            ).with(collect(p).add(c).`as`(classes))
            .unwind(classes)
            .`as`(`class`)
            .withDistinct(`class`)
        val query = match
            .optionalMatch(
                anyNode()
                    .named(`class`)
                    .relationshipTo(node("Class").named(p), SUBCLASS_OF)
            )
            .returning(`class`, p.property("class_id").`as`(parentId))
            .orderBy(`class`.property("class_id").ascending())
            .build(pageable)
        val countQuery = match
            .returning(count(`class`))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(ClassHierarchyEntry::class.java)
            .mappedBy { _, record -> ClassHierarchyEntry(record[`class`].asNode().toClass(), record[parentId].toThingId()) }
            .paged(pageable, countQuery)
    }

    override fun countClassInstances(id: ThingId): Long {
        val r = name("r")
        val c = name("c")
        val ids = name("ids")
        val i = name("i")
        val label = name("label")
        val instance = node("Thing")
            .named(i)
        val idLiteral = literalOf<String>(id.value)
        val query = match(
                node("Class")
                    .named(c)
                    .relationshipTo(
                        node("Class")
                            .named(r)
                            .withProperties("class_id", idLiteral),
                        SUBCLASS_OF
                    ).unbounded()
            ).with(collect(c.property("class_id")).add(idLiteral).`as`(ids))
            .match(instance)
            .where(any(label).`in`(labels(instance)).where(label.`in`(ids)))
            .returning(count(i))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs<Long>()
            .one() ?: 0
    }

    override fun existsChild(id: ThingId, childId: ThingId): Boolean {
        val c = node("Class")
            .named("c")
            .withProperties("class_id", literalOf<String>(childId.value))
        val query = match(c)
            .returning(
                exists(
                    c.relationshipTo(
                        node("Class")
                            .withProperties("class_id", literalOf<String>(id.value)),
                        SUBCLASS_OF
                    ).unbounded()
                )
            ).build()
        return neo4jClient.query(query.cypher)
            .fetchAs<Boolean>()
            .one() ?: false
    }

    override fun existsChildren(id: ThingId): Boolean {
        val c = node("Class")
            .named("c")
            .withProperties("class_id", literalOf<String>(id.value))
        val query = match(c)
            .returning(exists(node("Class").relationshipTo(c, SUBCLASS_OF)))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs<Boolean>()
            .one() ?: false
    }
}
