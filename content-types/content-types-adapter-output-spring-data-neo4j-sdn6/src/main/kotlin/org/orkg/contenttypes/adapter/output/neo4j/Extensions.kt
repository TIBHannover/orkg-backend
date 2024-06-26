package org.orkg.contenttypes.adapter.output.neo4j

import java.util.function.BiFunction
import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Cypher.anyNode
import org.neo4j.cypherdsl.core.Cypher.call
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.unionAll
import org.neo4j.cypherdsl.core.Cypher.valueAt
import org.neo4j.cypherdsl.core.Functions.collect
import org.neo4j.cypherdsl.core.Node
import org.neo4j.cypherdsl.core.Predicates.exists
import org.neo4j.cypherdsl.core.RelationshipPattern
import org.neo4j.cypherdsl.core.StatementBuilder
import org.neo4j.cypherdsl.core.SymbolicName
import org.neo4j.driver.Record
import org.neo4j.driver.types.TypeSystem
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Certainty
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementVersion
import org.orkg.graph.adapter.output.neo4j.match
import org.orkg.graph.adapter.output.neo4j.toResource
import org.orkg.graph.adapter.output.neo4j.toThing
import org.orkg.graph.adapter.output.neo4j.toThingId
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Thing

private const val RELATED = "RELATED"
private val reservedRosettaStoneStatementLabels = setOf(
    ThingId("Thing"),
    ThingId("Resource"),
    ThingId("RosettaStoneStatement"),
    ThingId("LatestVersion")
)

data class RosettaStoneStatementMapper(
    val latest: String = "latest",
    val templateId: String = "templateId",
    val contextId: String = "contextId",
    val versions: String = "versions",
) : BiFunction<TypeSystem, Record, RosettaStoneStatement> {
    constructor(
        latest: SymbolicName,
        templateId: SymbolicName,
        context: SymbolicName,
        versions: SymbolicName
    ) : this(latest.value, templateId.value, context.value, versions.value)

    override fun apply(typeSystem: TypeSystem, record: Record): RosettaStoneStatement {
        val latest = record[latest].asNode().toResource()
        val templateId = record[templateId].toThingId()!!
        val contextId = record[contextId].takeUnless { it.isNull }?.toThingId()
        val versions = record[versions].asList { it }
            .map { version ->
                val node = version[0].asNode().toResource()
                val metadata = version[1].asNode()
                val subjects = version[2] // (thing, index)
                    .asList { SubjectNode(it[0].asNode().toThing(), it[1].asInt()) }
                    .sortedBy { it.index }
                    .map { it.thing }
                val objects = run {
                    val objectNodes = version[3] // (thing, index, position)
                        .asList { ObjectNode(it[0].asNode().toThing(), it[1].asInt(), it[2].asInt()) }
                        .groupBy { it.position }
                    (0 until metadata["object_count"].asInt()).map { position ->
                        objectNodes[position].orEmpty()
                            .sortedBy { it.index }
                            .map { it.thing }
                    }
                }
                metadata["version"].asLong() to RosettaStoneStatementVersion(
                    id = node.id,
                    formattedLabel = FormattedLabel.of(metadata["formatted_label"].asString()),
                    subjects = subjects,
                    objects = objects,
                    createdAt = node.createdAt,
                    createdBy = node.createdBy,
                    certainty = metadata["certainty"].let { Certainty.valueOf(it.asString()) },
                    negated = metadata["negated"].asBoolean(),
                    observatories = listOf(node.observatoryId),
                    organizations = listOf(node.organizationId),
                    extractionMethod = node.extractionMethod,
                    visibility = node.visibility,
                    unlistedBy = node.unlistedBy,
                    modifiable = node.modifiable
                )
            }
            .sortedBy { it.first }
            .map { it.second }
        return RosettaStoneStatement(
            id = latest.id,
            contextId = contextId,
            templateId = templateId,
            templateTargetClassId = (latest.classes - reservedRosettaStoneStatementLabels).single(),
            label = latest.label,
            versions = versions,
            observatories = listOf(latest.observatoryId),
            organizations = listOf(latest.organizationId),
            extractionMethod = latest.extractionMethod,
            visibility = latest.visibility,
            unlistedBy = latest.unlistedBy,
            modifiable = latest.modifiable
        )
    }

    data class SubjectNode(
        val thing: Thing,
        val index: Int
    )

    data class ObjectNode(
        val thing: Thing,
        val index: Int,
        val position: Int
    )
}

internal fun matchLiteratureList(
    node: SymbolicName,
    patternGenerator: (Node) -> Collection<RelationshipPattern>,
    published: Boolean? = null
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
    when (published) {
        true -> matchPublishedLiteratureLists(node, patternGenerator)
        false -> matchUnpublishedLiteratureLists(node, patternGenerator)
        else -> call(
            unionAll(
                matchPublishedLiteratureLists(node, patternGenerator).returning(node).build(),
                matchUnpublishedLiteratureLists(node, patternGenerator).returning(node).build()
            )
        ).with(node)
    }

private fun matchPublishedLiteratureLists(
    symbolicName: SymbolicName,
    patternGenerator: (Node) -> Collection<RelationshipPattern>
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere {
    val llp = node("LiteratureListPublished").named("llp")
    val lll = name("lll")
    return match(
        llp.relationshipFrom(node("LiteratureList").named(lll), RELATED)
            .withProperties("predicate_id", literalOf<String>(Predicates.hasPublishedVersion.value))
    ).with(
        lll.asExpression(),
        valueAt(call("apoc.coll.sortNodes").withArgs(collect(llp), literalOf<String>("created_at")).asFunction(), 0).`as`(symbolicName)
    ).let {
        val patterns = patternGenerator(anyNode().named(symbolicName))
        if (patterns.isNotEmpty()) {
            it.where(patterns.map(::exists).reduceOrNull(Condition::and)!!).with(symbolicName)
        } else {
            it
        }
    }
}

private fun matchUnpublishedLiteratureLists(
    symbolicName: SymbolicName,
    patternGenerator: (Node) -> Collection<RelationshipPattern>
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
    match(node("LiteratureList").named(symbolicName), patternGenerator)

internal fun matchSmartReview(
    node: SymbolicName,
    patternGenerator: (Node) -> Collection<RelationshipPattern>,
    published: Boolean? = null
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
    when (published) {
        true -> matchPublishedSmartReviews(node, patternGenerator)
        false -> matchUnpublishedSmartReviews(node, patternGenerator)
        else -> call(
            unionAll(
                matchPublishedSmartReviews(node, patternGenerator).returning(node).build(),
                matchUnpublishedSmartReviews(node, patternGenerator).returning(node).build()
            )
        ).with(node)
    }

private fun matchPublishedSmartReviews(
    symbolicName: SymbolicName,
    patternGenerator: (Node) -> Collection<RelationshipPattern>
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere {
    val srp = node("SmartReviewPublished").named("srp")
    val srl = name("srl")
    return match(
        srp.relationshipFrom(node("SmartReview").named(srl), RELATED)
            .withProperties("predicate_id", literalOf<String>(Predicates.hasPublishedVersion.value))
    ).with(
        srl.asExpression(),
        valueAt(call("apoc.coll.sortNodes").withArgs(collect(srp), literalOf<String>("created_at")).asFunction(), 0).`as`(symbolicName)
    ).let {
        val patterns = patternGenerator(anyNode().named(symbolicName))
        if (patterns.isNotEmpty()) {
            it.where(patterns.map(::exists).reduceOrNull(Condition::and)!!).with(symbolicName)
        } else {
            it
        }
    }
}

private fun matchUnpublishedSmartReviews(
    symbolicName: SymbolicName,
    patternGenerator: (Node) -> Collection<RelationshipPattern>
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
    match(node("SmartReview").named(symbolicName), patternGenerator)
