package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Object
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Statement
import eu.tib.orkg.prototype.statements.domain.model.StatementRepository
import eu.tib.orkg.prototype.statements.infrastructure.InMemoryStatementRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("Statement Controller")
class StatementControllerTest : RestDocumentationBaseTest() {

    private val repository: StatementRepository =
        InMemoryStatementRepository()

    override fun createController() = StatementController(repository)

    @Test
    fun index() {
        repository.add(
            Statement(
                repository.nextIdentity(),
                ResourceId("123"),
                PredicateId("P576"),
                Object.Resource(ResourceId("789"))
            )
        )
        repository.add(
            Statement(
                repository.nextIdentity(),
                ResourceId("123"),
                PredicateId("P432"),
                Object.Resource(ResourceId("633"))
            )
        )

        mockMvc
            .perform(
                get("/api/statements/")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("[].statementId").description(
                            "The statement ID"
                        ),
                        fieldWithPath("[].subject").description(
                            "The resource ID"
                        ),
                        fieldWithPath("[].predicate").description(
                            "The predicate ID"
                        ),
                        subsectionWithPath("[].object").description(
                            "The type of object"
                        )
                    )
                )
            )
    }

    @Test
    fun fetch() {
        repository.add(
            Statement(
                1,
                ResourceId("123"),
                PredicateId("P576"),
                Object.Resource(ResourceId("789"))
            )
        )
        repository.add(
            Statement(
                2,
                ResourceId("123"),
                PredicateId("P432"),
                Object.Resource(ResourceId("633"))
            )
        )

        mockMvc
            .perform(
                get("/api/statements/2")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("statementId").description(
                            "The statement ID"
                        ),
                        fieldWithPath("subject").description(
                            "The resource ID"
                        ),
                        fieldWithPath("predicate").description(
                            "The predicate ID"
                        ),
                        subsectionWithPath("object").description(
                            "The type of object"
                        )
                    )
                )
            )
    }

    @Test
    fun lookupBySubject() {
        repository.add(
            Statement(
                repository.nextIdentity(),
                ResourceId("123"),
                PredicateId("P576"),
                Object.Resource(ResourceId("789"))
            )
        )
        repository.add(
            Statement(
                repository.nextIdentity(),
                ResourceId("123"),
                PredicateId("P432"),
                Object.Resource(ResourceId("633"))
            )
        )

        mockMvc
            .perform(
                get("/api/statements/subject/123")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("[].statementId").description(
                            "The statement ID"
                        ),
                        fieldWithPath("[].subject").description(
                            "The resource ID"
                        ),
                        fieldWithPath("[].predicate").description(
                            "The predicate ID"
                        ),
                        subsectionWithPath("[].object").description(
                            "The type of object"
                        )
                    )
                )
            )
    }

    @Test
    fun lookupByPredicate() {
        repository.add(
            Statement(
                repository.nextIdentity(),
                ResourceId("123"),
                PredicateId("P576"),
                Object.Resource(ResourceId("789"))
            )
        )
        repository.add(
            Statement(
                repository.nextIdentity(),
                ResourceId("345"),
                PredicateId("P576"),
                Object.Resource(ResourceId("633"))
            )
        )

        mockMvc
            .perform(
                get("/api/statements/predicate/P576")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("[].statementId").description(
                            "The statement ID"
                        ),
                        fieldWithPath("[].subject").description(
                            "The resource ID"
                        ),
                        fieldWithPath("[].predicate").description(
                            "The predicate ID"
                        ),
                        subsectionWithPath("[].object").description(
                            "The type of object"
                        )
                    )
                )
            )
    }

    @Test
    fun addWithResource() {
        mockMvc.perform(
            post(
                "/api/statements/{subject}/{predicate}/{object}",
                "123",
                "P234",
                "345"
            )
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    pathParameters(
                        parameterWithName("subject").description("The resource ID describing the subject"),
                        parameterWithName("predicate").description("The predicate ID describing the predicate"),
                        parameterWithName("object").description("The resource ID describing the object")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("Location to the created statement")
                    )
                )
            )
    }

    @Test
    fun addWithLiteral() {
        val value = mapOf(
            "value" to "some value",
            "type" to "literal"
        )
        mockMvc.perform(
            post(
                "/api/statements/{subject}/{predicate}",
                "123",
                "P234"
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(value))
        )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    pathParameters(
                        parameterWithName("subject").description("The resource ID describing the subject"),
                        parameterWithName("predicate").description("The predicate ID describing the predicate")
                    ),
                    requestFields(
                        fieldWithPath("value").description("The literal value"),
                        fieldWithPath("type").description("The type of object. Must be \"literal\".")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("Location to the created statement")
                    )
                )
            )
    }
}
