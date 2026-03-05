package org.orkg.contenttypes.adapter.output.neo4j

import org.neo4j.cypherdsl.core.Cypher
import org.neo4j.cypherdsl.core.Cypher.collect
import org.neo4j.cypherdsl.core.Cypher.listOf
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.optionalMatch
import org.neo4j.cypherdsl.core.Cypher.parameter
import org.neo4j.cypherdsl.core.Cypher.union
import org.neo4j.cypherdsl.core.Cypher.unwind
import org.neo4j.cypherdsl.core.Node
import org.neo4j.cypherdsl.core.Statement
import org.neo4j.driver.Value
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilderFactory
import org.orkg.common.neo4jdsl.QueryCache.Uncached
import org.orkg.common.neo4jdsl.SingleQueryBuilder.fetchAs
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jComparisonAuxiliaryRepository
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jComparisonPathEntry
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jComparisonPathLabelEntry
import org.orkg.contenttypes.domain.ComparisonColumnData
import org.orkg.contenttypes.domain.ComparisonPath
import org.orkg.contenttypes.domain.ComparisonTableValue
import org.orkg.contenttypes.domain.LabeledComparisonPath
import org.orkg.contenttypes.domain.SimpleComparisonPath
import org.orkg.contenttypes.output.ComparisonAuxiliaryRepository
import org.orkg.graph.adapter.output.neo4j.toThing
import org.orkg.graph.adapter.output.neo4j.toThingId
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Thing
import org.springframework.stereotype.Component

private const val RELATED = "RELATED"

@Component
class SpringDataNeo4jComparisonAuxiliaryAdapter(
    private val neo4jRepository: Neo4jComparisonAuxiliaryRepository,
    private val cypherQueryBuilderFactory: CypherQueryBuilderFactory,
) : ComparisonAuxiliaryRepository {
    override fun findAllLabeledComparisonPathsByComparisonId(id: ThingId, maxDepth: Int): List<LabeledComparisonPath> {
        val entries = neo4jRepository.findAllComparisonTablePredicatePathsByComparisonId(id, maxDepth)
        val rootIds = entries.filter { entry -> entries.none { entry.subjectId in it.objectIds } }.map { it.subjectId }.toSet()
        return buildTree(entries, rootIds, maxDepth)
    }

    private fun buildTree(
        entries: Set<Neo4jComparisonPathEntry>,
        rootIds: Set<ThingId>,
        depth: Int,
        parents: Set<ThingId> = emptySet(),
    ): List<LabeledComparisonPath> =
        entries.filter { entry -> entry.subjectId in rootIds }
            .map { entry ->
                LabeledComparisonPath(
                    id = entry.predicateId,
                    label = entry.predicateLabel,
                    description = entry.description,
                    type = entry.type,
                    children = when {
                        // filter object ids if present in parent tree?
                        depth > 1 -> buildTree(entries, entry.objectIds.toSet() - parents, depth - 1, parents + rootIds)

                        else -> emptyList()
                    },
                )
            }
            .merge()

    private fun List<LabeledComparisonPath>.merge(): List<LabeledComparisonPath> =
        when {
            isEmpty() || (size == 1 && first().children.isEmpty()) -> this

            size == 1 -> map { it.copy(children = it.children.merge()) }

            else -> groupBy { it.id }.values.map { paths -> paths.reduce { a, b -> a.merge(b) } }.sortedBy {
                when (it.type) {
                    ComparisonPath.Type.PREDICATE -> {
                        it.label
                    }

                    ComparisonPath.Type.ROSETTA_STONE_STATEMENT -> {
                        it.label
                    }

                    ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE -> {
                        val id = it.id.value
                        when {
                            id == "hasSubjectPosition" -> "0"

                            // always sort rs subject to first position
                            id.startsWith("hasObjectPosition") -> id

                            // sort rs objects by id
                            else -> throw IllegalStateException("""Illegal id "$id" for rosetta stone statement value while fetching comparison paths. This is a bug.""")
                        }
                    }
                }
            }
        }

    private fun LabeledComparisonPath.merge(other: LabeledComparisonPath): LabeledComparisonPath {
        assert(type == other.type)
        assert(description == other.description)
        return LabeledComparisonPath(
            id = id,
            label = label,
            type = type,
            description = description,
            children = (children + other.children).merge()
        )
    }

    override fun findAllLabeledComparisonPathsBySimpleComparionPaths(paths: List<SimpleComparisonPath>): List<LabeledComparisonPath> {
        val predicateIds = paths.getIds { it.type == ComparisonPath.Type.PREDICATE }.toSet()
        val rosettaStoneTemplateIds = paths.getIds { it.type == ComparisonPath.Type.ROSETTA_STONE_STATEMENT }.toSet()
        val entries = neo4jRepository.findComparisonPathLabelsByThingIdsAndRosettaStoneTemplateIds(predicateIds, rosettaStoneTemplateIds)
        val predicateIdToEntry = entries.associateBy { it.predicateId }
        val templateIdToEntryMap = entries.filter { it.templateId != null }
            .groupBy { it.templateId!! }
            .mapValues { (_, value) -> value.associateBy { it.predicateId } }
        return paths.withLabels(null, predicateIdToEntry, templateIdToEntryMap)
    }

    private fun List<SimpleComparisonPath>.withLabels(
        parentId: ThingId?,
        predicateIdToEntry: Map<ThingId, Neo4jComparisonPathLabelEntry>,
        templateIdToEntryMap: Map<ThingId, Map<ThingId, Neo4jComparisonPathLabelEntry>>,
    ): List<LabeledComparisonPath> =
        mapNotNull {
            val labelEntry = when (it.type) {
                ComparisonPath.Type.PREDICATE, ComparisonPath.Type.ROSETTA_STONE_STATEMENT -> {
                    predicateIdToEntry[it.id]
                        ?.takeIf { entry -> entry.type == it.type }
                        ?: return@mapNotNull null
                }

                ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE -> {
                    templateIdToEntryMap[parentId]
                        ?.get(it.id)
                        ?.takeIf { entry -> entry.type == it.type }
                        ?: return@mapNotNull null
                }
            }
            LabeledComparisonPath(
                id = it.id,
                label = labelEntry.predicateLabel,
                description = labelEntry.description,
                type = it.type,
                children = it.children.withLabels(it.id, predicateIdToEntry, templateIdToEntryMap)
            )
        }

    private fun List<SimpleComparisonPath>.getIds(predicate: (SimpleComparisonPath) -> Boolean): List<ThingId> =
        filter(predicate).flatMap { it.children.getIds(predicate) + it.id }

    override fun findComparisonColumnDataByRootIdsAndPaths(
        rootIds: List<ThingId>,
        paths: List<ComparisonPath<*>>,
    ): Map<ThingId, ComparisonColumnData> = cypherQueryBuilderFactory.newBuilder(Uncached)
        .withQuery {
            val ids = parameter("ids")
            val id = name("id")
            val root = node("Thing").named("n0")
            val paper = node("Paper").named("paper")
            val nextPredicateName = name("pid1")
            val nextObjectName = name("object1")
            val nextChildrenName = name("children1")
            val children = name("children")
            val match = unwind(ids).`as`(id)
                .match(root.withProperties("id", id))
                .optionalMatch(root.relationshipFrom(paper, RELATED).withProperties("predicate_id", literalOf<String>(Predicates.hasContribution.value)))
            val filteredPaths = paths.filter { it.type == ComparisonPath.Type.PREDICATE }
            if (filteredPaths.isNotEmpty()) {
                match.call(buildQueryTree(filteredPaths, root, 1), root)
                    .returning(
                        root.`as`("root"),
                        paper.asExpression(),
                        collect(Cypher.mapOf("object", nextObjectName, "predicate_id", nextPredicateName, "children", nextChildrenName)).`as`(children)
                    )
            } else {
                match.returning(root.`as`("root"), paper.asExpression(), listOf().`as`(children))
            }
        }
        .withParameters("ids" to rootIds.map { it.value })
        .fetchAs<Pair<ThingId, ComparisonColumnData>>()
        .mappedBy { _, record ->
            val root = record["root"].asNode().toThing()
            val paper = record["paper"].takeUnless { it.isNull }?.asNode()?.toThing()
            val children = record["children"].asList { it.toNeo4jComparisonTableData() }.filterNotNull().toMap()
            val title = paper ?: root
            val subtitle = root.takeIf { paper != null }
            root.id to ComparisonColumnData(title, subtitle, children)
        }
        .all()
        .toMap()

    private fun buildQueryTree(
        comparisonPaths: List<ComparisonPath<*>>,
        subjectNode: Node,
        depth: Int,
    ): Statement {
        val relName = name("r$depth")
        val predicateId = name("pid$depth")
        val childrenName = name("children$depth")
        val objectName = name("object$depth")
        val objectNodeName = name("n$depth")
        val objectNode = node("Thing").named(objectNodeName)
        val mappedComparisonPaths = comparisonPaths.map { comparisonPath ->
            val nextPredicateIdName = name("pid${depth + 1}")
            val nextObjectName = name("object${depth + 1}")
            val nextChildrenName = name("children${depth + 1}")
            val match = optionalMatch(
                subjectNode.relationshipTo(objectNode, RELATED)
                    .withProperties("predicate_id", literalOf<String>(comparisonPath.id.value))
                    .named(relName)
            )
            val filteredChildren = comparisonPath.children.filter { it.type == ComparisonPath.Type.PREDICATE }
            if (filteredChildren.isEmpty()) {
                match.returning(objectNodeName.`as`(objectName), relName.property("predicate_id").`as`(predicateId), listOf().`as`(childrenName))
                    .build()
            } else {
                match.call(buildQueryTree(filteredChildren, objectNode, depth + 1), objectNodeName)
                    .returning(
                        objectNodeName.`as`(objectName),
                        relName.property("predicate_id").`as`(predicateId),
                        collect(Cypher.mapOf("object", nextObjectName, "predicate_id", nextPredicateIdName, "children", nextChildrenName)).`as`(childrenName)
                    )
                    .build()
            }
        }
        return if (mappedComparisonPaths.size >= 2) union(mappedComparisonPaths) else mappedComparisonPaths.first()
    }

    private fun Value.toNeo4jComparisonTableData(): Neo4jComparisonTableData? {
        if (this["object"].isNull) return null
        return Neo4jComparisonTableData(
            thing = this["object"].asNode().toThing(),
            predicateId = this["predicate_id"].toThingId()!!,
            children = this["children"].asList { it.toNeo4jComparisonTableData() }.filterNotNull()
        )
    }

    private fun List<Neo4jComparisonTableData>.toMap(): Map<ThingId, List<ComparisonTableValue>> =
        groupBy { it.predicateId }.mapValues { (_, value) ->
            value.map { ComparisonTableValue(it.thing, it.children.toMap()) }
                .sortedBy { it.value.label.lowercase() }
        }

    data class Neo4jComparisonTableData(
        val thing: Thing,
        val predicateId: ThingId,
        val children: List<Neo4jComparisonTableData>,
    )
}
