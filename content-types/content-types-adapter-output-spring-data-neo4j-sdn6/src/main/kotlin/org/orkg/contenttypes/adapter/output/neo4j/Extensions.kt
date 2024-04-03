package org.orkg.contenttypes.adapter.output.neo4j

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
import org.orkg.graph.adapter.output.neo4j.match
import org.orkg.graph.domain.Predicates

private const val RELATED = "RELATED"

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
