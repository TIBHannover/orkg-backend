package org.orkg.contenttypes.domain.testing.fixtures

import dev.forkhandles.fabrikate.Fabrikate
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementVersion
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource

fun Fabrikate.withRosettaStoneStatementMappings() = apply {
    config.register {
        RosettaStoneStatementVersion(
            id = random(),
            subjects = listOf(random<Resource>(), random<Predicate>(), random<Class>()),
            objects = listOf(
                listOf(random<Resource>(), random<Predicate>(), random<Class>()),
                listOf(random<Resource>(), random<Predicate>(), random<Class>()),
                listOf(random<Resource>(), random<Predicate>(), random<Class>())
            ),
            createdAt = random(),
            createdBy = random(),
            certainty = random(),
            negated = random(),
            observatories = listOf(random()),
            organizations = listOf(random()),
            extractionMethod = random(),
            visibility = random(),
            unlistedBy = null,
            modifiable = true
        )
    }
    config.register {
        RosettaStoneStatement(
            id = random(),
            contextId = random(),
            templateId = random(),
            templateTargetClassId = random(),
            label = random(),
            versions = listOf(random<RosettaStoneStatementVersion>()),
            observatories = listOf(random()),
            organizations = listOf(random()),
            extractionMethod = random(),
            visibility = random(),
            unlistedBy = null,
            modifiable = true
        )
    }
}
