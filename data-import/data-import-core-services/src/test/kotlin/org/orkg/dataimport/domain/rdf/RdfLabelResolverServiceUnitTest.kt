package org.orkg.dataimport.domain.rdf

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import java.util.UUID

internal class RdfLabelResolverServiceUnitTest : MockkBaseTest {
    private val mockResolver1 = mockk<RdfLabelResolver>()
    private val mockResolver2 = mockk<RdfLabelResolver>()

    private val rdfLabelResolverService = RdfLabelResolverService(listOf(mockResolver1, mockResolver2))

    @Test
    fun `Given an rdf file id and a resource, when resolving the label, and a resolver is applicable, it returns the resolved label`() {
        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI("https://example.org/OK")
        val expected = "resolved"

        every { mockResolver1.canResolve(rdfFileId, resource) } returns true
        every { mockResolver1.resolve(rdfFileId, resource) } returns expected

        rdfLabelResolverService.resolveLabel(rdfFileId, resource) shouldBe expected

        verify(exactly = 1) { mockResolver1.canResolve(rdfFileId, resource) }
        verify(exactly = 1) { mockResolver1.resolve(rdfFileId, resource) }
    }

    @Test
    fun `Given an rdf file id and a resource, when resolving the label, and resource is blank node, it returns an empty label`() {
        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createBNode("b0")
        val expected = ""

        rdfLabelResolverService.resolveLabel(rdfFileId, resource) shouldBe expected
    }

    @ParameterizedTest
    @MethodSource("dirtyLabels")
    fun `Given an rdf file id and a resource, when resolving the label and label needs sanitization, it returns the sanitized label`(resolved: String, sanitized: String) {
        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI("https://example.org/OK")

        every { mockResolver1.canResolve(rdfFileId, resource) } returns true
        every { mockResolver1.resolve(rdfFileId, resource) } returns resolved

        rdfLabelResolverService.resolveLabel(rdfFileId, resource) shouldBe sanitized

        verify(exactly = 1) { mockResolver1.canResolve(rdfFileId, resource) }
        verify(exactly = 1) { mockResolver1.resolve(rdfFileId, resource) }
    }

    @Test
    fun `Given an rdf file id and a resource, when resolving the label, and no resolver is applicable, it returns the resource as string`() {
        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI("https://example.org/OK")
        val expected = resource.stringValue()

        every { mockResolver1.canResolve(rdfFileId, resource) } returns false
        every { mockResolver2.canResolve(rdfFileId, resource) } returns false

        rdfLabelResolverService.resolveLabel(rdfFileId, resource) shouldBe expected

        verify(exactly = 1) { mockResolver1.canResolve(rdfFileId, resource) }
        verify(exactly = 1) { mockResolver2.canResolve(rdfFileId, resource) }
    }

    @Test
    fun `Given an rdf file id and a resource, when resolving the label, and no resolver is applicable, and resource has a fragment, it returns the fragment`() {
        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI("https://example.org/OK#frag")
        val expected = "frag"

        every { mockResolver1.canResolve(rdfFileId, resource) } returns false
        every { mockResolver2.canResolve(rdfFileId, resource) } returns false

        rdfLabelResolverService.resolveLabel(rdfFileId, resource) shouldBe expected

        verify(exactly = 1) { mockResolver1.canResolve(rdfFileId, resource) }
        verify(exactly = 1) { mockResolver2.canResolve(rdfFileId, resource) }
    }

    @Test
    fun `Given an rdf file id and a resource, when resolving the label, and a resolver is applicable but was unable to resolve the label, it proceeds to the next resolver`() {
        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI("https://example.org/OK")
        val expected = "resolved"

        every { mockResolver1.canResolve(rdfFileId, resource) } returns true
        every { mockResolver1.resolve(rdfFileId, resource) } returns null
        every { mockResolver2.canResolve(rdfFileId, resource) } returns true
        every { mockResolver2.resolve(rdfFileId, resource) } returns expected

        rdfLabelResolverService.resolveLabel(rdfFileId, resource) shouldBe expected

        verify(exactly = 1) { mockResolver1.canResolve(rdfFileId, resource) }
        verify(exactly = 1) { mockResolver1.resolve(rdfFileId, resource) }
        verify(exactly = 1) { mockResolver2.canResolve(rdfFileId, resource) }
        verify(exactly = 1) { mockResolver2.resolve(rdfFileId, resource) }
    }

    @Test
    fun `Given an rdf file id and a resource, when resolving the label, and a resolver is not applicable, it proceeds to the next resolver`() {
        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI("https://example.org/OK")
        val expected = "resolved"

        every { mockResolver1.canResolve(rdfFileId, resource) } returns false
        every { mockResolver2.canResolve(rdfFileId, resource) } returns true
        every { mockResolver2.resolve(rdfFileId, resource) } returns expected

        rdfLabelResolverService.resolveLabel(rdfFileId, resource) shouldBe expected

        verify(exactly = 1) { mockResolver1.canResolve(rdfFileId, resource) }
        verify(exactly = 1) { mockResolver2.canResolve(rdfFileId, resource) }
        verify(exactly = 1) { mockResolver2.resolve(rdfFileId, resource) }
    }

    companion object {
        @JvmStatic
        fun dirtyLabels(): List<Arguments> = listOf(
            Arguments.of("contains\nnew\nlines", "contains new lines"),
            Arguments.of("a".repeat(MAX_LABEL_LENGTH + 1), "a".repeat(MAX_LABEL_LENGTH)),
        )
    }
}
