package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.AuthorRepresentation
import eu.tib.orkg.prototype.statements.api.AuthorRepresentation.LiteralAuthorRepresentation
import eu.tib.orkg.prototype.statements.api.AuthorRepresentation.ResourceAuthorRepresentation
import eu.tib.orkg.prototype.statements.api.ComparisonAuthorRepresentation
import eu.tib.orkg.prototype.statements.api.PaperAuthorRepresentation
import eu.tib.orkg.prototype.statements.domain.model.Author
import eu.tib.orkg.prototype.statements.domain.model.Author.LiteralAuthor
import eu.tib.orkg.prototype.statements.domain.model.Author.ResourceAuthor
import eu.tib.orkg.prototype.statements.domain.model.ComparisonAuthor
import eu.tib.orkg.prototype.statements.domain.model.PaperAuthor
import eu.tib.orkg.prototype.statements.services.FormattedLabels
import eu.tib.orkg.prototype.statements.services.StatementCounts
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

    fun Author.toAuthorRepresentation(
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
