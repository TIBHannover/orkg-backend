package org.orkg.graph.adapter.output.neo4j

import org.apache.lucene.queryparser.classic.QueryParser
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.jetbrains.annotations.Contract
import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Cypher.call
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.noCondition
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.toLower
import org.neo4j.cypherdsl.core.ExposesWith
import org.neo4j.cypherdsl.core.Expression
import org.neo4j.cypherdsl.core.FunctionInvocation
import org.neo4j.cypherdsl.core.PatternElement
import org.neo4j.cypherdsl.core.SortItem
import org.neo4j.cypherdsl.core.StatementBuilder
import org.neo4j.cypherdsl.core.SymbolicName
import org.neo4j.driver.Record
import org.neo4j.driver.Value
import org.neo4j.driver.types.Node
import org.neo4j.driver.types.TypeSystem
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.StringReader
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import org.orkg.graph.output.PredicateRepository
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.function.BiFunction

val reservedLabels = setOf(
    Classes.literal,
    Classes.`class`,
    Classes.predicate,
    Classes.resource,
    Classes.thing
)

val reservedLabelsAsStrings = reservedLabels.map { it.value }

data class StatementMapper(
    val predicateRepository: PredicateRepository,
    val subject: String = "sub",
    val relation: String = "rel",
    val `object`: String = "obj",
) : BiFunction<TypeSystem, Record, GeneralStatement> {
    constructor(
        predicateRepository: PredicateRepository,
        subject: SymbolicName,
        relation: SymbolicName,
        `object`: SymbolicName,
    ) : this(predicateRepository, subject.value, relation.value, `object`.value)

    override fun apply(typeSystem: TypeSystem, record: Record): GeneralStatement {
        val relation = record[relation]
        return GeneralStatement(
            id = StatementId(relation["statement_id"].asString()),
            // This could be fetched directly in findByXYZ
            predicate = relation["predicate_id"].toThingId()!!.let {
                predicateRepository.findById(it).orElseThrow { PredicateNotFound(it) }
            },
            createdAt = relation["created_at"].toOffsetDateTime(),
            createdBy = relation["created_by"].toContributorId(),
            subject = record[subject].asNode().toThing(),
            `object` = record[`object`].asNode().toThing(),
            index = relation["index"].asNullableInt(),
            modifiable = relation["modifiable"].asBoolean()
        )
    }
}

data class LiteralMapper(val name: String) : BiFunction<TypeSystem, Record, Literal> {
    constructor(symbolicName: SymbolicName) : this(symbolicName.value)

    override fun apply(typeSystem: TypeSystem, record: Record) = record[name].asNode().toLiteral()
}

data class ResourceMapper(val name: String) : BiFunction<TypeSystem, Record, Resource> {
    constructor(symbolicName: SymbolicName) : this(symbolicName.value)

    override fun apply(typeSystem: TypeSystem, record: Record) = record[name].asNode().toResource()
}

data class PredicateMapper(val name: String) : BiFunction<TypeSystem, Record, Predicate> {
    constructor(symbolicName: SymbolicName) : this(symbolicName.value)

    override fun apply(typeSystem: TypeSystem, record: Record) = record[name].asNode().toPredicate()
}

data class ClassMapper(val name: String) : BiFunction<TypeSystem, Record, Class> {
    constructor(symbolicName: SymbolicName) : this(symbolicName.value)

    override fun apply(typeSystem: TypeSystem, record: Record) = record[name].asNode().toClass()
}

data class ThingMapper(val name: String) : BiFunction<TypeSystem, Record, Thing> {
    constructor(symbolicName: SymbolicName) : this(symbolicName.value)

    override fun apply(typeSystem: TypeSystem, record: Record) = record[name].asNode().toThing()
}

fun Node.toLiteral() = Literal(
    id = this["id"].toThingId()!!,
    label = this["label"].asString(),
    datatype = this["datatype"]?.asString() ?: "xsd:string",
    createdAt = this["created_at"].toOffsetDateTime(),
    createdBy = this["created_by"].toContributorId(),
    modifiable = this["modifiable"].asBoolean()
)

fun Node.toThing(): Thing = when {
    hasLabel("Resource") -> toResource()
    hasLabel("Literal") -> toLiteral()
    hasLabel("Class") -> toClass()
    hasLabel("Predicate") -> toPredicate()
    else -> throw IllegalStateException("Cannot parse Thing with labels ${labels()}")
}

fun Node.toClass() = Class(
    id = this["id"].toThingId()!!,
    label = this["label"].asString(),
    uri = this["uri"].toIRI(),
    createdAt = this["created_at"].toOffsetDateTime(),
    createdBy = this["created_by"].toContributorId(),
    modifiable = this["modifiable"].asBoolean()
)

fun Node.toResource() = Resource(
    id = this["id"].toThingId()!!,
    label = this["label"].asString(),
    classes = this.labels().filter { it !in reservedLabelsAsStrings }.map(::ThingId).toSet(),
    createdAt = this["created_at"].toOffsetDateTime(),
    createdBy = this["created_by"].toContributorId(),
    observatoryId = this["observatory_id"].toObservatoryId(),
    organizationId = this["organization_id"].toOrganizationId(),
    extractionMethod = this["extraction_method"].toExtractionMethod(),
    visibility = this["visibility"].toVisibility(),
    unlistedBy = this["unlisted_by"].toNullableContributorId(),
    verified = this["verified"].asNullableBoolean(),
    modifiable = this["modifiable"].asBoolean()
)

fun Node.toPredicate() = Predicate(
    id = this["id"].toThingId()!!,
    label = this["label"].asString(),
    createdAt = this["created_at"].toOffsetDateTime(),
    createdBy = this["created_by"].toContributorId(),
    modifiable = this["modifiable"].asBoolean()
)

internal fun Value.asNullableBoolean(): Boolean? = if (isNull) null else isTrue

internal fun Value.toOffsetDateTime() = OffsetDateTime.parse(asString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)

internal fun Value.toIRI() = if (isNull) null else ParsedIRI.create(asString())

internal fun Value.toStatementId() = StatementId(asString())

internal fun Value.toContributorId() = if (isNull) ContributorId.UNKNOWN else ContributorId(asString())

internal fun Value.toNullableContributorId() = if (isNull) null else ContributorId(asString())

internal fun Value.toObservatoryId() = if (isNull) ObservatoryId.UNKNOWN else ObservatoryId(asString())

internal fun Value.toOrganizationId() = if (isNull) OrganizationId.UNKNOWN else OrganizationId(asString())

internal fun Value.toExtractionMethod() = if (isNull) ExtractionMethod.UNKNOWN else ExtractionMethod.valueOf(asString())

fun Value.toThingId() = if (isNull) null else ThingId(asString())

internal fun Value.toVisibility() = if (isNull) Visibility.DEFAULT else Visibility.valueOf(asString())

internal fun Value.asNullableInt() = if (isNull) null else asInt()

internal fun Value.asNullableLong() = if (isNull) null else asLong()

internal fun Value.asNullableNode() = if (isNull) null else asNode()

@Contract(pure = true)
internal fun startNode(symbolicName: SymbolicName): FunctionInvocation =
    FunctionInvocation.create({ "startNode" }, symbolicName)

@Contract(pure = true)
internal fun endNode(symbolicName: SymbolicName): FunctionInvocation =
    FunctionInvocation.create({ "endNode" }, symbolicName)

fun paperNode() = node("Paper")

fun comparisonNode() = node("Comparison")

fun problemNode() = node("Problem")

fun contributionNode() = node("Contribution")

fun ExposesWith.withSortableFields(node: SymbolicName) =
    with(
        node,
        node.property("label").`as`("label"),
        node.property("id").`as`("id"),
        node.property("created_at").`as`("created_at")
    )

fun Sort.toSortItems(
    node: Expression,
    vararg knownProperties: String,
): List<SortItem> = toSortItems(
    propertyMappings = knownProperties.associateWith { node.property(it) },
    knownProperties = knownProperties
)

fun Sort.toSortItems(
    propertyMappings: Map<String, Expression>? = null,
    vararg knownProperties: String,
): List<SortItem> =
    map { sort ->
        if (knownProperties.isNotEmpty() && sort.property !in knownProperties) {
            throw UnknownSortingProperty(sort.property)
        }
        var expression: Expression = propertyMappings?.get(sort.property) ?: name(sort.property)
        if (sort.isIgnoreCase) {
            expression = toLower(expression)
        }
        when (sort.direction) {
            Sort.Direction.DESC -> expression.descending()
            else -> expression.ascending()
        }
    }.toList()

inline fun <T> T?.toCondition(mapper: (T) -> Condition): Condition =
    this?.let(mapper) ?: noCondition()

inline fun <T : Collection<*>> T.toCondition(mapper: (T) -> Condition): Condition =
    if (isEmpty()) noCondition() else mapper(this)

fun StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere.where(
    vararg conditions: Condition,
): StatementBuilder.OrderableOngoingReadingAndWithWithWhere =
    where(conditions.reduceOrNull(Condition::and) ?: noCondition())

fun StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere.where(
    conditions: kotlin.collections.List<Condition>,
): StatementBuilder.OrderableOngoingReadingAndWithWithWhere =
    where(conditions.reduceOrNull(Condition::and) ?: noCondition())

fun StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere?.call(
    function: String,
    arguments: Array<Expression>,
    yieldItems: Array<String>,
    condition: Condition,
): StatementBuilder.OngoingReading =
    this?.apply {
        call(function)
            .withArgs(*arguments)
            .yield(*yieldItems)
            .where(condition)
    } ?: call(function)
        .withArgs(*arguments)
        .yield(*yieldItems)
        .where(condition)

fun Collection<PatternElement>.toMatchOrNull(node: org.neo4j.cypherdsl.core.Node): StatementBuilder.OngoingReadingWithoutWhere? =
    if (isEmpty()) null else match(node).match(this)

fun matchDistinct(
    node: org.neo4j.cypherdsl.core.Node,
    patternGenerator: (org.neo4j.cypherdsl.core.Node) -> Collection<PatternElement>,
): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
    patternGenerator(node).let { patterns ->
        match(node).let { if (patterns.isNotEmpty()) it.match(patterns) else it }.withDistinct(node)
    }

inline fun Sort.orElseGet(sort: () -> Sort): Sort =
    if (isSorted) this else sort()

fun orderByOptimizations(
    node: Expression,
    sort: Sort,
    vararg properties: String,
): kotlin.collections.List<Condition> = orderByOptimizations(
    propertyMappings = properties.associateWith { node.property(it) },
    sort = sort,
    properties = properties
)

fun orderByOptimizations(
    propertyMappings: Map<String, Expression>,
    sort: Sort,
    vararg properties: String,
): kotlin.collections.List<Condition> {
    val sortProperties = sort.map { it.property }
    return properties.filter { it in sortProperties }
        .map { propertyMappings[it]!!.isNotNull }
}

fun node(label: ThingId, vararg additionalLabels: ThingId) =
    node(label.value, additionalLabels.map { it.value })

val FuzzySearchString.query: String get() {
    val builder = StringBuilder()
    val reader = StringReader(input)

    while (reader.canRead()) {
        val c = reader.peek()
        if (c.isWordCharacter()) {
            if (builder.isNotEmpty()) {
                builder.append(" AND ")
            }
            builder.append("*")
            builder.append(reader.readWord())
            builder.append("*")
            reader.skipWhitespace()
        } else if ((c == '+' || c == '-') &&
            reader.canRead(1) &&
            reader.peek(1).isWordCharacter() &&
            (builder.isEmpty() || reader.cursor > 0 && reader.peek(-1).isWhitespace())
        ) {
            reader.skip()
            if (builder.isNotEmpty()) {
                builder.append(" AND ")
            }
            builder.append(c)
            builder.append(reader.readWord())
            reader.skipWhitespace()
        } else {
            reader.skip()
        }
    }

    return builder.toString().trim().ifBlank { "*" }
}

val ExactSearchString.query: String get() =
    if (input.isBlank()) "*" else QueryParser.escape(input)

private fun StringReader.readWord(): String {
    val start = cursor
    while (canRead()) {
        if (!peek().isWordCharacter()) {
            break
        }
        skip()
    }
    return string.substring(start, cursor)
}

private fun StringReader.skipWhitespace() {
    while (canRead()) {
        if (!peek().isWhitespace()) {
            return
        }
        skip()
    }
}

private fun Char.isWordCharacter(): Boolean = toString().matches(Regex("\\w"))
