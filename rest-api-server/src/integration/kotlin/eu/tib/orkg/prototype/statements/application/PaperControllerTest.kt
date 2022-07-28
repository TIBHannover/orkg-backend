package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.application.service.CreatePaperRequest
import eu.tib.orkg.prototype.statements.application.service.Paper
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.services.PredicateService
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder

@DisplayName("Paper Controller")
@Transactional
@Import(MockUserDetailsService::class)
class PaperControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var paperController: PaperController

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        predicateService.removeAll()
        resourceService.removeAll()
        classService.removeAll()

        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)

        predicateService.create(CreatePredicateRequest(PredicateId("P26"), "Has DOI"))
        predicateService.create(CreatePredicateRequest(PredicateId("P27"), "Has Author"))
        predicateService.create(CreatePredicateRequest(PredicateId("P28"), "Has publication month"))
        predicateService.create(CreatePredicateRequest(PredicateId("P29"), "Has publication year"))
        predicateService.create(CreatePredicateRequest(PredicateId("P30"), "Has Research field"))
        predicateService.create(CreatePredicateRequest(PredicateId("P31"), "Has contribution"))
        predicateService.create(CreatePredicateRequest(PredicateId("P32"), "Has research problem"))
        predicateService.create(CreatePredicateRequest(PredicateId("HAS_EVALUATION"), "Has evaluation"))
        predicateService.create(CreatePredicateRequest(PredicateId("url"), "Has url"))
        predicateService.create(CreatePredicateRequest(PredicateId("HAS_ORCID"), "Has ORCID"))
        predicateService.create(CreatePredicateRequest(PredicateId("HAS_VENUE"), "Has Venue"))

        classService.create(CreateClassRequest(ClassId("Paper"), "paper", null))
        classService.create(CreateClassRequest(ClassId("Contribution"), "Contribution", null))

        resourceService.create(CreateResourceRequest(ResourceId("R12"), "Computer Science"))
        resourceService.create(CreateResourceRequest(ResourceId("R3003"), "Question Answering over Linked Data"))
    }

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
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
                            "P32" to listOf(mapOf("@id" to "R3003")),
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

    private fun createDummyPaperObject(title: String = "long title here", doi: String = "doi.id.here"): CreatePaperRequest =
        CreatePaperRequest(null, Paper(
            title = title,
            doi = doi,
            researchField = "R12",
            publicationYear = 2015,
            contributions = listOf(
                NamedObject(
                    name = "Contribution 1",
                    classes = emptyList(),
                    values = HashMap(mapOf(
                        "P32" to listOf(ObjectStatement(`@id` = "R3003", "resource", null, null, null, null, null, null)),
                        "HAS_EVALUATION" to listOf(ObjectStatement(null, "resource", null, null, null, null, label = "MOTO", null))
                    ))
                )),
            authors = null,
            publicationMonth = null,
            publishedIn = null,
            url = null,
            extractionMethod = ExtractionMethod.MANUAL
        )
        )

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun `shouldn't merge if DOI is empty`() {

        val originalPaper = createDummyPaperObject(doi = "")

        val originalId = paperController.add(originalPaper, UriComponentsBuilder.fromUriString("localhost"), false).body!!.id.value

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
                            "P32" to listOf(mapOf("@id" to "R3003")),
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
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun `merge papers that exists on title`() {

        val originalPaper = createDummyPaperObject()

        val originalId = paperController.add(originalPaper, UriComponentsBuilder.fromUriString("localhost"), false).body!!.id.value

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
                            "P32" to listOf(mapOf("@id" to "R3003")),
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
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun `merge papers that exists on doi`() {

        val originalPaper = createDummyPaperObject()

        val originalId = paperController.add(originalPaper, UriComponentsBuilder.fromUriString("localhost"), false).body!!.id.value

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
                            "P32" to listOf(mapOf("@id" to "R3003")),
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
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun `merge papers if both title and DOI exist`() {

        val originalPaper = createDummyPaperObject()

        val originalId = paperController.add(originalPaper, UriComponentsBuilder.fromUriString("localhost"), false).body!!.id.value

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
                            "P32" to listOf(mapOf("@id" to "R3003")),
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
            fieldWithPath("featured").optional().ignored(),
            fieldWithPath("unlisted").optional().ignored()
        )
}
