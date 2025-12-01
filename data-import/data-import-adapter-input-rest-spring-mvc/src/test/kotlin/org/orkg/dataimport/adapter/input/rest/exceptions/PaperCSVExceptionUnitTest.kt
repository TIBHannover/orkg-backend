package org.orkg.dataimport.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.dataimport.adapter.input.rest.json.DataImportJacksonModule
import org.orkg.dataimport.domain.PaperCSVMissingResearchField
import org.orkg.dataimport.domain.PaperCSVMissingTitle
import org.orkg.dataimport.domain.PaperCSVResourceNotFound
import org.orkg.dataimport.domain.PaperCSVThingNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [DataImportJacksonModule::class, CommonJacksonModule::class, FixedClockConfig::class])
internal class PaperCSVExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun paperCSVMissingTitle() {
        val type = "orkg:problem:paper_csv_missing_paper_title"
        documentedGetRequestTo(PaperCSVMissingTitle(1, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing title for paper in row 1 (line 2).""")
            .andExpect(jsonPath("$.item_number").value("1"))
            .andExpect(jsonPath("$.line_number").value("2"))
            .andDocument {
                responseFields<PaperCSVMissingTitle>(
                    fieldWithPath("item_number").description("The number of the paper within the CSV."),
                    fieldWithPath("line_number").description("The line within the CSV."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun paperCSVMissingResearchField() {
        val type = "orkg:problem:paper_csv_missing_research_field"
        documentedGetRequestTo(PaperCSVMissingResearchField(1, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing research field for paper in row 1 (line 2).""")
            .andExpect(jsonPath("$.item_number").value("1"))
            .andExpect(jsonPath("$.line_number").value("2"))
            .andDocument {
                responseFields<PaperCSVMissingResearchField>(
                    fieldWithPath("item_number").description("The number of the paper within the CSV."),
                    fieldWithPath("line_number").description("The line within the CSV."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun paperCSVResourceNotFound() {
        val type = "orkg:problem:paper_csv_resource_not_found"
        documentedGetRequestTo(PaperCSVResourceNotFound(ThingId("R123"), 1, 2, 4))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Resource "R123" in row 1, column 4 not found (line 2).""")
            .andExpect(jsonPath("$.resource_id").value("R123"))
            .andExpect(jsonPath("$.item_number").value("1"))
            .andExpect(jsonPath("$.line_number").value("2"))
            .andExpect(jsonPath("$.csv_column").value("4"))
            .andDocument {
                responseFields<PaperCSVResourceNotFound>(
                    fieldWithPath("resource_id").description("The id of the resource."),
                    fieldWithPath("item_number").description("The number of the paper within the CSV."),
                    fieldWithPath("line_number").description("The line within the CSV."),
                    fieldWithPath("csv_column").description("The column of the CSV."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun paperCSVThingNotFound() {
        val type = "orkg:problem:paper_csv_thing_not_found"
        documentedGetRequestTo(PaperCSVThingNotFound(ThingId("R123"), 1, 2, 4))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Thing "R123" in row 1, column 4 not found (line 2).""")
            .andExpect(jsonPath("$.thing_id").value("R123"))
            .andExpect(jsonPath("$.item_number").value("1"))
            .andExpect(jsonPath("$.line_number").value("2"))
            .andExpect(jsonPath("$.csv_column").value("4"))
            .andDocument {
                responseFields<PaperCSVThingNotFound>(
                    fieldWithPath("thing_id").description("The id of the thing."),
                    fieldWithPath("item_number").description("The number of the paper within the CSV."),
                    fieldWithPath("line_number").description("The line within the CSV."),
                    fieldWithPath("csv_column").description("The column of the CSV."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
