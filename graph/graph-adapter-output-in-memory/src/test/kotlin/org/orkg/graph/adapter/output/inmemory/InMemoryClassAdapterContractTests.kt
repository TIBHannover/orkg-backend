package org.orkg.graph.adapter.output.inmemory

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.graph.testing.fixtures.classRepositoryContract

internal class InMemoryClassAdapterContractTests : DescribeSpec({
    val inMemoryGraph = InMemoryGraph()
    include(
        classRepositoryContract(
            InMemoryClassRepository(inMemoryGraph),
            InMemoryStatementRepository(inMemoryGraph),
            InMemoryLiteralRepository(inMemoryGraph),
            InMemoryPredicateRepository(inMemoryGraph)
        )
    )
})
