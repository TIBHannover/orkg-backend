package org.orkg.dataimport.adapter.output.jpa

import org.orkg.dataimport.adapter.output.jpa.configuration.DataImportJpaConfiguration
import org.orkg.dataimport.adapter.output.jpa.configuration.DataImportJpaTestConfiguration
import org.orkg.dataimport.output.PaperCSVRecordImportResultRepository
import org.orkg.dataimport.output.testing.fixtures.PaperCSVRecordImportResultRepositoryContracts
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringDataJpaPaperCSVRecordImportResultRepositoryAdapter::class,
        DataImportJpaConfiguration::class,
        DataImportJpaTestConfiguration::class,
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class SpringDataJpaPaperCSVRecordImportResultRepositoryAdapterContractTest : PaperCSVRecordImportResultRepositoryContracts {
    @Autowired
    private lateinit var adapter: SpringDataJpaPaperCSVRecordImportResultRepositoryAdapter

    override val repository: PaperCSVRecordImportResultRepository
        get() = adapter

    override fun cleanUpAfterEach() {
        adapter.deleteAll()
    }
}
