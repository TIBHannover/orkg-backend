package org.orkg.contenttypes.adapter.output.neo4j

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.function.BiFunction
import org.neo4j.cypherdsl.core.Cypher.call
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.unionAll
import org.neo4j.cypherdsl.core.Node
import org.neo4j.cypherdsl.core.RelationshipPattern
import org.neo4j.cypherdsl.core.StatementBuilder
import org.neo4j.cypherdsl.core.SymbolicName
import org.neo4j.driver.Record
import org.neo4j.driver.types.TypeSystem
import org.orkg.common.ContributorId
import org.orkg.contenttypes.domain.Certainty
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementVersion
import org.orkg.graph.adapter.output.neo4j.matchDistinct
import org.orkg.graph.adapter.output.neo4j.toResource
import org.orkg.graph.adapter.output.neo4j.toThing
import org.orkg.graph.adapter.output.neo4j.toThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Thing

private const val RELATED = "RELATED"
private val reservedRosettaStoneStatementLabels = setOf(
    Classes.thing,
    Classes.resource,
    Classes.rosettaStoneStatement,
    Classes.latestVersion
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
                    modifiable = node.modifiable,
                    deletedBy = metadata["deleted_by"].takeUnless { it.isNull }
                        ?.asString()
                        ?.let(::ContributorId),
                    deletedAt = metadata["deleted_at"].takeUnless { it.isNull }
                        ?.asString()
                        ?.let { OffsetDateTime.parse(it, ISO_OFFSET_DATE_TIME) }
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
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
    matchDistinct(node("LiteratureListPublished", "LatestVersion").named(symbolicName), patternGenerator)

private fun matchUnpublishedLiteratureLists(
    symbolicName: SymbolicName,
    patternGenerator: (Node) -> Collection<RelationshipPattern>
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
    matchDistinct(node("LiteratureList").named(symbolicName), patternGenerator)

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
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
    matchDistinct(node("SmartReviewPublished", "LatestVersion").named(symbolicName), patternGenerator)

private fun matchUnpublishedSmartReviews(
    symbolicName: SymbolicName,
    patternGenerator: (Node) -> Collection<RelationshipPattern>
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
    matchDistinct(node("SmartReview").named(symbolicName), patternGenerator)

internal fun matchComparison(
    node: SymbolicName,
    patternGenerator: (Node) -> Collection<RelationshipPattern>,
    published: Boolean? = null
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
    when (published) {
        true -> matchPublishedComparisons(node, patternGenerator)
        false -> matchUnpublishedComparisons(node, patternGenerator)
        else -> call(
            unionAll(
                matchPublishedComparisons(node, patternGenerator).returning(node).build(),
                matchUnpublishedComparisons(node, patternGenerator).returning(node).build()
            )
        ).with(node)
    }

private fun matchPublishedComparisons(
    symbolicName: SymbolicName,
    patternGenerator: (Node) -> Collection<RelationshipPattern>
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
    matchDistinct(node("ComparisonPublished", "LatestVersion").named(symbolicName), patternGenerator)

private fun matchUnpublishedComparisons(
    symbolicName: SymbolicName,
    patternGenerator: (Node) -> Collection<RelationshipPattern>
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
    matchDistinct(node("Comparison").named(symbolicName), patternGenerator)
