package org.orkg.graph.adapter.output.inmemory

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing

class InMemoryGraph {
    private val things: MutableMap<ThingId, Thing> = mutableMapOf()
    private val resources: MutableMap<ThingId, Resource> = mutableMapOf()
    private val predicates: MutableMap<ThingId, Predicate> = mutableMapOf()
    private val classes: MutableMap<ThingId, Class> = mutableMapOf()
    private val literals: MutableMap<ThingId, Literal> = mutableMapOf()
    private val statements: MutableMap<StatementId, GeneralStatement> = mutableMapOf()

    fun add(thing: Thing) {
        things[thing.id] = thing
        when (thing) {
            is Resource -> resources[thing.id] = thing
            is Predicate -> predicates[thing.id] = thing
            is Class -> classes[thing.id] = thing
            is Literal -> literals[thing.id] = thing
        }
    }

    fun add(statement: GeneralStatement) {
        add(statement.subject)
        add(statement.predicate)
        add(statement.`object`)
        statements[statement.id!!] = statement
    }

    fun findById(thingId: ThingId): Optional<Thing> =
        Optional.ofNullable(things[thingId])

    fun findResourceById(thingId: ThingId): Optional<Resource> =
        Optional.ofNullable(resources[thingId])

    fun findPredicateById(thingId: ThingId): Optional<Predicate> =
        Optional.ofNullable(predicates[thingId])

    fun findClassById(thingId: ThingId): Optional<Class> =
        Optional.ofNullable(classes[thingId])

    fun findLiteralById(thingId: ThingId): Optional<Literal> =
        Optional.ofNullable(literals[thingId])

    fun findAll(): List<Thing> = things.values.distinct()

    fun findAllResources(): List<Thing> = resources.values.distinct()

    fun findAllPredicates(): List<Thing> = predicates.values.distinct()

    fun findAllClasses(): List<Thing> = classes.values.distinct()

    fun findAllLiterals(): List<Thing> = literals.values.distinct()

    fun findAllStatements(): List<GeneralStatement> = statements.values.distinct()

    fun remove(thing: Thing) =
        remove(thing.id)

    fun remove(thingId: ThingId) {
        things.remove(thingId)?.let { thing ->
            when (thing) {
                is Resource -> resources.remove(thingId)
                is Predicate -> predicates.remove(thingId)
                is Class -> classes.remove(thingId)
                is Literal -> literals.remove(thingId)
            }
            statements.values.filter { it.subject.id == thingId || it.predicate.id == thingId || it.`object`.id == thingId }
                .forEach { statements.remove(it.id) }
        }
    }

    fun remove(statementId: StatementId) {
        statements.remove(statementId)
    }

    fun remove(statement: GeneralStatement) =
        statement.id?.let { remove(it) }

    fun removeAll() {
        statements.clear()
        things.clear()
        resources.clear()
        predicates.clear()
        classes.clear()
        literals.clear()
    }
}
