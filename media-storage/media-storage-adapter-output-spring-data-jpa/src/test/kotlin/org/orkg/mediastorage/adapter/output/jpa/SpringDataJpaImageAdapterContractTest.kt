package org.orkg.mediastorage.adapter.output.jpa

import org.orkg.mediastorage.adapter.output.jpa.configuration.MediaStorageJpaConfiguration
import org.orkg.mediastorage.output.ImageRepository
import org.orkg.mediastorage.testing.fixtures.ImageRepositoryContracts
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringDataJpaImageAdapter::class,
        MediaStorageJpaConfiguration::class
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class SpringDataJpaImageAdapterContractTest : ImageRepositoryContracts {
    @Autowired
    private lateinit var adapter: SpringDataJpaImageAdapter

    override val repository: ImageRepository
        get() = adapter
}
