package org.orkg.graph.adapter.output.inmemory

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.ClassSubclassRelation
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
    private val childClassIdToClassRelation: MutableMap<ThingId, ClassSubclassRelation> = mutableMapOf()

    fun add(thing: Thing) {
        things[thing.id] = thing
        resources.remove(thing.id)
        predicates.remove(thing.id)
        classes.remove(thing.id)
        literals.remove(thing.id)
        when (thing) {
            is Resource -> resources[thing.id] = thing
            is Predicate -> predicates[thing.id] = thing
            is Class -> classes[thing.id] = thing
            is Literal -> literals[thing.id] = thing
        }
        statements.values.mapNotNull { statement ->
            var result = statement
            if (result.subject.id == thing.id) {
                result = result.copy(subject = thing)
            }
            if (result.predicate.id == thing.id && thing is Predicate) {
                result = result.copy(predicate = thing)
            }
            if (result.`object`.id == thing.id) {
                result = result.copy(`object` = thing)
            }
            result.takeIf { it != statement }
        }.forEach { statements[it.id] = it }
        childClassIdToClassRelation.toMap().forEach { (key, value) ->
            if (value.parent.id == thing.id) {
                if (thing is Class) {
                    childClassIdToClassRelation[key] = value.copy(parent = thing)
                } else {
                    childClassIdToClassRelation.remove(key)
                }
            }
            if (value.child.id == thing.id) {
                if (thing is Class) {
                    childClassIdToClassRelation[key] = value.copy(child = thing)
                } else {
                    childClassIdToClassRelation.remove(key)
                }
            }
        }
    }

    fun add(statement: GeneralStatement) {
        add(statement.subject)
        add(statement.predicate)
        add(statement.`object`)
        statements[statement.id] = statement
    }

    fun add(classRelation: ClassSubclassRelation) {
        childClassIdToClassRelation[classRelation.child.id] = classRelation
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

    fun findStatementById(statementId: StatementId): Optional<GeneralStatement> =
        Optional.ofNullable(statements[statementId])

    fun findClassRelationByChildId(childId: ThingId) =
        Optional.ofNullable(childClassIdToClassRelation[childId])

    fun findAll(): List<Thing> = things.values.distinct()

    fun findAllResources(): List<Resource> = resources.values.distinct()

    fun findAllPredicates(): List<Predicate> = predicates.values.distinct()

    fun findAllClasses(): List<Class> = classes.values.distinct()

    fun findAllLiterals(): List<Literal> = literals.values.distinct()

    fun findAllStatements(): List<GeneralStatement> = statements.values.distinct()

    fun delete(thing: Thing) =
        delete(thing.id)

    fun delete(thingId: ThingId): Thing? =
        things.remove(thingId)?.also { thing ->
            when (thing) {
                is Resource -> resources.remove(thingId)
                is Predicate -> predicates.remove(thingId)
                is Class -> {
                    classes.remove(thingId)
                    childClassIdToClassRelation.remove(thingId)
                    childClassIdToClassRelation.filterValues { it.parent.id == thingId }
                        .map { it.value.child.id }
                        .forEach { childClassIdToClassRelation.remove(it) }
                }
                is Literal -> literals.remove(thingId)
            }
            statements.values.filter { it.subject.id == thingId || it.predicate.id == thingId || it.`object`.id == thingId }
                .forEach { statements.remove(it.id) }
        }

    fun delete(statementId: StatementId): GeneralStatement? =
        statements.remove(statementId)

    fun delete(statement: GeneralStatement): GeneralStatement? =
        delete(statement.id)

    fun delete(classRelation: ClassSubclassRelation): ClassSubclassRelation? =
        childClassIdToClassRelation.remove(classRelation.child.id)

    fun deleteAll() {
        statements.clear()
        things.clear()
        resources.clear()
        predicates.clear()
        classes.clear()
        literals.clear()
        deleteAllClassRelations()
    }

    fun deleteAllClassRelations() {
        childClassIdToClassRelation.clear()
    }
}
