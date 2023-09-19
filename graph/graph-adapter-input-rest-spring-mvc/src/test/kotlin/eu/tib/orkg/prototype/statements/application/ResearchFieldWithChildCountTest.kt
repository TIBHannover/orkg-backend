package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.ResearchFieldWithChildCountRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.ResearchFieldWithChildCountRepresentation
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldHierarchyUseCase.ResearchFieldWithChildCount
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.ResearchFieldWithChildCountTest.FakeResearchFieldWithChildCountController
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import eu.tib.orkg.prototype.testing.andExpectResearchFieldWithChildCount
import eu.tib.orkg.prototype.testing.spring.restdocs.RestDocsTest
import eu.tib.orkg.prototype.testing.spring.restdocs.documentedGetRequestTo
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [FakeResearchFieldWithChildCountController::class])
internal class ResearchFieldWithChildCountTest : RestDocsTest("research-fields") {
    @Test
    fun researchFieldWithChildCount() {
        mockMvc.perform(documentedGetRequestTo("/subfield"))
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
        override val templateRepository: TemplateRepository = mockk(),
        override val flags: FeatureFlagService = mockk()
    ) : ResearchFieldWithChildCountRepresentationAdapter {
        @GetMapping("/subfield")
        fun dummySubResearchField(): ResearchFieldWithChildCountRepresentation =
            ResearchFieldWithChildCount(createResource().copy(classes = setOf(ThingId("ResearchField"))), 5)
                .toResearchFieldWithChildCountRepresentation(emptyMap(), emptyMap())
    }
}
