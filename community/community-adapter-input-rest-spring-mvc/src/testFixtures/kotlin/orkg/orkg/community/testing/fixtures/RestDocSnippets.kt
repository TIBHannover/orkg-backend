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

fun contributorResponseFields() = listOf(
    fieldWithPath("id").description("The contributor ID."),
    fieldWithPath("display_name").description("The name of the contributor."),
    fieldWithPath("joined_at").description("The time the contributor joined the project (in ISO 8601 format)."),
    fieldWithPath("organization_id").description("The ID of the organization the contributor belongs to. All zeros if the contributor is not part of an organization."),
    fieldWithPath("observatory_id").description("The ID of the observatory the contributor belongs to. All zeros if the contributor has not joined an observatory."),
    fieldWithPath("gravatar_id").description("The ID of the contributor on https://gravatar.com/[Gravatar]. (Useful for generating profile pictures.)"),
    fieldWithPath("avatar_url").description("A URL to an avatar representing the user. Currently links to https://gravatar.com/[Gravatar].")
)
