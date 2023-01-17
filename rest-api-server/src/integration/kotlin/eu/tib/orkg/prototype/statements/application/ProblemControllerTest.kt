package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createClasses
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.services.PredicateService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Problem Controller")
@Transactional
@Import(MockUserDetailsService::class)
class ProblemControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        predicateService.removeAll()
        resourceService.removeAll()
        classService.removeAll()

        classService.createClasses("Problem", "Contribution")
        predicateService.createPredicate(id = "P32", label = "addresses")
    }

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun getUsersPerProblem() {
        val predicate = PredicateId("P32")

        val problem = resourceService.createResource(
            classes = setOf("Problem"),
            label = "save the world"
        )

        val userEmail = "test@testemail.com"
        if (!userService.findByEmail(userEmail).isPresent)
            userService.registerUser(userEmail, "testTest123", "test_user")
        val uuid = userService.findByEmail(userEmail).get().id!!
        val contributor = ContributorId(uuid)

        val contribution = resourceService.createResource(
            classes = setOf("Contribution"),
            label = "Be healthy",
            userId = contributor,
            extractionMethod = ExtractionMethod.MANUAL
        )

        statementService.create(contributor, contribution.value, predicate, problem.value)

        mockMvc
            .perform(getRequestTo("/api/problems/$problem/users?size=4"))
            .andExpect(status().isOk)
            .andDo { println(it.response.contentAsString) }
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("page").description("Page number of items to fetch (default: 1)").optional(),
                        parameterWithName("size").description("Number of items to fetch per page (default: 10)").optional()
                    ),
                    listOfUsersPerProblemResponseFields()
                )
            )
    }

    companion object RestDoc {

        private fun usersPerProblemResponseFields() = listOf(
            fieldWithPath("user").description("The user object"),
            fieldWithPath("user.id").description("The UUID of the user in the system"),
            fieldWithPath("user.gravatar_id").description("The gravatar id of the user"),
            fieldWithPath("user.display_name").description("The user's display name"),
            fieldWithPath("user.avatar_url").description("The user's avatar url (gravatar url)"),
            fieldWithPath("user.joined_at").description("the datetime when the user was created"),
            fieldWithPath("user.organization_id").description("the organization id that this user belongs to").optional(),
            fieldWithPath("user.observatory_id").description("the observatory id that this user belongs to").optional(),
            fieldWithPath("contributions").description("The number of contributions this user created")
        )

        fun listOfUsersPerProblemResponseFields(): ResponseFieldsSnippet =
            responseFields(fieldWithPath("[]").description("A list of users"))
                .andWithPrefix("[].", usersPerProblemResponseFields())
    }
}
