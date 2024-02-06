package org.orkg.graph.adapter.output.inmemory

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.graph.testing.fixtures.resourceRepositoryContract

internal class InMemoryResourceAdapterContractTests : DescribeSpec({
    include(resourceRepositoryContract(InMemoryResourceRepository(InMemoryGraph())))
})
