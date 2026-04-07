package org.orkg.contenttypes.adapter.input.rest.mapping

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.TRANSPOSED_CAPABILITY
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.ComparisonTableRow
import org.orkg.contenttypes.domain.LabeledComparisonPath
import org.orkg.graph.domain.Thing
import java.io.StringWriter
import java.util.Optional

interface ComparisonTableCsvAdapter {
    fun Optional<ComparisonTable>.mapToComparisonTableCsv(capabilities: MediaTypeCapabilities): Optional<String> =
        map { it.toCsv(capabilities) }

    fun ComparisonTable.toCsv(capabilities: MediaTypeCapabilities): String {
        if (capabilities.getOrDefault(TRANSPOSED_CAPABILITY)) {
            return toTransposedCsv()
        } else {
            return toCsv()
        }
    }

    fun ComparisonTable.toCsv(): String {
        val headers = buildHeader(titles, subtitles)
        val format = CSVFormat.DEFAULT.builder()
            .setHeader("Properties", *headers.toTypedArray())
            .get()
        val predicateLabelLookup = selectedPaths.toPredicateLabelLookup()
        val writer = StringWriter()
        CSVPrinter(writer, format).use { printer ->
            printer.printTable(values, predicateLabelLookup)
        }
        return writer.toString()
    }

    private fun buildHeader(titles: List<Thing>, subtitles: List<Thing?>) =
        titles.zip(subtitles).map { (title, subtitle) -> formatTitle(title, subtitle) }

    private fun formatTitle(title: Thing, subtitle: Thing?): String =
        if (!subtitle?.label.isNullOrEmpty()) {
            "${title.label} - ${subtitle.label}"
        } else {
            title.label
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

    fun ComparisonTable.toTransposedCsv(): String {
        val predicateLabelLookup = selectedPaths.toPredicateLabelLookup()
        val headers = buildTransposedHeader(values, predicateLabelLookup)
        val format = CSVFormat.DEFAULT.builder()
            .setHeader("title", *headers.toTypedArray())
            .get()
        val writer = StringWriter()
        CSVPrinter(writer, format).use { printer ->
            printer.printTransposedTable(titles, subtitles, values)
        }
        return writer.toString()
    }

    private fun buildTransposedHeader(
        values: Map<ThingId, List<ComparisonTableRow>>,
        predicateLabelLookup: Map<ThingId, String>,
        path: List<String> = emptyList(),
    ): List<String> {
        val result = mutableListOf<String>()
        values.entries.forEach { (predicateId, rows) ->
            val predicateLabel = predicateLabelLookup[predicateId]!!
            val newPath = path + predicateLabel
            val columnLabel = newPath.joinToString(separator = " - ")
            rows.forEach { row ->
                result += columnLabel
                result += buildTransposedHeader(row.children, predicateLabelLookup, newPath)
            }
        }
        return result
    }

    private fun buildTransposedRow(
        valueIndex: Int,
        values: Map<ThingId, List<ComparisonTableRow>>,
    ): List<String> {
        val result = mutableListOf<String>()
        values.values.forEach { rows ->
            rows.forEach { row ->
                result += row.values[valueIndex]?.label.orEmpty()
                result += buildTransposedRow(valueIndex, row.children)
            }
        }
        return result
    }

    private fun CSVPrinter.printTransposedTable(
        titles: List<Thing>,
        subtitles: List<Thing?>,
        data: Map<ThingId, List<ComparisonTableRow>>,
    ) {
        val headers = buildHeader(titles, subtitles)
        headers.forEachIndexed { valueIndex, header ->
            printRecord(header, *buildTransposedRow(valueIndex, data).toTypedArray())
        }
    }

    private fun List<LabeledComparisonPath>.toPredicateLabelLookup(): Map<ThingId, String> =
        associate { it.id to it.label } + flatMap { it.children.toPredicateLabelLookup().toList() }.toMap()
}
