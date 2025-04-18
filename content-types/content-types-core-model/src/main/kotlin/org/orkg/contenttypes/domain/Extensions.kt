package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.common.toIRIOrNull
import org.orkg.contenttypes.domain.identifiers.Identifier
import org.orkg.contenttypes.domain.identifiers.Identifiers
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing

fun Iterable<GeneralStatement>.wherePredicate(predicateId: ThingId) =
    filter { it.predicate.id == predicateId }

fun List<GeneralStatement>.objectIdsAndLabel(): List<ObjectIdAndLabel> =
    map { it.objectIdAndLabel() }.sortedBy { it.id }

fun GeneralStatement.objectIdAndLabel(): ObjectIdAndLabel =
    ObjectIdAndLabel(`object`.id, `object`.label)

fun List<GeneralStatement>.objects() = map { it.`object` }

fun List<GeneralStatement>.firstObjectLabel(): String? = firstOrNull()?.`object`?.label

fun List<GeneralStatement>.singleObjectLabel(): String? = singleOrNull()?.`object`?.label

fun List<GeneralStatement>.associateIdentifiers(identifiers: Set<Identifier>): Map<String, List<String>> =
    mapNotNull { statement ->
        identifiers.firstOrNull { statement.predicate.id == it.predicateId }
            ?.let { identifier -> identifier.id to statement.`object` }
    }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, value) -> value.map { it.label }.distinct().sorted() }

fun List<GeneralStatement>.withoutObjectsWithBlankLabels(): List<GeneralStatement> =
    filter { it.`object`.label.isNotBlank() }

inline val List<ObjectIdAndLabel>.ids get() = map { it.id }

inline val Set<ObjectIdAndLabel>.ids get() = mapTo(mutableSetOf()) { it.id }

inline val List<GeneralStatement>.ids get() = mapTo(mutableSetOf()) { it.id }

private fun Thing.toAuthor(statements: List<GeneralStatement>): Author =
    when (this) {
        is Resource -> toAuthor(statements.withoutObjectsWithBlankLabels())
        is Literal -> toAuthor()
        else -> throw IllegalStateException("""Cannot convert "$id" to author. This is a bug!""")
    }

private fun Resource.toAuthor(statements: List<GeneralStatement>): Author =
    Author(
        id = id,
        name = label,
        identifiers = statements.associateIdentifiers(Identifiers.author),
        homepage = statements.wherePredicate(Predicates.hasWebsite).firstObjectLabel()?.toIRIOrNull()
    )

private fun Literal.toAuthor(): Author =
    Author(
        id = null,
        name = label,
        identifiers = emptyMap(),
        homepage = null
    )

fun Map<ThingId, List<GeneralStatement>>.authors(subjectId: ThingId): List<Author> =
    this[subjectId]
        ?.singleOrNull { it.predicate.id == Predicates.hasAuthors }
        ?.let { this[it.`object`.id] }
        ?.filter { it.predicate.id == Predicates.hasListElement }
        ?.sortedBy { it.index }
        ?.objects()
        ?.filter { it is Resource || it is Literal }
        ?.map { it.toAuthor(this[it.id].orEmpty()) }
        .orEmpty()

fun Map<ThingId, List<GeneralStatement>>.legacyAuthors(subjectId: ThingId): List<Author> =
    this[subjectId]
        ?.filter { it.predicate.id == Predicates.hasAuthor }
        ?.sortedBy { it.createdAt }
        ?.objects()
        ?.filter { it is Resource || it is Literal }
        ?.map { it.toAuthor(this[it.id].orEmpty()) }
        .orEmpty()
