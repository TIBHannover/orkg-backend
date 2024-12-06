package org.orkg.graph.adapter.output.inmemory

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.graph.testing.fixtures.predicateRepositoryContract

internal class InMemoryPredicateAdapterContractTest : DescribeSpec({
    val inMemoryGraph = InMemoryGraph()
    include(
        predicateRepositoryContract(
            InMemoryPredicateRepository(inMemoryGraph),
            InMemoryStatementRepository(inMemoryGraph),
            InMemoryClassRepository(inMemoryGraph),
            InMemoryLiteralRepository(inMemoryGraph),
            InMemoryResourceRepository(inMemoryGraph)
        )
    )
})
