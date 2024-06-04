package org.orkg.contenttypes.adapter.input.rest

import ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.not
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.LegacyCreatePaperUseCase.LegacyCreatePaperRequest
import org.orkg.contenttypes.input.LegacyCreatePaperUseCase.PaperDefinition
import org.orkg.contenttypes.input.LegacyPaperUseCases
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateObjectUseCase.NamedObject
import org.orkg.graph.input.CreateObjectUseCase.ObjectStatement
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createClasses
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Paper Controller")
@Transactional
@Import(MockUserDetailsService::class)
@ImportAutoConfiguration(MigrationsAutoConfiguration::class)
class LegacyPaperControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var legacyPaperService: LegacyPaperUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        predicateService.removeAll()
        resourceService.removeAll()
        classService.removeAll()

        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)

        predicateService.createPredicate(Predicates.hasDOI)
        predicateService.createPredicate(Predicates.hasAuthors)
        predicateService.createPredicate(Predicates.monthPublished)
        predicateService.createPredicate(Predicates.yearPublished)
        predicateService.createPredicate(Predicates.hasResearchField)
        predicateService.createPredicate(Predicates.hasContribution)
        predicateService.createPredicate(Predicates.hasResearchProblem)
        predicateService.createPredicate(Predicates.hasEvaluation)
        predicateService.createPredicate(Predicates.hasURL)
        predicateService.createPredicate(Predicates.hasORCID)
        predicateService.createPredicate(Predicates.hasVenue)
        predicateService.createPredicate(Predicates.hasListElement)

        classService.createClasses("Paper", "Contribution", "Problem", "ResearchField", "Author", "Venue")

        resourceService.createResource(id = "R12", label = "Computer Science")
        resourceService.createResource(id = "CUSTOM_ID", label = "Question Answering over Linked Data")
    }

    @Test
    @TestWithMockUser
    fun add() {
        val paper = mapOf(
            "paper" to mapOf(
                "title" to "test",
                "doi" to "dummy.doi.numbers",
                "researchField" to "R12",
                "publicationYear" to 2015,
                "contributions" to listOf(
                    mapOf(
                        "name" to "Contribution 1",
                        "values" to mapOf(
                            "P32" to listOf(mapOf("@id" to "CUSTOM_ID")),
                            "HAS_EVALUATION" to listOf(mapOf(
                                "@temp" to "_b24c054a-fdde-68a7-c655-d4e7669a2079",
                                "label" to "MOTO"
                            ))
                        )
                    )
                )
            )
        )

        mockMvc
            .perform(postRequestWithBody("/api/papers/", paper))
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("mergeIfExists")
                            .description("Boolean (default = false) to merge newly added contribution in the paper if it already exists")
                            .optional()
                    ),
                    createdResponseHeaders(),
                    paperResponseFields()
                )
            )
    }

    fun createDummyPaperObject(title: String = "long title here", doi: String = "doi.id.here", researchField: String = "R12"): LegacyCreatePaperRequest =
        LegacyCreatePaperRequest(null, PaperDefinition(
            title = title,
            doi = doi,
            researchField = ThingId(researchField),
            publicationYear = 2015,
            contributions = listOf(
                NamedObject(
                    name = "Contribution 1",
                    classes = emptyList(),
                    values = HashMap(
                        mapOf(
                            "P32" to listOf(ObjectStatement(`@id` = "CUSTOM_ID", null, null, null, null, null, null)),
                            "HAS_EVALUATION" to listOf(
                                ObjectStatement(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    label = "MOTO",
                                    null
                                )
                            )
                        )
                    )
                )
            ),
            authors = null,
            publicationMonth = null,
            publishedIn = null,
            url = null,
            extractionMethod = ExtractionMethod.MANUAL
        )
        )

    @Test
    @TestWithMockUser
    fun shouldNotMergeIfDoiIsEmpty() {
        val originalPaper = createDummyPaperObject(doi = "")

        val originalId = legacyPaperService.addPaperContent(originalPaper, false, UUID.randomUUID()).value

        val paperWithEmptyDOI = mapOf(
            "paper" to mapOf(
                "title" to "some title",
                "doi" to "",
                "researchField" to "R12",
                "publicationYear" to 2015,
                "contributions" to listOf(
                    mapOf(
                        "name" to "Contribution 2",
                        "values" to mapOf(
                            "P32" to listOf(mapOf("@id" to "CUSTOM_ID")),
                            "HAS_EVALUATION" to listOf(mapOf(
                                "@temp" to "_b24c054a-fdde-68a7-c655-d4e7669a2079",
                                "label" to "MOTO"
                            ))
                        )
                    )
                )
            )
        )

        mockMvc
            .perform(postRequestWithBody("/api/papers/?mergeIfExists=True", paperWithEmptyDOI))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", not(originalId)))
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("mergeIfExists")
                            .description("Boolean (default = false) to merge newly added contribution in the paper if it already exists")
                            .optional()
                    ),
                    createdResponseHeaders(),
                    paperResponseFields()
                )
            )
    }

    @Test
    @TestWithMockUser
    fun mergePapersThatExistsOnTitle() {
        val originalPaper = createDummyPaperObject()

        val userUUID = UUID.fromString(MockUserId.USER)
        val originalId = legacyPaperService.addPaperContent(originalPaper, false, userUUID).value

        val paperWithSameTitle = mapOf(
            "paper" to mapOf(
                "title" to "long title here",
                "doi" to "dummy.doi.numbers",
                "researchField" to "R12",
                "publicationYear" to 2015,
                "contributions" to listOf(
                    mapOf(
                        "name" to "Contribution 2",
                        "values" to mapOf(
                            "P32" to listOf(mapOf("@id" to "CUSTOM_ID")),
                            "HAS_EVALUATION" to listOf(mapOf(
                                "@temp" to "_b24c054a-fdde-68a7-c655-d4e7669a2079",
                                "label" to "MOTO"
                            ))
                        )
                    )
                )
            )
        )

        mockMvc
            .perform(postRequestWithBody("/api/papers/?mergeIfExists=True", paperWithSameTitle))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(originalId))
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("mergeIfExists")
                            .description("Boolean (default = false) to merge newly added contribution in the paper if it already exists")
                            .optional()
                    ),
                    createdResponseHeaders(),
                    paperResponseFields()
                )
            )
    }

    @Test
    @TestWithMockUser
    fun mergePapersThatExistsOnDoi() {
        val originalPaper = createDummyPaperObject()

        val userUUID = UUID.fromString(MockUserId.USER)
        val originalId = legacyPaperService.addPaperContent(originalPaper, false, userUUID).value

        val paperWithSameDOI = mapOf(
            "paper" to mapOf(
                "title" to "a different title here",
                "doi" to "doi.id.here",
                "researchField" to "R12",
                "publicationYear" to 2015,
                "contributions" to listOf(
                    mapOf(
                        "name" to "Contribution 2",
                        "values" to mapOf(
                            "P32" to listOf(mapOf("@id" to "CUSTOM_ID")),
                            "HAS_EVALUATION" to listOf(mapOf(
                                "@temp" to "_b24c054a-fdde-68a7-c655-d4e7669a2079",
                                "label" to "MOTO"
                            ))
                        )
                    )
                )
            )
        )

        mockMvc
            .perform(postRequestWithBody("/api/papers/?mergeIfExists=True", paperWithSameDOI))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(originalId))
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("mergeIfExists")
                            .description("Boolean (default = false) to merge newly added contribution in the paper if it already exists")
                            .optional()
                    ),
                    createdResponseHeaders(),
                    paperResponseFields()
                )
            )
    }

    @Test
    @TestWithMockUser
    fun mergePapersIfBothTitleAndDoiExist() {
        val originalPaper = createDummyPaperObject()

        val userUUID = UUID.fromString(MockUserId.USER)
        val originalId = legacyPaperService.addPaperContent(originalPaper, false, userUUID).value

        val paperWithSameTitleAndDOI = mapOf(
            "paper" to mapOf(
                "title" to "long title here",
                "doi" to "doi.id.here",
                "researchField" to "R12",
                "publicationYear" to 2015,
                "contributions" to listOf(
                    mapOf(
                        "name" to "Contribution 2",
                        "values" to mapOf(
                            "P32" to listOf(mapOf("@id" to "CUSTOM_ID")),
                            "HAS_EVALUATION" to listOf(mapOf(
                                "@temp" to "_b24c054a-fdde-68a7-c655-d4e7669a2079",
                                "label" to "MOTO"
                            ))
                        )
                    )
                )
            )
        )

        mockMvc
            .perform(postRequestWithBody("/api/papers/?mergeIfExists=True", paperWithSameTitleAndDOI))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(originalId))
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("mergeIfExists")
                            .description("Boolean (default = false) to merge newly added contribution in the paper if it already exists")
                            .optional()
                    ),
                    createdResponseHeaders(),
                    paperResponseFields()
                )
            )
    }

    @Test
    @TestWithMockUser
    fun whenResearchFieldIsBlank400BadRequestIsReturned() {
        val paperWithNoResearchField = mapOf(
            "paper" to mapOf(
                "title" to "long title here",
                "doi" to "doi.id.here",
                "researchField" to "",
                "publicationYear" to 2015,
                "contributions" to emptyMap<String, Any>()
            )
        )

        mockMvc
            .perform(postRequestWithBody("/api/papers/?mergeIfExists=false", paperWithNoResearchField))
            .andExpect(status().isBadRequest)
    }

    @Test
    @Tag("regression")
    @Tag("issue:292")
    @TestWithMockUser
    fun creatingAPaperTwiceWorksAsExpected() {
        resourceService.createResource(
            id = "R106",
            classes = setOf("ResearchField"),
            label = "Some research field required by the example data"
        )

        @Suppress("UNCHECKED_CAST") // This is fine. We know we are dealing with JSON here and do not have "null" keys.
        val paper: Map<String, Any?> =
            objectMapper.readValue(exampleDataFromIssue, Map::class.java) as Map<String, Any?>

        // Create the paper twice. It should create two papers.
        mockMvc
            .perform(postRequestWithBody("/api/papers/?mergeIfExists=false", paper))
            .andExpect(status().isCreated)
        mockMvc
            .perform(postRequestWithBody("/api/papers/?mergeIfExists=false", paper))
            .andExpect(status().isCreated)
    }

    @Test
    fun fetchPapersRelatedToAParticularResource() {
        val predicate1 = predicateService.createPredicate(label = "Predicate 1")
        val predicate2 = predicateService.createPredicate(label = "Predicate 2")

        val relatedPaper1 = resourceService.createResource(setOf("Paper"), label = "Paper 1")
        val relatedPaper2 = resourceService.createResource(setOf("Paper"), label = "Paper 2")
        val unrelatedPaper = resourceService.createResource(setOf("Paper"), label = "Paper 3")
        val intermediateResource = resourceService.createResource(label = "Not interesting")
        val unrelatedResource = resourceService.createResource(label = "Some resource")
        val id = resourceService.createResource(label = "Our resource")

        statementService.create(relatedPaper1, predicate1, id)
        statementService.create(relatedPaper2, predicate2, intermediateResource)
        statementService.create(intermediateResource, predicate1, id)
        statementService.create(unrelatedPaper, predicate1, unrelatedResource)

        mockMvc
            .perform(getRequestTo("/api/papers/?linkedTo=$id&size=50"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    // TODO: figure out how to document this call
                )
            )
    }

    private fun paperResponseFields() =
        responseFields(
            fieldWithPath("id").description("The paper ID"),
            fieldWithPath("label").description("The paper label"),
            fieldWithPath("classes").description("The list of classes the paper belongs to"),
            fieldWithPath("created_at").description("The paper creation datetime"),
            fieldWithPath("shared").description("The number of times this resource is shared").optional(),
            fieldWithPath("created_by").description("The user's ID that created the paper"),
            fieldWithPath("observatory_id").description("The observatory ID that this paper belong to"),
            fieldWithPath("organization_id").description("The organization ID that this paper belong to"),
            fieldWithPath("verified").description("Whether this paper is checked by the curation team or not"),
            fieldWithPath("extraction_method").description("The method of extraction for this paper"),
            fieldWithPath("_class").description("The type of the entity").ignored(),
            fieldWithPath("visibility").optional().ignored(),
            fieldWithPath("featured").optional().ignored(),
            fieldWithPath("unlisted").optional().ignored(),
            fieldWithPath("modifiable").description("Whether this resource can be modified.").optional().ignored(),
            fieldWithPath("formatted_label").description("The formatted label of the resource. See <<content-negotiation,Content Negotiation>> for information on how to obtain this value.").optional()
        )
}

//region Example data for issue #292
@Language("json")
@Suppress("HttpUrlsUsage")
private val exampleDataFromIssue = """
    {
      "paper": {
        "authors": [
          {
            "label": "Robert Challen",
            "orcid": "http://orcid.org/0000-0002-5504-7768"
          },
          {
            "label": "Ellen Brooks-Pollock",
            "orcid": "http://orcid.org/0000-0002-5984-4932"
          },
          {
            "label": "Jonathan M Read",
            "orcid": "http://orcid.org/0000-0002-9697-0962"
          },
          {
            "label": "Louise Dyson",
            "orcid": "http://orcid.org/0000-0001-9788-4858"
          },
          {
            "label": "Krasimira Tsaneva-Atanasova",
            "orcid": "http://orcid.org/0000-0002-6294-7051"
          },
          {
            "label": "Leon Danon",
            "orcid": "http://orcid.org/0000-0002-7076-1871"
          }
        ],
        "contributions": [
          {
            "classes": [
              "Contribution"
            ],
            "name": "Contribution 1",
            "values": {}
          }
        ],
        "doi": "10.1136/bmj.n579",
        "publicationMonth": 3,
        "publicationYear": 2021,
        "publishedIn": "BMJ",
        "researchField": "R106",
        "title": "Risk of mortality in patients infected with SARS-CoV-2 variant of concern 202012/1: matched cohort study",
        "url": ""
      },
      "predicates": []
    }
    """.trimIndent()
//endregion
