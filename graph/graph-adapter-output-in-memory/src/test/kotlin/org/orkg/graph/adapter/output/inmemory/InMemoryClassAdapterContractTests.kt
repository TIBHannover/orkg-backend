package org.orkg.graph.adapter.output.inmemory

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.graph.testing.fixtures.classRepositoryContract

internal class InMemoryClassAdapterContractTests : DescribeSpec({
    include(
        classRepositoryContract(
            InMemoryClassRepository(),
            InMemoryStatementRepository(),
            InMemoryLiteralRepository(),
            InMemoryPredicateRepository()
        )
    )
})
