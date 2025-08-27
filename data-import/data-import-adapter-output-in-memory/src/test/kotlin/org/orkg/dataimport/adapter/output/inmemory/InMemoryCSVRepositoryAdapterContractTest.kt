package org.orkg.dataimport.adapter.output.inmemory

import org.orkg.dataimport.output.CSVRepository
import org.orkg.dataimport.output.testing.fixtures.CSVRepositoryContracts

internal class InMemoryCSVRepositoryAdapterContractTest : CSVRepositoryContracts {
    override val repository: CSVRepository = InMemoryCSVRepositoryAdapter()

    override fun cleanUpAfterEach() {
        repository.deleteAll()
    }
}
