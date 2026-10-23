package org.orkg.dataimport.domain.testing.fixtures

import dev.forkhandles.fabrikate.Fabricator
import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import dev.forkhandles.fabrikate.StringFabricator
import dev.forkhandles.fabrikate.register
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.csv.papers.ContributionStatement

class ContributionStatementFabricator : Fabricator<ContributionStatement> {
    override fun invoke(fabrikate: Fabrikate): ContributionStatement = ContributionStatement(
        predicate = if (fabrikate.random<Boolean>()) Either.left(fabrikate.random<ThingId>()) else Either.right(fabrikate.random<String>()),
        `object` = fabrikate.random(),
    )
}

class IRIFabricator : Fabricator<IRI> {
    private val stringFabricator = StringFabricator(length = IntRange(10, 20))

    override fun invoke(fabrikate: Fabrikate): IRI =
        SimpleValueFactory.getInstance().createIRI("https://example.com/${stringFabricator(fabrikate)}")
}

class ResourceFabricator : Fabricator<Resource> {
    private val stringFabricator = StringFabricator(length = IntRange(10, 20))

    override fun invoke(fabrikate: Fabrikate): Resource =
        if (fabrikate.random<Boolean>()) {
            fabrikate.random<IRI>()
        } else {
            SimpleValueFactory.getInstance().createBNode(stringFabricator(fabrikate))
        }
}

class ValueFabricator : Fabricator<Value> {
    private val stringFabricator = StringFabricator(length = IntRange(10, 20))

    override fun invoke(fabrikate: Fabrikate): Value =
        if (fabrikate.random<Boolean>()) {
            fabrikate.random<Resource>()
        } else {
            SimpleValueFactory.getInstance().createLiteral(stringFabricator(fabrikate))
        }
}

fun FabricatorConfig.withDataImportMappings(): FabricatorConfig = withMappings {
    register(ContributionStatementFabricator())
    register(IRIFabricator())
    register(ResourceFabricator())
    register(ValueFabricator())
}
