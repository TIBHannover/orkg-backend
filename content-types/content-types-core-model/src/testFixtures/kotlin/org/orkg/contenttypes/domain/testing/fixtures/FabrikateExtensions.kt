package org.orkg.contenttypes.domain.testing.fixtures

import dev.forkhandles.fabrikate.Fabricator
import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import dev.forkhandles.fabrikate.register
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementVersion
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource

class RosettaStoneStatementVersionFabricator : Fabricator<RosettaStoneStatementVersion> {
    override fun invoke(fabrikate: Fabrikate): RosettaStoneStatementVersion = RosettaStoneStatementVersion(
        id = fabrikate.random(),
        formattedLabel = FormattedLabel.of(fabrikate.random()),
        subjects = listOf(fabrikate.random<Resource>(), fabrikate.random<Predicate>(), fabrikate.random<Class>()),
        objects = listOf(
            listOf(fabrikate.random<Resource>(), fabrikate.random<Predicate>(), fabrikate.random<Class>()),
            listOf(fabrikate.random<Resource>(), fabrikate.random<Predicate>(), fabrikate.random<Class>()),
            listOf(fabrikate.random<Resource>(), fabrikate.random<Predicate>(), fabrikate.random<Class>())
        ),
        createdAt = fabrikate.random(),
        createdBy = fabrikate.random(),
        certainty = fabrikate.random(),
        negated = fabrikate.random(),
        observatories = listOf(fabrikate.random()),
        organizations = listOf(fabrikate.random()),
        extractionMethod = fabrikate.random(),
        visibility = fabrikate.random(),
        unlistedBy = null,
        modifiable = true
    )
}

class RosettaStoneStatementFabricator : Fabricator<RosettaStoneStatement> {
    override fun invoke(fabrikate: Fabrikate): RosettaStoneStatement = RosettaStoneStatement(
        id = fabrikate.random(),
        contextId = fabrikate.random(),
        templateId = fabrikate.random(),
        templateTargetClassId = fabrikate.random(),
        label = fabrikate.random(),
        versions = listOf(fabrikate.random<RosettaStoneStatementVersion>()),
        observatories = listOf(fabrikate.random()),
        organizations = listOf(fabrikate.random()),
        extractionMethod = fabrikate.random(),
        visibility = fabrikate.random(),
        unlistedBy = null,
        modifiable = true
    )
}

fun FabricatorConfig.withRosettaStoneStatementMappings(): FabricatorConfig = withMappings {
    register(RosettaStoneStatementVersionFabricator())
    register(RosettaStoneStatementFabricator())
}
