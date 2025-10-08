package org.orkg.contenttypes.domain.testing.fixtures

import dev.forkhandles.fabrikate.Fabricator
import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import dev.forkhandles.fabrikate.register
import org.orkg.common.Handle
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.EmbeddedStatement
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementVersion
import org.orkg.contenttypes.domain.TemplateInstance
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.DynamicLabel
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import kotlin.math.absoluteValue

class RosettaStoneStatementVersionFabricator : Fabricator<RosettaStoneStatementVersion> {
    override fun invoke(fabrikate: Fabrikate): RosettaStoneStatementVersion = RosettaStoneStatementVersion(
        id = fabrikate.random(),
        label = fabrikate.random(),
        dynamicLabel = DynamicLabel(fabrikate.random()),
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

class EmbeddedStatementFabricator : Fabricator<EmbeddedStatement> {
    override fun invoke(fabrikate: Fabrikate): EmbeddedStatement = EmbeddedStatement(
        thing = fabrikate.random(),
        createdAt = fabrikate.random(),
        createdBy = fabrikate.random(),
        statements = emptyMap()
    )
}

class TemplateInstanceFabricator : Fabricator<TemplateInstance> {
    override fun invoke(fabrikate: Fabrikate): TemplateInstance {
        val predicateIds = fabrikate.random<List<ThingId>>()
        return TemplateInstance(
            root = fabrikate.random<Resource>(),
            predicates = predicateIds.associateWith { fabrikate.random<Predicate>() },
            statements = predicateIds.associate { it to fabrikate.random<List<EmbeddedStatement>>() },
        )
    }
}

class HandleFabricator : Fabricator<Handle> {
    override fun invoke(fabrikate: Fabrikate): Handle =
        Handle.of("${fabrikate.random<Long>().absoluteValue}.${fabrikate.random<Long>().absoluteValue}/${fabrikate.random<Long>()}")
}

fun FabricatorConfig.withRosettaStoneStatementMappings(): FabricatorConfig = withMappings {
    register(RosettaStoneStatementVersionFabricator())
    register(RosettaStoneStatementFabricator())
}

fun FabricatorConfig.withContentTypeMappings(): FabricatorConfig = withMappings {
    register(EmbeddedStatementFabricator())
    register(TemplateInstanceFabricator())
    register(HandleFabricator())
}
