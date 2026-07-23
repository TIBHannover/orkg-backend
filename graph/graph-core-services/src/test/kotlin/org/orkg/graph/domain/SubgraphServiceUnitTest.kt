package org.orkg.graph.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.output.SubgraphRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import org.springframework.data.domain.PageRequest
import java.util.Optional

internal class SubgraphServiceUnitTest : MockkBaseTest {
    private val subgraphRepository: SubgraphRepository = mockk()
    private val thingRepository: ThingRepository = mockk()

    private val service = SubgraphService(subgraphRepository, thingRepository)

    @Test
    fun `Given a subgraph, when fetching it by id, it returns the subgraph`() {
        val id = ThingId("R123")
        val subgraph = pageOf(createStatement())

        every { thingRepository.findById(id) } returns Optional.of(subgraph.first().subject)
        every { subgraphRepository.findByRootId(id, any(), any(), any(), any(), any(), any()) } returns subgraph

        service.findByRootId(id, PageRequest.of(0, 10)) shouldBe subgraph

        verify(exactly = 1) { thingRepository.findById(id) }
        verify(exactly = 1) { subgraphRepository.findByRootId(id, any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Given a subgraph, when fetching it by id, but min hops is larger than max bounds, it throws an exception`() {
        shouldThrow<InvalidHopBounds> {
            service.findByRootId(
                id = ThingId("R123"),
                minHops = 5,
                maxHops = 2,
                pageable = PageRequest.of(0, 10),
            )
        }
    }

    @Test
    fun `Given a subgraph, when fetching it by id, but root does not exist, it throws an exception`() {
        val id = ThingId("R123")

        every { thingRepository.findById(id) } returns Optional.empty()

        shouldThrow<ThingNotFound> {
            service.findByRootId(id, PageRequest.of(0, 10))
        }

        verify(exactly = 1) { thingRepository.findById(id) }
    }
}
