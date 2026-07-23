package org.orkg.graph.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.output.PathRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.pageOf
import org.springframework.data.domain.PageRequest
import java.util.Optional

internal class PathServiceUnitTest : MockkBaseTest {
    private val pathRepository: PathRepository = mockk()
    private val thingRepository: ThingRepository = mockk()

    private val service = PathService(pathRepository, thingRepository)

    @Test
    fun `Given several statements, when fetching all paths by root id, it returns the path`() {
        val id = ThingId("R123")
        val path = pageOf(*arrayOf(listOf(createResource(id), createPredicate(), createClass())))

        every { thingRepository.findById(id) } returns Optional.of(path.first().first())
        every { pathRepository.findAllByRootId(id, any(), any(), any(), any(), any(), any(), any(), any()) } returns path

        service.findAllByRootId(id, PageRequest.of(0, 10)) shouldBe path

        verify(exactly = 1) { thingRepository.findById(id) }
        verify(exactly = 1) { pathRepository.findAllByRootId(id, any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Given several statements, when fetching all paths by root id, but min hops is larger than max bounds, it throws an exception`() {
        shouldThrow<InvalidHopBounds> {
            service.findAllByRootId(
                id = ThingId("R123"),
                minHops = 5,
                maxHops = 2,
                pageable = PageRequest.of(0, 10),
            )
        }
    }

    @Test
    fun `Given several statements, when fetching all paths by root id, but root does not exist, it throws an exception`() {
        val id = ThingId("R123")

        every { thingRepository.findById(id) } returns Optional.empty()

        shouldThrow<ThingNotFound> {
            service.findAllByRootId(id, PageRequest.of(0, 10))
        }

        verify(exactly = 1) { thingRepository.findById(id) }
    }
}
