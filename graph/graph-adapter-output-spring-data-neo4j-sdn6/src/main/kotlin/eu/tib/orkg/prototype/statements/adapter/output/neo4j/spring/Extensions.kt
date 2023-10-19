package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.PredicateNotFound
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.List
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import java.net.URI
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.function.BiFunction
import java.util.stream.Collectors
import org.jetbrains.annotations.Contract
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Expression
import org.neo4j.cypherdsl.core.FunctionInvocation
import org.neo4j.cypherdsl.core.Functions
import org.neo4j.cypherdsl.core.ResultStatement
import org.neo4j.cypherdsl.core.StatementBuilder
import org.neo4j.cypherdsl.core.SymbolicName
import org.neo4j.driver.Record
import org.neo4j.driver.Value
import org.neo4j.driver.types.MapAccessor
import org.neo4j.driver.types.Node
import org.neo4j.driver.types.TypeSystem
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

private val reservedClassIds = setOf(
    "Thing",
    "Literal",
    "Class",
    "Predicate",
    "Resource"
)

internal data class StatementMapper(
    val predicateRepository: PredicateRepository,
    val subject: String = "sub",
    val relation: String = "rel",
    val `object`: String = "obj",
) : BiFunction<TypeSystem, Record, GeneralStatement> {
    constructor(
        predicateRepository: PredicateRepository,
        subject: SymbolicName,
        relation: SymbolicName,
        `object`: SymbolicName
    ) : this(predicateRepository, subject.value, relation.value, `object`.value)

    override fun apply(typeSystem: TypeSystem, record: Record) : GeneralStatement {
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
            index = relation["index"].asNullableInt()
        )
    }
}

internal data class LiteralMapper(val name: String) : BiFunction<TypeSystem, Record, Literal> {
    constructor(symbolicName: SymbolicName) : this(symbolicName.value)
    override fun apply(typeSystem: TypeSystem, record: Record) = record[name].asNode().toLiteral()
}

internal data class ResourceMapper(val name: String) : BiFunction<TypeSystem, Record, Resource> {
    constructor(symbolicName: SymbolicName) : this(symbolicName.value)
    override fun apply(typeSystem: TypeSystem, record: Record) = record[name].asNode().toResource()
}

internal data class ListMapper(val name: String, val elements: String) : BiFunction<TypeSystem, Record, List> {
    constructor(name: SymbolicName, elements: SymbolicName) : this(name.value, elements.value)
    override fun apply(typeSystem: TypeSystem, record: Record): List {
        val node = record[name]
        return List(
            id = node["id"].toThingId()!!,
            label = node["label"].asString(),
            elements = record[elements].toThingIds(),
            createdAt = node["created_at"].toOffsetDateTime(),
            createdBy = node["created_by"].toContributorId(),
        )
    }
}

internal data class PredicateMapper(val name: String) : BiFunction<TypeSystem, Record, Predicate> {
    constructor(symbolicName: SymbolicName) : this(symbolicName.value)
    override fun apply(typeSystem: TypeSystem, record: Record) = record[name].asNode().toPredicate()
}

internal data class ClassMapper(val name: String) : BiFunction<TypeSystem, Record, Class> {
    constructor(symbolicName: SymbolicName) : this(symbolicName.value)
    override fun apply(typeSystem: TypeSystem, record: Record) = record[name].asNode().toClass()
}

internal fun Node.toLiteral() = Literal(
    id = this["id"].toThingId()!!,
    label = this["label"].asString(),
    datatype = this["datatype"]?.asString() ?: "xsd:string",
    createdAt = this["created_at"].toOffsetDateTime(),
    createdBy = this["created_by"].toContributorId(),
)

internal fun Node.toThing(): Thing = when {
    hasLabel("Resource") -> toResource()
    hasLabel("Literal") -> toLiteral()
    hasLabel("Class") -> toClass()
    hasLabel("Predicate") -> toPredicate()
    else -> throw IllegalStateException("Cannot parse Thing with labels ${labels()}")
}

internal fun Node.toClass() = Class(
    id = this["id"].toThingId()!!,
    label = this["label"].asString(),
    uri = this["uri"].toURI(),
    createdAt = this["created_at"].toOffsetDateTime(),
    createdBy = this["created_by"].toContributorId(),
)

internal fun Node.toResource() = Resource(
    id = this["id"].toThingId()!!,
    label = this["label"].asString(),
    classes = this.labels().filter { it !in reservedClassIds }.map(::ThingId).toSet(),
    createdAt = this["created_at"].toOffsetDateTime(),
    createdBy = this["created_by"].toContributorId(),
    observatoryId = this["observatory_id"].toObservatoryId(),
    organizationId = this["organization_id"].toOrganizationId(),
    extractionMethod = this["extraction_method"].toExtractionMethod(),
    visibility = this["visibility"].toVisibility(),
    verified = this["verified"].asNullableBoolean()
)

internal fun Node.toPredicate() = Predicate(
    id = this["id"].toThingId()!!,
    label = this["label"].asString(),
    createdAt = this["created_at"].toOffsetDateTime(),
    createdBy = this["created_by"].toContributorId(),
)

internal fun Value.asNullableBoolean(): Boolean? = if (isNull) null else isTrue
internal fun Value.toOffsetDateTime() = OffsetDateTime.parse(asString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
internal fun Value.toURI() = if (isNull) null else URI.create(asString())
internal fun Value.toStatementId() = StatementId(asString())
internal fun Value.toContributorId() = if (isNull) ContributorId.createUnknownContributor() else ContributorId(asString())
internal fun Value.toObservatoryId() = if (isNull) ObservatoryId.createUnknownObservatory() else ObservatoryId(asString())
internal fun Value.toOrganizationId() = if (isNull) OrganizationId.createUnknownOrganization() else OrganizationId(asString())
internal fun Value.toExtractionMethod() = if (isNull) ExtractionMethod.UNKNOWN else ExtractionMethod.valueOf(asString())
internal fun Value.toThingId() = if (isNull) null else ThingId(asString())
internal fun Value.toVisibility() = if (isNull) Visibility.DEFAULT else Visibility.valueOf(asString())
internal fun Value.toThingIds() = asList().map { ThingId(it as String) }
internal fun Value.asNullableInt() = if (isNull) null else asInt()
internal fun Value.asNullableLong() = if (isNull) null else asLong()
internal fun Value.asNullableNode() = if (isNull) null else asNode()

@Contract(pure = true)
internal fun startNode(symbolicName: SymbolicName): FunctionInvocation =
    FunctionInvocation.create({ "startNode" }, symbolicName)

@Contract(pure = true)
internal fun endNode(symbolicName: SymbolicName): FunctionInvocation =
    FunctionInvocation.create({ "endNode" }, symbolicName)

// TODO: This extension function is required because the Cypher DSL used does not support calling the method, and
//       newer versions requiring Java 17. It can be replaced after upgrading.
@Contract(pure = true)
internal fun toUpper(expression: Expression): FunctionInvocation =
    FunctionInvocation.create({ "toUpper" }, expression)

internal operator fun MapAccessor.get(symbolicName: SymbolicName): Value = this[symbolicName.value]

internal fun paperNode() = node("Paper", "Resource")
internal fun comparisonNode() = node("Comparison", "Resource")
internal fun problemNode() = node("Problem", "Resource")
internal fun contributionNode() = node("Contribution", "Resource")

internal fun StatementBuilder.TerminalExposesOrderBy.build(pageable: Pageable): ResultStatement =
    orderBy(pageable.sort).skip(pageable.offset).limit(pageable.pageSize).build()

internal fun StatementBuilder.TerminalExposesOrderBy.orderBy(sort: Sort): StatementBuilder.OngoingMatchAndReturnWithOrder =
    orderBy(sort.get().map {
        var expression: Expression = name(it.property)
        if (it.isIgnoreCase) {
            expression = Functions.toLower(expression)
        }
        when (it.direction) {
            Sort.Direction.DESC -> expression.descending()
            else -> expression.ascending()
        }
    }.collect(Collectors.toList()))

internal fun String.sortedWith(sort: Sort): String =
    if (sort.isUnsorted) {
        this
    } else {
        StringBuilder(this)
            .insert(lastIndexOf("SKIP"),"ORDER BY ${sort.toNeo4jSnippet()} ")
            .toString()
    }

internal fun Sort.toNeo4jSnippet(): String =
    stream().map { it.toNeo4jSnippet() }.collect(Collectors.joining(", "))

internal fun Sort.Order.toNeo4jSnippet(): String = buildString {
    if (isIgnoreCase) {
        append("toLower(").append(property).append(")")
    } else {
        append(property)
    }
    append(" ").append(direction)
}

internal fun StringBuilder.appendOrderByOptimizations(pageable: Pageable, createdAt: OffsetDateTime?, createdBy: ContributorId?) {
    val properties = pageable.sort.map { it.property }

    if (createdAt == null && "created_at" in properties) {
        append(" AND n.created_at IS NOT NULL")
    }
    if (createdBy == null && "created_by" in properties) {
        append(" AND n.created_by IS NOT NULL")
    }
    if ("id" in properties) {
        append(" AND n.id IS NOT NULL")
    }
    if ("label" in properties) {
        append(" AND n.label IS NOT NULL")
    }
}
