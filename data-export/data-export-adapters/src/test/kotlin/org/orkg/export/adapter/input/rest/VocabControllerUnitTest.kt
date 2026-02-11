package org.orkg.export.adapter.input.rest

import com.epages.restdocs.apispec.SimpleType
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.export.adapter.input.rest.configuration.RdfConfiguration
import org.orkg.export.adapter.input.rest.testing.fixtures.configuration.DataExportControllerUnitTestConfiguration
import org.orkg.export.domain.FileExportService
import org.orkg.export.domain.RDFService
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(
    classes = [
        VocabController::class,
        RdfConfiguration::class,
        RDFService::class,
        DataExportControllerUnitTestConfiguration::class,
    ]
)
@WebMvcTest(VocabController::class)
internal class VocabControllerUnitTest : MockMvcBaseTest("rdf-vocab") {
    @MockkBean
    private lateinit var classRepository: ClassRepository

    @MockkBean
    private lateinit var resourceRepository: ResourceRepository

    @MockkBean
    private lateinit var predicateRepository: PredicateRepository

    @MockkBean
    private lateinit var statementRepository: StatementRepository

    @MockkBean // Required by RdfService, not used in test
    private lateinit var fileExportService: FileExportService

    @MockkBean // Required by RdfService, not used in test
    private lateinit var classHierarchyRepository: ClassHierarchyRepository

    private fun resolveResource(accept: String) {
        val resourceId = ThingId("R1")
        val resource = createResource(id = resourceId)

        every { resourceRepository.findById(resourceId) } returns Optional.of(resource)
        every { statementRepository.findAll(subjectId = resourceId, pageable = any()) } returns pageOf(
            createStatement(subject = resource, `object` = createLiteral())
        )

        get("/api/vocab/resource/{id}", resourceId)
            .accept(accept)
            .perform()
            .andExpect(status().isOk)
            .andExpect(header().string(CONTENT_TYPE, startsWith(accept)))
            .andDocument {
                tag("RDF Vocab")
                summary("Resolving resources")
                description(
                    """
                    A `GET` request to get the description of an ORKG resource.
                    """
                )
                requestHeaders(
                    headerWithName("Accept").description("The RDF media type to return.").optional(),
                )
                pathParameters(
                    parameterWithName("id").description("The ID of the resource to fetch."),
                )
                simpleResponse(SimpleType.STRING)
                throws(ResourceNotFound::class)
            }

        verify(exactly = 1) { resourceRepository.findById(resourceId) }
        verify(exactly = 1) { statementRepository.findAll(subjectId = resourceId, pageable = any()) }
    }

    @Test
    fun resolveResource_applicationNTriples() = resolveResource("application/n-triples")

    @Test
    fun resolveResource_textPlain() = resolveResource("text/plain")

    @Test
    fun resolveResource_applicationRdfXml() = resolveResource("application/rdf+xml")

    @Test
    fun resolveResource_applicationXml() = resolveResource("application/xml")

    @Test
    fun resolveResource_textXml() = resolveResource("text/xml")

    @Test
    fun resolveResource_textN3() = resolveResource("text/n3")

    @Test
    fun resolveResource_textRdfN3() = resolveResource("text/rdf+n3")

    @Test
    fun resolveResource_applicationLdJson() = resolveResource("application/ld+json")

    @Test
    fun resolveResource_applicationTrig() = resolveResource("application/trig")

    @Test
    fun resolveResource_applicationXTrig() = resolveResource("application/x-trig")

    @Test
    fun resolveResource_applicationNQuads() = resolveResource("application/n-quads")

    @Test
    fun resolveResource_textXNQuads() = resolveResource("text/x-nquads")

    @Test
    fun resolveResource_textNQuads() = resolveResource("text/nquads")

    @Test
    fun resolveResource_textTurtle() = resolveResource("text/turtle")

    @Test
    fun resolveResource_applicationXTurtle() = resolveResource("application/x-turtle")

    private fun resolvePredicate(accept: String) {
        val predicateId = ThingId("P1")
        every { predicateRepository.findById(predicateId) } returns Optional.of(createPredicate(id = predicateId))

        get("/api/vocab/predicate/{id}", predicateId)
            .accept(accept)
            .perform()
            .andExpect(status().isOk)
            .andExpect(header().string(CONTENT_TYPE, startsWith(accept)))
            .andDocument {
                tag("RDF Vocab")
                summary("Resolving predicates")
                description(
                    """
                    A `GET` request to get the description of an ORKG predicate.
                    """
                )
                requestHeaders(
                    headerWithName("Accept").description("The RDF media type to return.").optional(),
                )
                pathParameters(
                    parameterWithName("id").description("The ID of the predicate to fetch."),
                )
                simpleResponse(SimpleType.STRING)
                throws(PredicateNotFound::class)
            }

        verify(exactly = 1) { predicateRepository.findById(predicateId) }
    }

    @Test
    fun resolvePredicate_applicationNTriples() = resolvePredicate("application/n-triples")

    @Test
    fun resolvePredicate_textPlain() = resolvePredicate("text/plain")

    @Test
    fun resolvePredicate_applicationRdfXml() = resolvePredicate("application/rdf+xml")

    @Test
    fun resolvePredicate_applicationXml() = resolvePredicate("application/xml")

    @Test
    fun resolvePredicate_textXml() = resolvePredicate("text/xml")

    @Test
    fun resolvePredicate_textN3() = resolvePredicate("text/n3")

    @Test
    fun resolvePredicate_textRdfN3() = resolvePredicate("text/rdf+n3")

    @Test
    fun resolvePredicate_applicationLdJson() = resolvePredicate("application/ld+json")

    @Test
    fun resolvePredicate_applicationTrig() = resolvePredicate("application/trig")

    @Test
    fun resolvePredicate_applicationXTrig() = resolvePredicate("application/x-trig")

    @Test
    fun resolvePredicate_applicationNQuads() = resolvePredicate("application/n-quads")

    @Test
    fun resolvePredicate_textXNQuads() = resolvePredicate("text/x-nquads")

    @Test
    fun resolvePredicate_textNQuads() = resolvePredicate("text/nquads")

    @Test
    fun resolvePredicate_textTurtle() = resolvePredicate("text/turtle")

    @Test
    fun resolvePredicate_applicationXTurtle() = resolvePredicate("application/x-turtle")

    private fun resolveClass(accept: String) {
        val classId = ThingId("C1")
        every { classRepository.findById(classId) } returns Optional.of(createClass(id = classId))

        get("/api/vocab/class/{id}", classId)
            .accept(accept)
            .perform()
            .andExpect(status().isOk)
            .andExpect(header().string(CONTENT_TYPE, startsWith(accept)))
            .andDocument {
                tag("RDF Vocab")
                summary("Resolving classes")
                description(
                    """
                    A `GET` request to get the description of an ORKG class.
                    """
                )
                requestHeaders(
                    headerWithName("Accept").description("The RDF media type to return.").optional(),
                )
                pathParameters(
                    parameterWithName("id").description("The ID of the class to fetch."),
                )
                simpleResponse(SimpleType.STRING)
                throws(ClassNotFound::class)
            }

        verify(exactly = 1) { classRepository.findById(classId) }
    }

    @Test
    fun resolveClass_applicationNTriples() = resolveClass("application/n-triples")

    @Test
    fun resolveClass_textPlain() = resolveClass("text/plain")

    @Test
    fun resolveClass_applicationRdfXml() = resolveClass("application/rdf+xml")

    @Test
    fun resolveClass_applicationXml() = resolveClass("application/xml")

    @Test
    fun resolveClass_textXml() = resolveClass("text/xml")

    @Test
    fun resolveClass_textN3() = resolveClass("text/n3")

    @Test
    fun resolveClass_textRdfN3() = resolveClass("text/rdf+n3")

    @Test
    fun resolveClass_applicationLdJson() = resolveClass("application/ld+json")

    @Test
    fun resolveClass_applicationTrig() = resolveClass("application/trig")

    @Test
    fun resolveClass_applicationXTrig() = resolveClass("application/x-trig")

    @Test
    fun resolveClass_applicationNQuads() = resolveClass("application/n-quads")

    @Test
    fun resolveClass_textXNQuads() = resolveClass("text/x-nquads")

    @Test
    fun resolveClass_textNQuads() = resolveClass("text/nquads")

    @Test
    fun resolveClass_textTurtle() = resolveClass("text/turtle")

    @Test
    fun resolveClass_applicationXTurtle() = resolveClass("application/x-turtle")
}
