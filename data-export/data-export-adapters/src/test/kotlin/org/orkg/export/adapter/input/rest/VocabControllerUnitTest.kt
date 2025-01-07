package org.orkg.export.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.export.adapter.input.rest.configuration.RdfConfiguration
import org.orkg.export.domain.FileExportService
import org.orkg.export.domain.RDFService
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
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [VocabController::class, RdfConfiguration::class, ExceptionHandler::class, RDFService::class, FixedClockConfig::class])
@WebMvcTest(VocabController::class)
internal class VocabControllerUnitTest : RestDocsTest("rdf-vocab") {

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

    @Test
    fun resolveResource() {
        val resourceId = ThingId("R1")
        val resource = createResource(id = resourceId)

        every { resourceRepository.findById(resourceId) } returns Optional.of(resource)
        every { statementRepository.findAll(subjectId = resourceId, pageable = any()) } returns pageOf(
            createStatement(subject = resource, `object` = createLiteral())
        )

        get("/api/vocab/resource/{id}", resourceId)
            .accept("application/rdf+xml")
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    requestHeaders(headerWithName("Accept").description("The RDF media type to return.")),
                    pathParameters(parameterWithName("id").description("The ID of the resource to fetch."))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { resourceRepository.findById(resourceId) }
        verify(exactly = 1) { statementRepository.findAll(subjectId = resourceId, pageable = any()) }
    }

    @Test
    fun resolvePredicate() {
        val predicateId = ThingId("P1")
        every { predicateRepository.findById(predicateId) } returns Optional.of(createPredicate(id = predicateId))

        get("/api/vocab/predicate/{id}", predicateId)
            .accept("text/n3")
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    requestHeaders(headerWithName("Accept").description("The RDF media type to return.")),
                    pathParameters(parameterWithName("id").description("The ID of the predicate to fetch."))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { predicateRepository.findById(predicateId) }
    }

    @Test
    fun resolveClass() {
        val classId = ThingId("C1")
        every { classRepository.findById(classId) } returns Optional.of(createClass(id = classId))

        get("/api/vocab/class/{id}", classId)
            .accept("application/trig")
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    requestHeaders(headerWithName("Accept").description("The RDF media type to return.")),
                    pathParameters(parameterWithName("id").description("The ID of the class to fetch."))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { classRepository.findById(classId) }
    }
}
