package org.orkg.dataimport.adapter.output.inmemory

import org.orkg.dataimport.output.PaperCSVRecordImportResultRepository
import org.orkg.dataimport.output.testing.fixtures.PaperCSVRecordImportResultRepositoryContracts

internal class InMemoryPaperCSVRecordImportResultRepositoryAdapterContractTest : PaperCSVRecordImportResultRepositoryContracts {
    override val repository: PaperCSVRecordImportResultRepository = InMemoryPaperCSVRecordImportResultRepositoryAdapter()

    override fun cleanUpAfterEach() {
        repository.deleteAll()
    }
}
