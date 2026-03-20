package org.orkg.contenttypes.adapter.input.rest.mapping

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.ComparisonTableRow
import org.orkg.contenttypes.domain.LabeledComparisonPath
import java.io.StringWriter
import java.util.Optional

interface ComparisonTableCsvAdapter {
    fun Optional<ComparisonTable>.mapToComparisonTableCsv(): Optional<String> =
        map { it.toCsv() }

    fun ComparisonTable.toCsv(): String {
        val headers = titles.zip(subtitles)
            .map { (title, subtitle) ->
                if (!subtitle?.label.isNullOrEmpty()) {
                    "${title.label} - ${subtitle.label}"
                } else {
                    title.label
                }
            }
            .toTypedArray()
        val format = CSVFormat.DEFAULT.builder()
            .setHeader("Properties", *headers)
            .get()
        val predicateLabelLookup = selectedPaths.toPredicateLabelLookup()
        val writer = StringWriter()
        CSVPrinter(writer, format).use { printer ->
            printer.printTable(values, predicateLabelLookup)
        }
        return writer.toString()
    }

    private fun CSVPrinter.printTable(
        data: Map<ThingId, List<ComparisonTableRow>>,
        predicateLabelLookup: Map<ThingId, String>,
        path: List<String> = emptyList(),
    ) {
        data.forEach { (predicateId, rows) ->
            val predicateLabel = predicateLabelLookup[predicateId]!!
            val newPath = path + predicateLabel
            val rowLabel = newPath.joinToString(separator = " - ")
            rows.forEach { row ->
                printRecord(rowLabel, *row.values.map { it?.label.orEmpty() }.toTypedArray())
                printTable(row.children, predicateLabelLookup, newPath)
            }
        }
    }

    private fun List<LabeledComparisonPath>.toPredicateLabelLookup(): Map<ThingId, String> =
        associate { it.id to it.label } + flatMap { it.children.toPredicateLabelLookup().toList() }.toMap()
}
