package org.orkg.dataimport.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.common.IRI
import org.orkg.common.testing.fixtures.Assets.turtle
import org.orkg.common.testing.fixtures.PageRepresentation
import org.orkg.dataimport.domain.configuration.jobs.ImportRdfJobConfiguration
import org.orkg.dataimport.domain.configuration.jobs.ValidateRdfJobConfiguration
import org.orkg.dataimport.domain.configuration.steps.ImportRdfFileStepConfiguration
import org.orkg.dataimport.domain.configuration.steps.ValidateRdfFileStepConfiguration
import org.orkg.dataimport.domain.jobs.JobStatus
import org.orkg.dataimport.domain.rdf.RdfFile
import org.orkg.dataimport.domain.rdf.ResourceToTypeMapping.OrkgEntityType
import org.orkg.dataimport.input.RdfFileUseCases
import org.orkg.dataimport.output.RdfClassRepository
import org.orkg.dataimport.output.RdfFileRepository
import org.orkg.dataimport.output.RdfPredicateRepository
import org.orkg.dataimport.output.RdfResourceRepository
import org.orkg.dataimport.output.RdfTripleRepository
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.annotations.IntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.SpringBatchTestConfiguration
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.module.kotlin.readValue
import java.util.UUID

@IntegrationTest
@Import(
    value = [
        ImportRdfJobConfiguration::class,
        ValidateRdfJobConfiguration::class,
        ImportRdfFileStepConfiguration::class,
        ValidateRdfFileStepConfiguration::class,
        SpringBatchTestConfiguration::class,
    ],
)
@TestPropertySource(properties = ["orkg.import.rdf.enabled=true"])
internal class RdfFileControllerIntegrationTest : MockMvcBaseTest("rdf-files") {
    @Autowired
    private lateinit var rdfFileService: RdfFileUseCases

    @Autowired
    private lateinit var predicateUseCases: PredicateUseCases

    @Autowired
    private lateinit var classUseCases: ClassUseCases

    @Autowired
    private lateinit var resourceUseCases: ResourceUseCases

    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var literalService: LiteralUseCases

    @Autowired
    private lateinit var rdfTripleRepository: RdfTripleRepository

    @Autowired
    private lateinit var rdfClassRepository: RdfClassRepository

    @Autowired
    private lateinit var rdfPredicateRepository: RdfPredicateRepository

    @Autowired
    private lateinit var rdfResourceRepository: RdfResourceRepository

    @Autowired
    private lateinit var rdfFileRepository: RdfFileRepository

    @AfterEach
    fun cleanup() {
        rdfFileService.deleteAll()
        statementService.deleteAll()
        literalService.deleteAll()
        predicateUseCases.deleteAll()
        classUseCases.deleteAll()
        resourceUseCases.deleteAll()
        rdfTripleRepository.deleteAll()
        rdfClassRepository.deleteAll()
        rdfPredicateRepository.deleteAll()
        rdfResourceRepository.deleteAll()
        rdfFileRepository.deleteAll()
    }

    @Test
    @TestWithMockUser
    fun uploadAndValidateAndImport() {
        val name = "aliceKnowsBob.ttl"
        val baseUri = IRI.create("http://orkg.org/orkg/")
        val data = turtle("aliceKnowsBob")

        val id = postMultipart("/api/rdf-files")
            .file(MockMultipartFile("file", name, RdfFile.Format.TURTLE.contentType, data.toByteArray()))
            .part(MockPart("base_uri", baseUri.toString().toByteArray()))
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(UUID::fromString)

        post("/api/rdf-files/{id}/validate", id)
            .perform()
            .andExpect(status().isAccepted)

        val validationStatus = get("/api/rdf-files/{id}/validate", id)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue<JobStatusRepresentation>(it) }

        validationStatus.status shouldBe JobStatus.Status.DONE

        val validationResults = get("/api/rdf-files/{id}/validate/results", id)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue<PageRepresentation<ResourceToTypeMappingRepresentation>>(it) }

        validationResults.asClue { page ->
            page.pageInfo.asClue { pageInfo ->
                pageInfo.size shouldBe 20
                pageInfo.number shouldBe 0
                pageInfo.totalElements shouldBe 7
                pageInfo.totalPages shouldBe 1
            }
            page.content.asClue { content ->
                content[0].asClue {
                    it.iri shouldBe IRI.create("http://example.org/age")
                    it.type shouldBe OrkgEntityType.PREDICATE
                }
                content[1].asClue {
                    it.iri shouldBe IRI.create("http://example.org/Alice")
                    it.type shouldBe OrkgEntityType.RESOURCE
                }
                content[2].asClue {
                    it.iri shouldBe IRI.create("http://example.org/Bob")
                    it.type shouldBe OrkgEntityType.RESOURCE
                }
                content[3].asClue {
                    it.iri shouldBe IRI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
                    it.type shouldBe OrkgEntityType.PREDICATE
                }
                content[4].asClue {
                    it.iri shouldBe IRI.create("http://xmlns.com/foaf/0.1/knows")
                    it.type shouldBe OrkgEntityType.PREDICATE
                }
                content[5].asClue {
                    it.iri shouldBe IRI.create("http://xmlns.com/foaf/0.1/name")
                    it.type shouldBe OrkgEntityType.PREDICATE
                }
                content[6].asClue {
                    it.iri shouldBe IRI.create("http://xmlns.com/foaf/0.1/Person")
                    it.type shouldBe OrkgEntityType.CLASS
                }
            }
        }

        post("/api/rdf-files/{id}/import", id)
            .perform()
            .andExpect(status().isAccepted)

        val importStatus = get("/api/rdf-files/{id}/import", id)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue<JobStatusRepresentation>(it) }

        importStatus.status shouldBe JobStatus.Status.DONE

        val importResults = get("/api/rdf-files/{id}/import/results", id)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue<RdfFileImportResultRepresentation>(it) }

        importResults.asClue {
            it.classCount shouldBe 1
            it.predicateCount shouldBe 4
            it.resourceCount shouldBe 2
            it.statementCount shouldBe 3
        }
    }
}
