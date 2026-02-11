package org.orkg.dataimport.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.Assets.csv
import org.orkg.common.testing.fixtures.PageRepresentation
import org.orkg.contenttypes.adapter.input.rest.AuthorRepresentation
import org.orkg.createClasses
import org.orkg.createPredicate
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.dataimport.domain.configuration.jobs.ImportPaperCSVJobConfiguration
import org.orkg.dataimport.domain.configuration.jobs.ValidatePaperCSVJobConfiguration
import org.orkg.dataimport.domain.configuration.steps.CSVStepConfiguration
import org.orkg.dataimport.domain.configuration.steps.ImportPaperCSVStepConfiguration
import org.orkg.dataimport.domain.configuration.steps.PaperCSVStepConfiguration
import org.orkg.dataimport.domain.configuration.steps.ValidatePaperCSVStepConfiguration
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordImportResult
import org.orkg.dataimport.domain.jobs.JobStatus
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.PostgresContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.JpaTransactionManagerConfiguration
import org.orkg.testing.configuration.SpringBatchTestConfiguration
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.module.kotlin.readValue

@Neo4jContainerIntegrationTest
@PostgresContainerIntegrationTest
@Import(
    value = [
        ImportPaperCSVJobConfiguration::class,
        ValidatePaperCSVJobConfiguration::class,
        CSVStepConfiguration::class,
        ImportPaperCSVStepConfiguration::class,
        PaperCSVStepConfiguration::class,
        ValidatePaperCSVStepConfiguration::class,
        SpringBatchTestConfiguration::class,
        JpaTransactionManagerConfiguration::class,
    ]
)
internal class CSVControllerIntegrationTest : MockMvcBaseTest("csvs") {
    @Autowired
    private lateinit var predicateUseCases: PredicateUseCases

    @Autowired
    private lateinit var classUseCases: ClassUseCases

    @Autowired
    private lateinit var resourceUseCases: ResourceUseCases

    @BeforeEach
    fun setup() {
        predicateUseCases.createPredicates(
            Predicates.hasDOI,
            Predicates.hasISBN,
            Predicates.hasISSN,
            Predicates.hasAuthors,
            Predicates.monthPublished,
            Predicates.yearPublished,
            Predicates.hasResearchField,
            Predicates.hasContribution,
            Predicates.hasURL,
            Predicates.hasResearchProblem,
            Predicates.hasEvaluation,
            Predicates.hasORCID,
            Predicates.hasVenue,
            Predicates.hasWebsite,
            Predicates.description,
            Predicates.hasListElement,
            Predicates.sustainableDevelopmentGoal,
            Predicates.mentions,
        )

        classUseCases.createClasses(
            Classes.paper,
            Classes.contribution,
            Classes.problem,
            Classes.researchField,
            Classes.author,
            Classes.venue,
            Classes.sustainableDevelopmentGoal,
        )

        // Example specific entities

        predicateUseCases.createPredicate(ThingId("P2"))
        resourceUseCases.createResource(id = ThingId("R456"), classes = setOf(Classes.researchField))
        resourceUseCases.createResource(id = ThingId("R146"), classes = setOf(Classes.researchField))
    }

    @Test
    @TestWithMockUser
    fun uploadAndValidateAndImport() {
        val name = "papers.csv"
        val type = CSV.Type.PAPER
        val format = CSV.Format.DEFAULT
        val data = csv("papersWithoutDOI")

        val id = postMultipart("/api/csvs")
            .file(MockMultipartFile("file", name, "text/csv", data.toByteArray()))
            .part(MockPart("type", type.name.toByteArray()))
            .part(MockPart("format", format.name.toByteArray()))
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(::CSVID)

        post("/api/csvs/{id}/validate", id)
            .perform()
            .andExpect(status().isAccepted)

        val validationStatus = get("/api/csvs/{id}/validate", id)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue<JobStatusRepresentation>(it) }

        validationStatus.status shouldBe JobStatus.Status.DONE

        val validationResults = get("/api/csvs/{id}/validate/results", id)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue<PageRepresentation<PaperCSVRecordRepresentation>>(it) }

        validationResults.asClue { page ->
            page.pageInfo.asClue { pageInfo ->
                pageInfo.size shouldBe 20
                pageInfo.number shouldBe 0
                pageInfo.totalElements shouldBe 2
                pageInfo.totalPages shouldBe 1
            }
            page.content.asClue { content ->
                content[0].asClue {
                    it.id shouldNotBe null
                    it.csvId shouldBe id
                    it.itemNumber shouldBe 1
                    it.lineNumber shouldBe 2
                    it.title shouldBe "Dummy Paper Title"
                    it.authors shouldBe listOf(
                        AuthorRepresentation(
                            id = null,
                            name = "Josiah Stinkney Carberry",
                            identifiers = emptyMap(),
                            homepage = null,
                        ),
                        AuthorRepresentation(
                            id = null,
                            name = "Author 2",
                            identifiers = emptyMap(),
                            homepage = null,
                        ),
                    )
                    it.publishedMonth shouldBe 4
                    it.publishedYear shouldBe 2023
                    it.publishedIn shouldBe "Fancy Conference"
                    it.url shouldBe ParsedIRI("https://example.org")
                    it.doi shouldBe null
                    it.researchFieldId shouldBe ThingId("R456")
                    it.extractionMethod shouldBe ExtractionMethod.MANUAL
                    it.statements shouldBe setOf(
                        NewPredicateContributionStatementRepresentation(
                            `object` = TypedValueRepresentation(null, "Handbook", Classes.resource),
                            predicateLabel = "category"
                        ),
                        ExistingPredicateContributionStatementRepresentation(
                            `object` = TypedValueRepresentation("resource", "DOI", Classes.resource),
                            predicateId = ThingId("P2")
                        ),
                        NewPredicateContributionStatementRepresentation(
                            `object` = TypedValueRepresentation("resource", "Result", Classes.resource),
                            predicateLabel = "result"
                        ),
                        NewPredicateContributionStatementRepresentation(
                            `object` = TypedValueRepresentation(null, "5", Classes.integer),
                            predicateLabel = "numericValue"
                        ),
                        ExistingPredicateContributionStatementRepresentation(
                            `object` = TypedValueRepresentation(null, "DOI Handbook", Classes.string),
                            predicateId = ThingId("description")
                        ),
                        ExistingPredicateContributionStatementRepresentation(
                            `object` = TypedValueRepresentation(null, "Complicated Research Problem", Classes.problem),
                            predicateId = ThingId("P32")
                        ),
                    )
                }
                content[1].asClue {
                    it.id shouldNotBe null
                    it.csvId shouldBe id
                    it.itemNumber shouldBe 2
                    it.lineNumber shouldBe 3
                    it.title shouldBe "Other Paper Title"
                    it.authors shouldBe listOf(
                        AuthorRepresentation(
                            id = null,
                            name = "Author 1",
                            identifiers = emptyMap(),
                            homepage = null,
                        ),
                    )
                    it.publishedMonth shouldBe 7
                    it.publishedYear shouldBe 2010
                    it.publishedIn shouldBe "Other Conference"
                    it.url shouldBe null
                    it.doi shouldBe null
                    it.researchFieldId shouldBe ThingId("R146")
                    it.extractionMethod shouldBe ExtractionMethod.AUTOMATIC
                    it.statements shouldBe setOf(
                        NewPredicateContributionStatementRepresentation(
                            `object` = TypedValueRepresentation(null, "Book", Classes.resource),
                            predicateLabel = "category"
                        ),
                        ExistingPredicateContributionStatementRepresentation(
                            `object` = TypedValueRepresentation("resource", "ORKG", Classes.resource),
                            predicateId = ThingId("P2")
                        ),
                        NewPredicateContributionStatementRepresentation(
                            `object` = TypedValueRepresentation("resource", "New Result", Classes.resource),
                            predicateLabel = "result"
                        ),
                        NewPredicateContributionStatementRepresentation(
                            `object` = TypedValueRepresentation(null, "10", Classes.integer),
                            predicateLabel = "numericValue"
                        ),
                        ExistingPredicateContributionStatementRepresentation(
                            `object` = TypedValueRepresentation(null, "Some description", Classes.string),
                            predicateId = ThingId("description")
                        ),
                        ExistingPredicateContributionStatementRepresentation(
                            `object` = TypedValueRepresentation(null, "Super Complicated Research Problem", Classes.problem),
                            predicateId = ThingId("P32")
                        ),
                    )
                }
            }
        }

        post("/api/csvs/{id}/import", id)
            .perform()
            .andExpect(status().isAccepted)

        val importStatus = get("/api/csvs/{id}/import", id)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue<JobStatusRepresentation>(it) }

        importStatus.status shouldBe JobStatus.Status.DONE

        val importResults = get("/api/csvs/{id}/import/results", id)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue<PageRepresentation<PaperCSVRecordImportResultRepresentation>>(it) }

        importResults.asClue { page ->
            page.pageInfo.asClue { pageInfo ->
                pageInfo.size shouldBe 20
                pageInfo.number shouldBe 0
                pageInfo.totalElements shouldBe 2
                pageInfo.totalPages shouldBe 1
            }
            page.content.asClue { content ->
                content[0].asClue {
                    it.csvId shouldBe id
                    it.id shouldNotBe null
                    it.importedEntityId shouldNotBe null
                    it.importedEntityType shouldBe PaperCSVRecordImportResult.Type.PAPER
                    it.itemNumber shouldBe 1
                    it.lineNumber shouldBe 2
                }
                content[1].asClue {
                    it.csvId shouldBe id
                    it.id shouldNotBe null
                    it.importedEntityId shouldNotBe null
                    it.importedEntityType shouldBe PaperCSVRecordImportResult.Type.PAPER
                    it.itemNumber shouldBe 2
                    it.lineNumber shouldBe 3
                }
            }
        }
    }
}
