package org.orkg.contenttypes.adapter.input.rest

import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.input.rest.ResearchFieldHierarchyEntryTest.FakeResearchFieldHierarchyEntryController
import org.orkg.contenttypes.adapter.input.rest.mapping.ResearchFieldHierarchyEntryRepresentationAdapter
import org.orkg.contenttypes.domain.ResearchFieldHierarchyEntry
import org.orkg.graph.adapter.input.rest.ResearchFieldHierarchyEntryRepresentation
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.andExpectResearchFieldHierarchyEntry
import org.orkg.testing.configuration.FixedClockConfig
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
@ContextConfiguration(classes = [FakeResearchFieldHierarchyEntryController::class, CommonJacksonModule::class, FixedClockConfig::class])
internal class ResearchFieldHierarchyEntryTest : MockMvcBaseTest("research-fields") {
    @Test
    fun researchFieldHierarchyEntry() {
        documentedGetRequestTo("/hierarchy")
            .perform()
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
        override val formattedLabelService: FormattedLabelUseCases = mockk(),
    ) : ResearchFieldHierarchyEntryRepresentationAdapter {
        @GetMapping("/hierarchy")
        fun dummyResearchFieldHierarchyEntry(): ResearchFieldHierarchyEntryRepresentation =
            ResearchFieldHierarchyEntry(
                resource = createResource(classes = setOf(Classes.researchField)),
                parentIds = setOf(ThingId("R123"))
            ).toResearchFieldHierarchyEntryRepresentation(emptyMap(), emptyMap())
    }
}
