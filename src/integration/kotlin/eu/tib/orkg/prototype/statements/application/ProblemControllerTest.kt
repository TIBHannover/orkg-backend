package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementService
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
    private lateinit var resourceService: ResourceService

    @Autowired
    private lateinit var classService: ClassService

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var statementService: StatementService

    @Autowired
    private lateinit var userService: UserService

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun getUsersPerProblem() {
        val problemClassId = classService.create(CreateClassRequest(ClassId("Problem"), "Problem", null)).id!!
        val contributionClassId = classService.create(CreateClassRequest(ClassId("Contribution"), "Contribution", null)).id!!
        val predicate = predicateService.create(CreatePredicateRequest(PredicateId("P32"), "addresses")).id!!

        val problem = resourceService.create(CreateResourceRequest(null, "save the world", setOf(problemClassId))).id!!

        val userEmail = "test@testemail.com"
        if (!userService.findByEmail(userEmail).isPresent)
            userService.registerUser(userEmail, "testTest123", "test_user")
        val uuid = userService.findByEmail(userEmail).get().id!!
        val contributor = ContributorId(uuid)

        val contribution = resourceService.create(
            contributor,
            CreateResourceRequest(null, "Be healthy", setOf(contributionClassId)),
            ObservatoryId.createUnknownObservatory(),
            ExtractionMethod.MANUAL,
            OrganizationId.createUnknownOrganization()
        ).id!!

        statementService.create(contributor, contribution.value, predicate, problem.value)

        mockMvc
            .perform(getRequestTo("/api/problems/$problem/users?items=4"))
            .andExpect(status().isOk)
            .andDo { println(it.response.contentAsString) }
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("page").description("Page number of items to fetch (default: 1)").optional(),
                        parameterWithName("items").description("Number of items to fetch per page (default: 10)").optional()
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
