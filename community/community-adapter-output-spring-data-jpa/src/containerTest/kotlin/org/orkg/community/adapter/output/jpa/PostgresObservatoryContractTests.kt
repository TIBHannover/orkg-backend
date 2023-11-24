package org.orkg.community.adapter.output.jpa

import org.orkg.auth.adapter.output.jpa.configuration.AuthJpaConfiguration
import org.orkg.auth.output.UserRepository
import org.orkg.community.adapter.output.jpa.configuration.CommunityJpaConfiguration
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.testing.fixtures.ObservatoryRepositoryContractTest
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringJpaPostgresObservatoryAdapter::class,
        CommunityJpaConfiguration::class,
        AuthJpaConfiguration::class
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PostgresObservatoryContractTests : ObservatoryRepositoryContractTest {

    @Autowired
    private lateinit var adapter: SpringJpaPostgresObservatoryAdapter

    // TODO: Replace with adapter
    @Autowired
    private lateinit var organizationAdapter: PostgresOrganizationRepository

    @Autowired
    @Qualifier("jpaUserAdapter")
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
