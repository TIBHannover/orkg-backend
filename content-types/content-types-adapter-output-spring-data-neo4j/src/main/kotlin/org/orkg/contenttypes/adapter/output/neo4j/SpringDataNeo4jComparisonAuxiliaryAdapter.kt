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
        return LabeledComparisonPathBuilder.buildTree(entries, rootIds, maxDepth)
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
                children = it.children.withLabels(it.id, predicateIdToEntry, templateIdToEntryMap),
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
                        collect(Cypher.mapOf("object", nextObjectName, "predicate_id", nextPredicateName, "children", nextChildrenName)).`as`(children),
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
                    .named(relName),
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
                        collect(Cypher.mapOf("object", nextObjectName, "predicate_id", nextPredicateIdName, "children", nextChildrenName)).`as`(childrenName),
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
            children = this["children"].asList { it.toNeo4jComparisonTableData() }.filterNotNull(),
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

    internal object LabeledComparisonPathBuilder {
        internal fun buildTree(entries: Set<Neo4jComparisonPathEntry>, rootIds: Set<ThingId>, maxDepth: Int): List<LabeledComparisonPath> {
            val roots = entries.filter { it.subjectId in rootIds }
            var parents = roots.map { entry ->
                ProtoLabeledComparisonPath(
                    id = entry.predicateId,
                    label = entry.predicateLabel,
                    description = entry.description,
                    type = entry.type,
                    parent = null,
                    children = mutableListOf(),
                    subjectIds = mutableSetOf(entry.subjectId),
                    objectIds = entry.objectIds.toMutableSet(),
                    sourceIds = mutableSetOf(entry.subjectId),
                )
            }
            val result = parents.toMutableList()
            val visited = rootIds.toMutableSet()
            var depth = maxDepth
            while (depth > 0 && parents.isNotEmpty()) {
                val children = mutableListOf<ProtoLabeledComparisonPath>()
                parents.forEach { parent ->
                    parent.children += (parent.objectIds - visited).flatMap { objectId -> entries.filter { it.subjectId == objectId } }
                        .groupBy { it.predicateId }
                        .map { (predicateId, entries) ->
                            val first = entries.first()
                            ProtoLabeledComparisonPath(
                                id = predicateId,
                                label = first.predicateLabel,
                                description = first.description,
                                type = first.type,
                                parent = parent,
                                children = mutableListOf(),
                                subjectIds = entries.mapTo(mutableSetOf()) { it.subjectId },
                                objectIds = entries.flatMapTo(mutableSetOf()) { it.objectIds },
                                sourceIds = parent.sourceIds,
                            )
                        }
                    children += parent.children
                }
                parents.forEach { parent -> visited += parent.objectIds }
                parents = children
                depth--
            }
            return result.mergeAndSort().map { it.toLabeledComparisonPath() }
        }

        private fun MutableList<ProtoLabeledComparisonPath>.mergeAndSort(): List<ProtoLabeledComparisonPath> {
            val roots = groupBy { it.id }
                .map { (predicateId, entries) ->
                    val first = entries.first()
                    ProtoLabeledComparisonPath(
                        id = predicateId,
                        label = first.label,
                        description = first.description,
                        type = first.type,
                        parent = null,
                        children = entries.flatMapTo(mutableListOf()) { it.children },
                        subjectIds = entries.flatMapTo(mutableSetOf()) { it.subjectIds },
                        objectIds = entries.flatMapTo(mutableSetOf()) { it.objectIds },
                        sourceIds = entries.flatMapTo(mutableSetOf()) { it.sourceIds },
                    )
                }
                .sortedWith(pathComparator)
            val visited = roots.flatMapTo(mutableSetOf()) { it.objectIds }
            var level = listOf(roots)
            while (level.isNotEmpty()) {
                val nextLevel = mutableListOf<List<ProtoLabeledComparisonPath>>()
                level.forEach { levelEntry ->
                    levelEntry.forEach { parent ->
                        val updatedChildren = parent.children
                            .filter { it.expandChildren }
                            .groupBy { it.id }
                            .map { (predicateId, entries) ->
                                val first = entries.first()
                                val objectIds = entries.flatMapTo(mutableSetOf()) { it.objectIds }
                                val expandChildren = (objectIds - visited).isNotEmpty()
                                ProtoLabeledComparisonPath(
                                    id = predicateId,
                                    label = first.label,
                                    description = first.description,
                                    type = first.type,
                                    parent = parent,
                                    children = mutableListOf(),
                                    subjectIds = entries.flatMapTo(mutableSetOf()) { it.subjectIds },
                                    objectIds = objectIds,
                                    sourceIds = parent.sourceIds,
                                    expandChildren = expandChildren,
                                )
                            }
                            .sortedWith(pathComparator)
                        parent.children = updatedChildren
                        nextLevel += parent.children
                    }
                }
                level.forEach { levelEntry ->
                    levelEntry.forEach { parent ->
                        visited += parent.subjectIds
                    }
                }
                level = nextLevel
            }
            return roots
        }

        private val pathComparator = Comparator.comparing<ProtoLabeledComparisonPath, String> {
            when (it.type) {
                ComparisonPath.Type.PREDICATE -> {
                    it.label.lowercase()
                }

                ComparisonPath.Type.ROSETTA_STONE_STATEMENT -> {
                    it.label.lowercase()
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

        private data class ProtoLabeledComparisonPath(
            val id: ThingId,
            val label: String,
            val description: String?,
            val type: ComparisonPath.Type,
            val parent: ProtoLabeledComparisonPath?,
            var children: List<ProtoLabeledComparisonPath>,
            val subjectIds: MutableSet<ThingId>,
            val objectIds: MutableSet<ThingId>,
            val sourceIds: MutableSet<ThingId>,
            var expandChildren: Boolean = true,
        ) {
            fun toLabeledComparisonPath(): LabeledComparisonPath =
                LabeledComparisonPath(
                    id = id,
                    label = label,
                    description = description,
                    sources = sourceIds.size,
                    type = type,
                    children = children.map { it.toLabeledComparisonPath() },
                )
        }
    }
}
