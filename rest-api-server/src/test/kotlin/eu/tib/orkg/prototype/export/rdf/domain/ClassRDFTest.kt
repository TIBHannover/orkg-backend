package eu.tib.orkg.prototype.export.rdf.domain

import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository
import io.mockk.every
import io.mockk.mockk
import java.io.StringWriter
import java.net.URI
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Suppress("HttpUrlsUsage")
internal class ClassRDFTest {
    private val classHierarchyRepository: ClassHierarchyRepository = mockk()

    @Test
    fun `converts to NTriple format correctly when URI is null`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/2000/01/rdf-schema#label> "some dummy label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val `class` = createClass().copy(id = ThingId("C100"), label = "some dummy label", uri = null)

        every { classHierarchyRepository.findParent(`class`.id) } returns Optional.empty()

        val writer = StringWriter()
        `class`.toNTriple(writer, classHierarchyRepository)

        assertThat(writer.toString()).isEqualTo(expectedOutput)
    }

    @Test
    fun `converts to NTriple format correctly when URI equals null as value`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/2000/01/rdf-schema#label> "some dummy label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val `class` = createClass().copy(id = ThingId("C100"), label = "some dummy label", uri = URI("null"))

        every { classHierarchyRepository.findParent(`class`.id) } returns Optional.empty()

        val writer = StringWriter()
        `class`.toNTriple(writer, classHierarchyRepository)

        assertThat(writer.toString()).isEqualTo(expectedOutput)
    }

    @Test
    fun `converts to NTriple format correctly when URI is set`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/2002/07/owl#equivalentClass> <https://example.org/OK> .
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/2000/01/rdf-schema#label> "some dummy label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val `class` = createClass().copy(id = ThingId("C100"), label = "some dummy label")

        every { classHierarchyRepository.findParent(`class`.id) } returns Optional.empty()

        val writer = StringWriter()
        `class`.toNTriple(writer, classHierarchyRepository)

        assertThat(writer.toString()).isEqualTo(expectedOutput)
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
        val `class` = createClass().copy(id = ThingId("C100"), label = "some dummy label")
        val parent = createClass().copy(id = ThingId("C101"))

        every { classHierarchyRepository.findParent(`class`.id) } returns Optional.of(parent)

        val writer = StringWriter()
        `class`.toNTriple(writer, classHierarchyRepository)

        assertThat(writer.toString()).isEqualTo(expectedOutput)
    }
}
