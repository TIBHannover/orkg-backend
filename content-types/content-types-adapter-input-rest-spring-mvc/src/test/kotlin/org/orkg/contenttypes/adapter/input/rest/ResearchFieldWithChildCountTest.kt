package org.orkg.contenttypes.adapter.input.rest

import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.adapter.input.rest.ResearchFieldWithChildCountTest.FakeResearchFieldWithChildCountController
import org.orkg.contenttypes.adapter.input.rest.mapping.ResearchFieldWithChildCountRepresentationAdapter
import org.orkg.contenttypes.domain.ResearchFieldWithChildCount
import org.orkg.graph.adapter.input.rest.ResearchFieldWithChildCountRepresentation
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.andExpectResearchFieldWithChildCount
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [FakeResearchFieldWithChildCountController::class])
internal class ResearchFieldWithChildCountTest : MockMvcBaseTest("research-fields") {
    @Test
    fun researchFieldWithChildCount() {
        documentedGetRequestTo("/subfield")
            .perform()
            .andExpect(status().isOk)
            .andExpectResearchFieldWithChildCount()
            .andDo(
                documentationHandler.document(
                    responseFields(
                        fieldWithPath("child_count").description("The count of direct subfields that this research field has."),
                        subsectionWithPath("resource").description("Resource representation of the research field resource.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @TestComponent
    @RestController
    internal class FakeResearchFieldWithChildCountController(
        override val statementService: StatementUseCases = mockk(),
        override val formattedLabelService: FormattedLabelUseCases = mockk(),
    ) : ResearchFieldWithChildCountRepresentationAdapter {
        @GetMapping("/subfield")
        fun dummySubResearchField(): ResearchFieldWithChildCountRepresentation =
            ResearchFieldWithChildCount(createResource(classes = setOf(Classes.researchField)), 5)
                .toResearchFieldWithChildCountRepresentation(emptyMap(), emptyMap())
    }
}
