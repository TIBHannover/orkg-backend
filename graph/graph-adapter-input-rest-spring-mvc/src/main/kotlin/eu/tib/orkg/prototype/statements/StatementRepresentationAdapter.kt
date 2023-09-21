package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.PredicateRepresentation
import eu.tib.orkg.prototype.statements.api.StatementRepresentation
import eu.tib.orkg.prototype.statements.api.ThingRepresentation
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.services.FormattedLabels
import eu.tib.orkg.prototype.statements.services.StatementCounts
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page

interface StatementRepresentationAdapter : ThingRepresentationAdapter {

    fun Optional<GeneralStatement>.mapToStatementRepresentation(): Optional<StatementRepresentation> =
        map { it.toStatementRepresentation() }

    fun Page<GeneralStatement>.mapToStatementRepresentation(): Page<StatementRepresentation> {
        val resources = content.resources()
        val statementCounts = countsFor(resources)
        val formattedLabelCounts = formatLabelFor(resources)
        return map { it.toRepresentation(statementCounts, formattedLabelCounts) }
    }

    fun Iterable<GeneralStatement>.mapToStatementRepresentation(): Iterable<StatementRepresentation> {
        val resources = toList().resources()
        val statementCounts = countsFor(resources)
        val formattedLabelCounts = formatLabelFor(resources)
        return map { it.toRepresentation(statementCounts, formattedLabelCounts) }
    }

    private fun GeneralStatement.toStatementRepresentation(): StatementRepresentation {
        val resources = listOf(this).resources()
        val counts = countsFor(resources)
        val labels = formatLabelFor(resources)
        return toRepresentation(counts, labels)
    }

    private fun GeneralStatement.toRepresentation(
        statementCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): StatementRepresentation =
        object : StatementRepresentation {
            override val id: StatementId = this@toRepresentation.id!!
            override val subject: ThingRepresentation =
                this@toRepresentation.subject.toThingRepresentation(statementCounts, formattedLabels)
            override val predicate: PredicateRepresentation =
                this@toRepresentation.predicate.toPredicateRepresentation()
            override val `object`: ThingRepresentation =
                this@toRepresentation.`object`.toThingRepresentation(statementCounts, formattedLabels)
            override val createdAt: OffsetDateTime = this@toRepresentation.createdAt!!
            override val createdBy: ContributorId = this@toRepresentation.createdBy
            override val index: Int? = this@toRepresentation.index
        }

    private fun List<GeneralStatement>.resources() =
        map(GeneralStatement::subject).filterIsInstance<Resource>() +
            map(GeneralStatement::`object`).filterIsInstance<Resource>()
}