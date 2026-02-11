package org.orkg.community.adapter.output.jpa

import org.orkg.community.adapter.output.jpa.configuration.CommunityJpaConfiguration
import org.orkg.community.adapter.output.jpa.configuration.CommunityJpaTestConfiguration
import org.orkg.community.output.ContributorIdentifierRepository
import org.orkg.community.output.ContributorRepository
import org.orkg.community.testing.fixtures.ContributorIdentifierRepositoryContracts
import org.orkg.eventbus.ReallySimpleEventBus
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringDataJpaContributorIdentifierAdapter::class,
        CommunityJpaConfiguration::class,
        CommunityJpaTestConfiguration::class,
        ReallySimpleEventBus::class,
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class SpringDataJpaContributorIdentifierContractTest : ContributorIdentifierRepositoryContracts {
    @Autowired
    private lateinit var adapter: SpringDataJpaContributorIdentifierAdapter

    @Autowired
    private lateinit var contributorAdapter: SpringDataJpaContributorFromUserAdapter

    override val repository: ContributorIdentifierRepository
        get() = adapter

    override val contributorRepository: ContributorRepository
        get() = contributorAdapter
}
