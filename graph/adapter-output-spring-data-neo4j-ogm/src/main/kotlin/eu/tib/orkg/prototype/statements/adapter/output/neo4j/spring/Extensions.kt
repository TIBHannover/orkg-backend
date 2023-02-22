package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId

internal fun ThingId.toClassId() = ClassId(value)

internal fun ThingId.toLiteralId() = LiteralId(value)

internal fun ThingId.toPredicateId() = PredicateId(value)

internal fun ThingId.toResourceId() = ResourceId(value)

internal fun Set<ThingId>.toClassIds() = map { it.toClassId() }.toSet()

internal fun Iterable<ThingId>.toClassIds() = map { it.toClassId() }

internal fun Set<ThingId>.toResourceIds() = map { it.toResourceId() }.toSet()
