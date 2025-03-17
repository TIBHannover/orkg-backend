package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.contenttypes.adapter.input.rest.ComparisonAuthorInfoRepresentation
import org.orkg.contenttypes.adapter.input.rest.ComparisonAuthorRepresentation
import org.orkg.contenttypes.domain.ComparisonAuthor
import org.orkg.contenttypes.domain.ComparisonAuthorInfo
import org.orkg.contenttypes.domain.PaperAuthor
import org.orkg.contenttypes.domain.SimpleAuthor
import org.orkg.contenttypes.domain.SimpleAuthor.LiteralAuthor
import org.orkg.contenttypes.domain.SimpleAuthor.ResourceAuthor
import org.orkg.graph.adapter.input.rest.PaperAuthorRepresentation
import org.orkg.graph.adapter.input.rest.SimpleAuthorRepresentation
import org.orkg.graph.adapter.input.rest.SimpleAuthorRepresentation.LiteralAuthorRepresentation
import org.orkg.graph.adapter.input.rest.SimpleAuthorRepresentation.ResourceAuthorRepresentation
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.StatementCounts
import org.springframework.data.domain.Page

interface LegacyAuthorRepresentationAdapter : ResourceRepresentationAdapter {
    fun Page<ComparisonAuthor>.mapToComparisonAuthorRepresentation(
        capabilities: MediaTypeCapabilities,
    ): Page<ComparisonAuthorRepresentation> {
        val resources = content.map { it.author }.filterIsInstance<ResourceAuthor>().map { it.value }
        val usageCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources, capabilities)
        return map { it.toComparisonAuthorRepresentation(usageCounts, formattedLabels) }
    }

    fun Page<PaperAuthor>.mapToPaperAuthorRepresentation(
        capabilities: MediaTypeCapabilities,
    ): Page<PaperAuthorRepresentation> {
        val resources = content.map { it.author }.filterIsInstance<ResourceAuthor>().map { it.value }
        val usageCounts = countIncomingStatements(resources)
        val formattedLabels = formatLabelFor(resources, capabilities)
        return map { it.toPaperAuthorRepresentation(usageCounts, formattedLabels) }
    }

    fun ComparisonAuthor.toComparisonAuthorRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels,
    ): ComparisonAuthorRepresentation =
        ComparisonAuthorRepresentation(
            author.toSimpleAuthorRepresentation(usageCounts, formattedLabels),
            info.map { it.toComparisonAuthorInfoRepresentation() }
        )

    fun PaperAuthor.toPaperAuthorRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels,
    ): PaperAuthorRepresentation =
        PaperAuthorRepresentation(author.toSimpleAuthorRepresentation(usageCounts, formattedLabels), papers)

    fun SimpleAuthor.toSimpleAuthorRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels,
    ): SimpleAuthorRepresentation =
        when (this) {
            is ResourceAuthor -> ResourceAuthorRepresentation(
                value.toResourceRepresentation(usageCounts, formattedLabels)
            )
            is LiteralAuthor -> LiteralAuthorRepresentation(value)
        }

    fun ComparisonAuthorInfo.toComparisonAuthorInfoRepresentation(): ComparisonAuthorInfoRepresentation =
        ComparisonAuthorInfoRepresentation(paperId, authorIndex, paperYear)
}
