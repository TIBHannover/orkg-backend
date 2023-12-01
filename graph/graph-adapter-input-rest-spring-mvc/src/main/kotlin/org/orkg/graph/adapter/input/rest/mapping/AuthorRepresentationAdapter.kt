package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.contenttypes.domain.ComparisonAuthor
import org.orkg.contenttypes.domain.PaperAuthor
import org.orkg.contenttypes.domain.SimpleAuthor
import org.orkg.contenttypes.domain.SimpleAuthor.LiteralAuthor
import org.orkg.contenttypes.domain.SimpleAuthor.ResourceAuthor
import org.orkg.graph.domain.FormattedLabels
import org.orkg.graph.domain.StatementCounts
import org.orkg.graph.input.AuthorRepresentation
import org.orkg.graph.input.AuthorRepresentation.LiteralAuthorRepresentation
import org.orkg.graph.input.AuthorRepresentation.ResourceAuthorRepresentation
import org.orkg.graph.input.ComparisonAuthorRepresentation
import org.orkg.graph.input.PaperAuthorRepresentation
import org.springframework.data.domain.Page

interface AuthorRepresentationAdapter : ResourceRepresentationAdapter {
    
    fun Page<ComparisonAuthor>.mapToComparisonAuthorRepresentation(): Page<ComparisonAuthorRepresentation> {
        val resources = content.map { it.author }.filterIsInstance<ResourceAuthor>().map { it.value }
        val usageCounts = countsFor(resources)
        val formattedLabels = formatLabelFor(resources)
        return map { it.toComparisonAuthorRepresentation(usageCounts, formattedLabels) }
    }
    
    fun Page<PaperAuthor>.mapToPaperAuthorRepresentation(): Page<PaperAuthorRepresentation> {
        val resources = content.map { it.author }.filterIsInstance<ResourceAuthor>().map { it.value }
        val usageCounts = countsFor(resources)
        val formattedLabels = formatLabelFor(resources)
        return map { it.toPaperAuthorRepresentation(usageCounts, formattedLabels) }
    }

    fun ComparisonAuthor.toComparisonAuthorRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): ComparisonAuthorRepresentation =
        ComparisonAuthorRepresentation(author.toAuthorRepresentation(usageCounts, formattedLabels), info)

    fun PaperAuthor.toPaperAuthorRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): PaperAuthorRepresentation =
        PaperAuthorRepresentation(author.toAuthorRepresentation(usageCounts, formattedLabels), papers)

    fun SimpleAuthor.toAuthorRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): AuthorRepresentation =
        when (this) {
            is ResourceAuthor -> ResourceAuthorRepresentation(
                value.toResourceRepresentation(usageCounts, formattedLabels)
            )
            is LiteralAuthor -> LiteralAuthorRepresentation(value)
        }
}