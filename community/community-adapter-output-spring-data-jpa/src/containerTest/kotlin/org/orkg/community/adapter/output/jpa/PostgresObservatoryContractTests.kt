package org.orkg.community.adapter.output.jpa

import org.orkg.auth.adapter.output.jpa.JpaUserAdapter
import org.orkg.auth.adapter.output.jpa.configuration.AuthJpaConfiguration
import org.orkg.auth.output.UserRepository
import org.orkg.community.adapter.output.jpa.configuration.CommunityJpaConfiguration
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.ObservatoryRepositoryContractTests
import org.orkg.eventbus.ReallySimpleEventBus
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringJpaPostgresObservatoryAdapter::class,
        CommunityJpaConfiguration::class,
        AuthJpaConfiguration::class,
        ReallySimpleEventBus::class,
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PostgresObservatoryContractTests : ObservatoryRepositoryContractTests {

    @Autowired
    private lateinit var adapter: SpringJpaPostgresObservatoryAdapter

    @Autowired
    private lateinit var organizationAdapter: SpringJpaPostgresOrganizationAdapter

    @Autowired
    private lateinit var userAdapter: JpaUserAdapter

    override val repository: ObservatoryRepository
        get() = adapter

    override val organizationRepository: OrganizationRepository
        get() = organizationAdapter

    override val userRepository: UserRepository
        get() = userAdapter

    override fun cleanUpAfterEach() {
        repository.deleteAll()
        organizationAdapter.deleteAll()
        userAdapter.deleteAll()
    }
}
