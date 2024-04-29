package org.orkg.mediastorage.adapter.output.jpa

import org.orkg.mediastorage.adapter.output.jpa.configuration.MediaStorageJpaConfiguration
import org.orkg.mediastorage.output.ImageRepository
import org.orkg.mediastorage.testing.fixtures.ImageRepositoryContractTest
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringJpaPostgresImageAdapter::class,
        MediaStorageJpaConfiguration::class
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SpringJpaPostgresImageAdapterContractTests : ImageRepositoryContractTest {

    @Autowired
    private lateinit var adapter: SpringJpaPostgresImageAdapter

    override val repository: ImageRepository
        get() = adapter
}
