package org.orkg.dataimport.domain.testing.fixtures

import dev.forkhandles.fabrikate.Fabricator
import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import dev.forkhandles.fabrikate.register
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.csv.papers.ContributionStatement

class ContributionStatementFabricator : Fabricator<ContributionStatement> {
    override fun invoke(fabrikate: Fabrikate): ContributionStatement = ContributionStatement(
        predicate = if (fabrikate.random<Boolean>()) Either.left(fabrikate.random<ThingId>()) else Either.right(fabrikate.random<String>()),
        `object` = fabrikate.random(),
    )
}

fun FabricatorConfig.withDataImportMappings(): FabricatorConfig = withMappings {
    register(ContributionStatementFabricator())
}
