package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.FieldWithFreqRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.RetrieveResearchProblemUseCase.FieldWithFreq
import eu.tib.orkg.prototype.statements.services.FormattedLabels
import eu.tib.orkg.prototype.statements.services.StatementCounts

interface FieldPerProblemRepresentationAdapter : ResourceRepresentationAdapter {

    fun List<FieldWithFreq>.mapToFieldWithFreqRepresentation(): List<FieldWithFreqRepresentation> {
        val resources = map { it.field }
        val usageCounts = countsFor(resources)
        val formattedLabels = formatLabelFor(resources)
        return map { it.toFieldWithFreqRepresentation(usageCounts, formattedLabels) }
    }

    fun FieldWithFreq.toFieldWithFreqRepresentation(
        usageCounts: StatementCounts,
        formattedLabels: FormattedLabels
    ): FieldWithFreqRepresentation =
        object : FieldWithFreqRepresentation {
            override val field: ResourceRepresentation =
                this@toFieldWithFreqRepresentation.field.toResourceRepresentation(usageCounts, formattedLabels)
            override val freq: Long = this@toFieldWithFreqRepresentation.freq
        }
}
