package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.ResearchFieldHierarchyEntryRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.ResearchFieldHierarchyEntryRepresentation
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldHierarchyUseCase.ResearchFieldHierarchyEntry
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.ResearchFieldHierarchyEntryTest.FakeResearchFieldHierarchyEntryController
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import eu.tib.orkg.prototype.testing.andExpectResearchFieldHierarchyEntry
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [FakeResearchFieldHierarchyEntryController::class])
internal class ResearchFieldHierarchyEntryTest : RestDocsTest("research-fields") {
    @Test
    fun researchFieldHierarchyEntry() {
        mockMvc.perform(documentedGetRequestTo("/hierarchy"))
            .andExpect(status().isOk)
            .andExpectResearchFieldHierarchyEntry()
            .andDo(
                documentationHandler.document(
                    responseFields(
                        fieldWithPath("parent_ids").description("The ids of the parent research fields."),
                        subsectionWithPath("resource").description("Resource representation of the research field resource.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @TestComponent
    @RestController
    internal class FakeResearchFieldHierarchyEntryController(
        override val statementService: StatementUseCases = mockk(),
        override val templateRepository: TemplateRepository = mockk(),
        override val flags: FeatureFlagService = mockk()
    ) : ResearchFieldHierarchyEntryRepresentationAdapter {
        @GetMapping("/hierarchy")
        fun dummyResearchFieldHierarchyEntry(): ResearchFieldHierarchyEntryRepresentation =
            ResearchFieldHierarchyEntry(
                resource = createResource(classes = setOf(ThingId("ResearchField"))),
                parentIds = setOf(ThingId("R123"))
            ).toResearchFieldHierarchyEntryRepresentation(emptyMap(), emptyMap())
    }
}
