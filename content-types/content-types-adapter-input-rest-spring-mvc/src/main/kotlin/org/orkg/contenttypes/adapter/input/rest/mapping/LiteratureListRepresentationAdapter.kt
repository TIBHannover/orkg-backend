package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.contenttypes.adapter.input.rest.ListSectionRepresentation
import org.orkg.contenttypes.adapter.input.rest.LiteratureListRepresentation
import org.orkg.contenttypes.adapter.input.rest.LiteratureListSectionRepresentation
import org.orkg.contenttypes.adapter.input.rest.TextSectionRepresentation
import org.orkg.contenttypes.domain.ListSection
import org.orkg.contenttypes.domain.LiteratureList
import org.orkg.contenttypes.domain.LiteratureListSection
import org.orkg.contenttypes.domain.TextSection
import org.springframework.data.domain.Page

interface LiteratureListRepresentationAdapter : AuthorRepresentationAdapter, VersionRepresentationAdapter, LabeledObjectRepresentationAdapter {

    fun Optional<LiteratureList>.mapToLiteratureListRepresentation(): Optional<LiteratureListRepresentation> =
        map { it.toLiteratureListRepresentation() }

    fun Page<LiteratureList>.mapToLiteratureListRepresentation(): Page<LiteratureListRepresentation> =
        map { it.toLiteratureListRepresentation() }

    fun LiteratureList.toLiteratureListRepresentation(): LiteratureListRepresentation =
        LiteratureListRepresentation(
            id = id,
            title = title,
            researchFields = researchFields,
            authors = authors.mapToAuthorRepresentation(),
            versions = versions.toVersionInfoRepresentation(),
            sustainableDevelopmentGoals = sustainableDevelopmentGoals.mapToLabeledObjectRepresentation(),
            observatories = observatories,
            organizations = organizations,
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            visibility = visibility,
            unlistedBy = unlistedBy,
            published = published,
            sections = sections.map { it.toLiteratureListSectionRepresentation() }
        )

    private fun LiteratureListSection.toLiteratureListSectionRepresentation(): LiteratureListSectionRepresentation =
        when (this) {
            is ListSection -> ListSectionRepresentation(id, entries)
            is TextSection -> TextSectionRepresentation(id, heading, headingSize, text)
        }
}
