package org.orkg.contenttypes.adapter.input.rest

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.FilterConfigDocTest.FakeController
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.SearchFilter.Operator
import org.orkg.graph.domain.SearchFilter.Value
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [FakeController::class, ContentTypeControllerUnitTestConfiguration::class])
internal class FilterConfigDocTest : MockMvcBaseTest("filter-configs") {
    @Test
    fun getSingle() {
        documentedGetRequestTo("/filter-config")
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                responseFields<SearchFilter>(
                    fieldWithPath("path").description("Describes the path from the contribution node of a paper to the node that should be matched, where every entry stands for the predicate id of a statement. For example the path provided in the example above matches node n in the following graph: (:Paper)-[P31]->(:Contribution)-[P105059]->(:Thing)-[P60013]->(n:Thing)"),
                    fieldWithPath("range").description("The class id that represents the range of the value that should be matched. Subclasses will also be considered when matching."),
                    fieldWithPath("values").description("A list that contains the actual values that should be matched. Multiple entries are logically combined with an OR operation, while multiple filter configs are logically combined with an AND operation."),
                    fieldWithPath("values[].op").description("The logical matching operation."),
                    fieldWithPath("values[].value").description("The value that needs to match."),
                    fieldWithPath("exact").description("Whether to exactly match the given path. If `true`, the given path needs to exactly match, starting from the contribution resource. If `false`, the given path needs to exactly match, starting at any node in the subgraph of the contribution or the contribution node itself. The total path length is limited to 10, including the length of the specified path, starting from the contribution node."),
                )
            }
    }

    @TestComponent
    @RestController
    internal class FakeController {
        @GetMapping("/filter-config")
        fun getSingle() = SearchFilter(
            path = listOf(ThingId("P105059"), ThingId("P60013")),
            range = ThingId("Pattern"),
            values = setOf(
                Value(
                    op = Operator.EQ,
                    value = "some value"
                )
            ),
            exact = true
        )
    }
}
