package org.orkg.dataimport.adapter.output.jpa

import org.orkg.dataimport.adapter.output.jpa.configuration.DataImportJpaConfiguration
import org.orkg.dataimport.adapter.output.jpa.configuration.DataImportJpaTestConfiguration
import org.orkg.dataimport.output.RdfClassRepository
import org.orkg.dataimport.output.testing.fixtures.RdfClassRepositoryContracts
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringDataJpaRdfClassRepositoryAdapter::class,
        DataImportJpaConfiguration::class,
        DataImportJpaTestConfiguration::class,
    ],
    initializers = [PostgresContainerInitializer::class],
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class SpringDataJpaRdfClassRepositoryAdapterContractTest : RdfClassRepositoryContracts {
    @Autowired
    private lateinit var adapter: SpringDataJpaRdfClassRepositoryAdapter

    override val repository: RdfClassRepository
        get() = adapter
}
