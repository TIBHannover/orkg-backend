package eu.tib.orkg.prototype.community.adapter.output.jpa

import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.community.spi.ObservatoryRepositoryContractTest
import eu.tib.orkg.prototype.testing.PostgresTestContainersBaseTest
import org.springframework.beans.factory.annotation.Autowired

class PostgresObservatoryContractTests : PostgresTestContainersBaseTest(), ObservatoryRepositoryContractTest {

    @Autowired
    private lateinit var adapter: SpringJpaPostgresObservatoryAdapter

    // TODO: Replace with adapter
    @Autowired
    private lateinit var organizationAdapter: PostgresOrganizationRepository

    // TODO: Replace with adapter
    @Autowired
    private lateinit var userAdapter: UserRepository

    override val repository: ObservatoryRepository
        get() = adapter

    // TODO: Replace with adapter
    override val organizationRepository: PostgresOrganizationRepository
        get() = organizationAdapter

    // TODO: Replace with adapter
    override val userRepository: UserRepository
        get() = userAdapter

    override fun cleanUpAfterEach() {
        repository.deleteAll()
        organizationAdapter.deleteAll()
        userAdapter.deleteAll()
    }
}
