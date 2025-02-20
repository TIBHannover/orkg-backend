package org.orkg.graph.adapter.output.inmemory

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.graph.testing.fixtures.literalRepositoryContract

internal class InMemoryLiteralAdapterContractTest :
    DescribeSpec({
        include(literalRepositoryContract(InMemoryLiteralRepository(InMemoryGraph())))
    })
