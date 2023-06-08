package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.spi.resourceRepositoryContract
import io.kotest.core.spec.style.DescribeSpec

internal class InMemoryResourceAdapterContractTests : DescribeSpec({
    include(resourceRepositoryContract(InMemoryResourceRepository()))
})
