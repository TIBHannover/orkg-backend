package orkg.orkg.community.testing.fixtures

import org.orkg.common.thingIdConstraint
import org.orkg.community.domain.ContributorIdentifier
import org.orkg.community.testing.asciidoc.allowedContributorIdentifierValues
import org.orkg.community.testing.asciidoc.allowedOrganizationTypeValues
import org.orkg.testing.spring.restdocs.arrayItemsType
import org.orkg.testing.spring.restdocs.constraints
import org.orkg.testing.spring.restdocs.enumValues
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

internal const val OBSERVATORY_FILTER_PATH_DESCRIPTION =
    "Describes the path from the contribution node of a paper to the node that should be matched, where every entry stands for the predicate id of a statement."
internal const val OBSERVATORY_FILTER_RANGE_DESCRIPTION =
    "The class id that represents the range of the value that should be matched. " +
        "Subclasses will also be considered when matching."
internal const val OBSERVATORY_FILTER_EXACT_MATCH_DESCRIPTION =
    "Whether to exactly match the given path. If `true`, the given path needs to match exactly, starting from the contribution resource. " +
        "If `false`, the given path needs to match exactly, starting at any node in the subgraph of the contribution or the contribution node itself. " +
        "The total path length is limited to 10, including the length of the specified path, starting from the contribution node."

fun observatoryResponseFields() = listOf(
    fieldWithPath("id").description("The observatory ID"),
    fieldWithPath("name").description("The observatory name"),
    fieldWithPath("description").description("The observatory description. (optional)").optional(),
    fieldWithPath("research_field").description("The research field of an observatory"),
    fieldWithPath("research_field.id").description("The research field of an observatory"),
    fieldWithPath("research_field.label").description("The research field of an observatory"),
    fieldWithPath("members").description("The members belonging to the observatory"),
    fieldWithPath("organization_ids").description("The list of organizations that the observatory belongs to"),
    fieldWithPath("display_id").description("The URI of an observatory"),
    fieldWithPath("sdgs").description("The set of ids of sustainable development goals the observatory belongs to. (optional)").arrayItemsType("String").constraints(thingIdConstraint).optional(),
)

fun organizationResponseFields() = listOf(
    fieldWithPath("id").description("The organization ID"),
    fieldWithPath("name").description("The organization name"),
    fieldWithPath("created_by").description("The ID of the user that created the organization."),
    fieldWithPath("homepage").description("The URL of the organization's homepage."),
    fieldWithPath("observatory_ids").description("The list of observatories that belong to this organization"),
    fieldWithPath("display_id").description("The URL of an organization"),
    fieldWithPath("type").description("The type of the organization. Either of $allowedOrganizationTypeValues."),
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

fun contributorIdentifierResponseFields() = listOf(
    fieldWithPath("type").description("The type of the identifier. Either of $allowedContributorIdentifierValues.").type("enum").enumValues(ContributorIdentifier.Type.entries.map { it.id }),
    fieldWithPath("value").description("The value of the identifier."),
    timestampFieldWithPath("created_at", "the identifier was added"),
)

fun observatoryFilterResponseFields() = listOf(
    // The order here determines the order in the generated table. More relevant items should be up.
    fieldWithPath("id").description("The identifier of the filter."),
    fieldWithPath("observatory_id").description("The id of the observatory that the filter belongs to."),
    fieldWithPath("label").description("The label of the filter."),
    fieldWithPath("path[]").description(OBSERVATORY_FILTER_PATH_DESCRIPTION),
    fieldWithPath("range").description(OBSERVATORY_FILTER_RANGE_DESCRIPTION),
    fieldWithPath("exact").description(OBSERVATORY_FILTER_EXACT_MATCH_DESCRIPTION),
    timestampFieldWithPath("created_at", "the filter was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this filter."),
    fieldWithPath("featured").description("Whether the filter is featured or not."),
)
