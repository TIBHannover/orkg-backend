package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.Fabrikate
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementVersion
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing

fun RosettaStoneStatementVersion.requiredEntities(): Set<Thing> =
    subjects.toSet() + objects.flatten()

fun RosettaStoneStatement.requiredEntities(fabricator: Fabrikate): Set<Thing> =
    setOfNotNull(
        fabricator.random<Resource>().copy(id = templateId, classes = setOf(Classes.rosettaNodeShape)),
        contextId?.let { fabricator.random<Resource>().copy(id = it) },
        fabricator.random<Class>().copy(id = templateTargetClassId),
    ) + versions.flatMap { it.requiredEntities() }
