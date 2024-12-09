package org.orkg.community.adapter.output.jpa

import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.community.adapter.output.jpa.configuration.CommunityJpaConfiguration
import org.orkg.eventbus.ReallySimpleEventBus
import org.orkg.eventbus.events.UserRegistered
import org.orkg.testing.MockUserId
import org.orkg.testing.PostgresContainerInitializer
import org.orkg.testing.fixedClock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        CommunityJpaConfiguration::class,
        ContributorFromUserAdapter::class,
        ReallySimpleEventBus::class,
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class ContributorAdapterIdempotencyTest {
    @Autowired
    private lateinit var adapter: ContributorFromUserAdapter

    @Test
    fun `duplicate registration messages create a single entry`() {
        val message = UserRegistered(
            id = MockUserId.USER,
            displayName = "Some User",
            enabled = true,
            email = "user@example.org",
            roles = setOf(UserRegistered.Role.ADMIN),
            createdAt = LocalDateTime.now(fixedClock),
            observatoryId = null,
            organizationId = null,
        )

        repeat(times = 3) {
            adapter.notify(message)
        }

        assertThat(adapter.countActiveUsers()).isEqualTo(1)
    }
}
