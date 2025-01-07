package org.orkg.export.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.StringWriter
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.testing.fixtures.createClass

@Suppress("HttpUrlsUsage")
internal class ClassNTripleSerializationTest : MockkBaseTest {
    private val classHierarchyRepository: ClassHierarchyRepository = mockk()

    @Test
    fun `converts to NTriple format correctly when URI is null`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/2000/01/rdf-schema#label> "some dummy label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val `class` = createClass(id = ThingId("C100"), label = "some dummy label", uri = null)

        every { classHierarchyRepository.findParent(`class`.id) } returns Optional.empty()

        val writer = StringWriter()
        `class`.toNTriple(writer, classHierarchyRepository)

        assertThat(writer.toString()).isEqualTo(expectedOutput)

        verify(exactly = 1) { classHierarchyRepository.findParent(`class`.id) }
    }

    @Test
    fun `converts to NTriple format correctly when URI is set`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/2002/07/owl#equivalentClass> <https://example.org/OK> .
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/2000/01/rdf-schema#label> "some dummy label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val `class` = createClass(id = ThingId("C100"), label = "some dummy label")

        every { classHierarchyRepository.findParent(`class`.id) } returns Optional.empty()

        val writer = StringWriter()
        `class`.toNTriple(writer, classHierarchyRepository)

        assertThat(writer.toString()).isEqualTo(expectedOutput)

        verify(exactly = 1) { classHierarchyRepository.findParent(`class`.id) }
    }

    @Test
    fun `converts to NTriple format correctly when parent class exists`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/2002/07/owl#equivalentClass> <https://example.org/OK> .
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/2000/01/rdf-schema#label> "some dummy label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://orkg.org/orkg/class/C101> .
            |
        """.trimMargin()
        val `class` = createClass(id = ThingId("C100"), label = "some dummy label")
        val parent = createClass(id = ThingId("C101"))

        every { classHierarchyRepository.findParent(`class`.id) } returns Optional.of(parent)

        val writer = StringWriter()
        `class`.toNTriple(writer, classHierarchyRepository)

        assertThat(writer.toString()).isEqualTo(expectedOutput)

        verify(exactly = 1) { classHierarchyRepository.findParent(`class`.id) }
    }
}
