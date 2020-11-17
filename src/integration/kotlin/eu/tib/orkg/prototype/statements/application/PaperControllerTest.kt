package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Paper Controller")
@Transactional
@Import(MockUserDetailsService::class)
class PaperControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var controller: PaperController

    @Autowired
    private lateinit var service: PredicateService

    @Autowired
    private lateinit var resourceService: ResourceService

    @Autowired
    private lateinit var classService: ClassService

    override fun createController() = controller

    @Test
    @Disabled("Broken, due to problems with fixed IDs. See GitLab issue #94.")
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun add() {
        service.create(CreatePredicateRequest(PredicateId("P26"), "Has DOI"))
        service.create(CreatePredicateRequest(PredicateId("P27"), "Has Author"))
        service.create(CreatePredicateRequest(PredicateId("P28"), "Has publication month"))
        service.create(CreatePredicateRequest(PredicateId("P29"), "Has publication year"))
        service.create(CreatePredicateRequest(PredicateId("P30"), "Has Research field"))
        service.create(CreatePredicateRequest(PredicateId("P31"), "Has contribution"))
        service.create(CreatePredicateRequest(PredicateId("P32"), "Has research problem"))
        service.create(CreatePredicateRequest(PredicateId("HAS_EVALUATION"), "Has evaluation"))
        service.create(CreatePredicateRequest(PredicateId("url"), "Has url"))
        service.create(CreatePredicateRequest(PredicateId("HAS_ORCID"), "Has ORCID"))
        service.create(CreatePredicateRequest(PredicateId("HAS_VENUE"), "Has Venue"))

        resourceService.create(CreateResourceRequest(ResourceId("R12"), "Computer Science"))
        resourceService.create(CreateResourceRequest(ResourceId("R3003"), "Question Answering over Linked Data"))

        classService.create(CreateClassRequest(ClassId("Paper"), "Paper", null))

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
                    RequestDocumentation.requestParameters(
                        RequestDocumentation.parameterWithName("mergeIfExists")
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
            fieldWithPath("_class").description("The type of the entity").ignored()
        )
}
