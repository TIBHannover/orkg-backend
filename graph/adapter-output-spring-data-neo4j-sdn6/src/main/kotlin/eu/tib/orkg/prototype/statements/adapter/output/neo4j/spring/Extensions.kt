package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ThingId

internal fun ThingId.toClassId() = ClassId(value)

internal fun Set<ThingId>.toClassIds() = map { it.toClassId() }.toSet()

internal fun Iterable<ThingId>.toClassIds() = map { it.toClassId() }
