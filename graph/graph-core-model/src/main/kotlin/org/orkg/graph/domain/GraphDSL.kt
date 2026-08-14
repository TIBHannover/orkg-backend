package org.orkg.graph.domain

import org.orkg.common.ThingId

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class GraphDSL

fun thing(spec: ThingSpecBuilder.() -> Unit): ThingSpec =
    ThingSpecBuilder().apply(spec).build()

@GraphDSL
data class ThingSpecBuilder(
    private var classPredicate: (Set<ThingId>) -> Boolean = { true },
    private var shared: Boolean = false,
    private val statements: MutableList<StatementSpecBuilder> = mutableListOf(),
) {
    fun classId(vararg allOfClasses: ThingId) {
        classPredicate = { it.containsAll(allOfClasses.toSet()) }
    }

    fun classId(predicate: (Set<ThingId>) -> Boolean) {
        classPredicate = predicate
    }

    fun literal() {
        classPredicate = { Classes.literal in it }
    }

    fun statementTo(spec: StatementSpecBuilder.Outgoing.() -> Unit) {
        statements.add(StatementSpecBuilder.Outgoing().apply(spec))
    }

    fun statementFrom(spec: StatementSpecBuilder.Incoming.() -> Unit) {
        statements.add(StatementSpecBuilder.Incoming().apply(spec))
    }

    fun shared() {
        shared = true
    }

    fun build(): ThingSpec = ThingSpec(
        classPredicate = classPredicate,
        shared = shared,
        statements = statements.map { it.build() },
    )
}

@GraphDSL
abstract class StatementSpecBuilder(protected var predicatePredicate: (ThingId) -> Boolean = { true }) {
    fun predicateId(vararg oneOfPredicates: ThingId) {
        predicatePredicate = { it in oneOfPredicates }
    }

    fun predicateId(predicate: (ThingId) -> Boolean) {
        predicatePredicate = predicate
    }

    abstract fun build(): StatementSpec

    @GraphDSL
    data class Outgoing(private val `object`: ThingSpecBuilder = ThingSpecBuilder()) : StatementSpecBuilder() {
        fun `object`(spec: ThingSpecBuilder.() -> Unit) {
            spec(`object`)
        }

        override fun build(): StatementSpec = StatementSpec.Outgoing(`object`.build(), predicatePredicate)
    }

    @GraphDSL
    data class Incoming(private val subject: ThingSpecBuilder = ThingSpecBuilder()) : StatementSpecBuilder() {
        fun subject(spec: ThingSpecBuilder.() -> Unit) {
            spec(subject)
        }

        override fun build(): StatementSpec = StatementSpec.Incoming(subject.build(), predicatePredicate)
    }
}

data class ThingSpec(
    val classPredicate: (Set<ThingId>) -> Boolean,
    val shared: Boolean,
    val statements: kotlin.collections.List<StatementSpec>,
)

sealed interface StatementSpec {
    val predicatePredicate: (ThingId) -> Boolean

    data class Outgoing(
        val `object`: ThingSpec,
        override val predicatePredicate: (ThingId) -> Boolean,
    ) : StatementSpec

    data class Incoming(
        val subject: ThingSpec,
        override val predicatePredicate: (ThingId) -> Boolean,
    ) : StatementSpec
}
