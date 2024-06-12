package org.orkg.graph.adapter.output.inmemory

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.graph.testing.fixtures.resourceRepositoryContract

internal class InMemoryResourceAdapterContractTests : DescribeSpec({
    val inMemoryGraph = InMemoryGraph()
    include(
        resourceRepositoryContract(
            InMemoryResourceRepository(inMemoryGraph),
            InMemoryClassRepository(inMemoryGraph),
            InMemoryClassRelationRepository(inMemoryGraph)
        )
    )
})
