package orkg.orkg.community.testing.fixtures

import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

fun observatoryResponseFields() = listOf(
    fieldWithPath("id").description("The observatory ID"),
    fieldWithPath("name").description("The observatory name"),
    fieldWithPath("description").description("The observatory description"),
    fieldWithPath("research_field").description("The research field of an observatory"),
    fieldWithPath("research_field.id").description("The research field of an observatory"),
    fieldWithPath("research_field.label").description("The research field of an observatory"),
    fieldWithPath("members").description("The members belonging to the observatory"),
    fieldWithPath("organization_ids").description("The list of organizations that the observatory belongs to"),
    fieldWithPath("display_id").description("The URI of an observatory"),
    fieldWithPath("sdgs").description("The set of ids of sustainable development goals that the observatory belongs to")
)
