package org.orkg.graph.adapter.output.inmemory

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.graph.testing.fixtures.classRepositoryContract

internal class InMemoryClassAdapterContractTest :
    DescribeSpec({
        include(
            classRepositoryContract(InMemoryClassRepository(InMemoryGraph()))
        )
    })
