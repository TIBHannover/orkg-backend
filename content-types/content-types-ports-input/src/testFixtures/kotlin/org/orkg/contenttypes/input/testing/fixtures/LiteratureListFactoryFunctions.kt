package org.orkg.contenttypes.input.testing.fixtures

import java.net.URI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.ListSection
import org.orkg.contenttypes.domain.LiteratureListSection
import org.orkg.contenttypes.domain.TextSection
import org.orkg.contenttypes.input.ListSectionCommand
import org.orkg.contenttypes.input.LiteratureListSectionDefinition
import org.orkg.contenttypes.input.TextSectionCommand
import org.orkg.contenttypes.input.UpdateLiteratureListUseCase
import org.orkg.graph.domain.ExtractionMethod

fun dummyUpdateLiteratureListCommand() = UpdateLiteratureListUseCase.UpdateCommand(
    literatureListId = ThingId("R123"),
    contributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    title = "Updated dummy LiteratureList Label",
    researchFields = listOf(ThingId("R12")),
    authors = listOf(
        Author(
            id = ThingId("R123"),
            name = "Author with id"
        ),
        Author(
            name = "Author with orcid",
            identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333"))
        ),
        Author(
            id = ThingId("R456"),
            name = "Author with id and orcid",
            identifiers = mapOf("orcid" to listOf("1111-2222-3333-4444"))
        ),
        Author(
            name = "Author with homepage",
            homepage = URI.create("http://example.org/author")
        ),
        Author(
            name = "Author that just has a name"
        )
    ),
    sustainableDevelopmentGoals = setOf(ThingId("SDG_3")),
    observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
    organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")),
    extractionMethod = ExtractionMethod.MANUAL,
    sections = listOf(
        dummyTextSectionDefinition(),
        dummyListSectionDefinition()
    )
)

fun dummyListSectionDefinition(): ListSectionCommand =
    ListSectionCommand(
        entries = listOf(
            ThingId("R2315"),
            ThingId("R3512"),
        )
    )

fun dummyTextSectionDefinition(): TextSectionCommand =
    TextSectionCommand(
        heading = "Updated Heading",
        headingSize = 3,
        text = "updated text section contents"
    )

fun LiteratureListSection.toLiteratureListSectionDefinition(): LiteratureListSectionDefinition =
    when (this) {
        is ListSection -> toListSectionDefinition()
        is TextSection -> toTextSectionDefinition()
    }

fun ListSection.toListSectionDefinition(): ListSectionCommand =
    ListSectionCommand(entries.map { it.id })

fun TextSection.toTextSectionDefinition(): TextSectionCommand =
    TextSectionCommand(heading, headingSize, text)
