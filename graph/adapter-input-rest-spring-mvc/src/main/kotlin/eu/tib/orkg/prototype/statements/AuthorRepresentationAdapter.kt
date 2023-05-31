package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.domain.model.Author
import eu.tib.orkg.prototype.statements.domain.model.Author.LiteralAuthor
import eu.tib.orkg.prototype.statements.domain.model.Author.ResourceAuthor
import eu.tib.orkg.prototype.statements.domain.model.ComparisonAuthor
import eu.tib.orkg.prototype.statements.domain.model.ComparisonAuthorInfo
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
        object : ComparisonAuthorRepresentation {
            override val author: AuthorRepresentation =
                this@toComparisonAuthorRepresentation.author.toAuthorRepresentation(usageCounts, formattedLabels)
            override val info: Iterable<ComparisonAuthorInfo> = this@toComparisonAuthorRepresentation.info
        }

    fun PaperAuthor.toPaperAuthorRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): PaperAuthorRepresentation =
        object : PaperAuthorRepresentation {
            override val author: AuthorRepresentation =
                this@toPaperAuthorRepresentation.author.toAuthorRepresentation(usageCounts, formattedLabels)
            override val papers: Int = this@toPaperAuthorRepresentation.papers
        }

    fun Author.toAuthorRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): AuthorRepresentation =
        when (this) {
            is ResourceAuthor -> object : AuthorRepresentation.ResourceAuthorRepresentation {
                override val value: ResourceRepresentation =
                    this@toAuthorRepresentation.value.toResourceRepresentation(usageCounts, formattedLabels)
            }
            is LiteralAuthor -> object : AuthorRepresentation.LiteralAuthorRepresentation {
                override val value: String = this@toAuthorRepresentation.value
            }
        }
}
