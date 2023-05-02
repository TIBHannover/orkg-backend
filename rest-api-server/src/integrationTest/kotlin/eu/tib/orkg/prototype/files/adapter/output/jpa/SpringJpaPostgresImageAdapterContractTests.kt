package eu.tib.orkg.prototype.files.adapter.output.jpa

import eu.tib.orkg.prototype.files.spi.ImageRepository
import eu.tib.orkg.prototype.files.spi.ImageRepositoryContractTest
import eu.tib.orkg.prototype.testing.PostgresTestContainersBaseTest
import org.springframework.beans.factory.annotation.Autowired

class SpringJpaPostgresImageAdapterContractTests : PostgresTestContainersBaseTest(), ImageRepositoryContractTest {

    @Autowired
    private lateinit var adapter: SpringJpaPostgresImageAdapter

    override val repository: ImageRepository
        get() = adapter
}
