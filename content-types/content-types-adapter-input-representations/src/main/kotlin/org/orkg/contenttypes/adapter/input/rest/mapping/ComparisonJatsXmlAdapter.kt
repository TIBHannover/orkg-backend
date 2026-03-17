package org.orkg.contenttypes.adapter.input.rest.mapping

import kotlinx.html.TBODY
import kotlinx.html.TD
import kotlinx.html.THEAD
import kotlinx.html.Tag
import kotlinx.html.article
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.jats.DocumentState
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`article-categories`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`article-id`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`article-meta`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`article-title`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.body
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.contrib
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`contrib-group`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`contrib-id`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`copyright-holder`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`copyright-year`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.day
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.description
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`ext-link`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.front
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.italic
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.license
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`license-p`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.license_ref
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.month
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.permissions
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`pub-date`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`string-name`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`subj-group`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.subject
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`table-wrap`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`title-group`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.year
import org.orkg.contenttypes.domain.Comparison
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.ComparisonTableRow
import org.orkg.contenttypes.domain.LabeledComparisonPath
import org.orkg.contenttypes.input.ComparisonTableUseCases
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import java.time.Clock
import java.time.Year
import java.util.Optional
import java.util.regex.Pattern

private val LINK_REGEX = Pattern.compile("(https?:\\/\\/[^\\s]+)")

interface ComparisonJatsXmlAdapter {
    val comparisonTableUseCases: ComparisonTableUseCases
    val frontendUri: String
    val clock: Clock

    fun Optional<Comparison>.mapToJatsXml(): Optional<String> =
        map {
            it.toJatsXml(
                comparisonTableUseCases.findByComparisonId(it.id)
                    .orElseThrow { throw IllegalStateException("Error while fetching comparison table, although the comparison exists! This is a bug!") },
            )
        }

    fun Comparison.toJatsXml(table: ComparisonTable): String =
        document {
            append.article {
                attributes["xmlns:xlink"] = "https://www.w3.org/1999/xlink"
                attributes["xmlns:ali"] = "http://www.niso.org/schemas/ali/1.0"
                front {
                    `article-meta` {
                        `article-categories` {
                            researchFields.forEach { researchField ->
                                `subj-group` {
                                    attributes["xml:lang"] = "en"
                                    subject { +researchField.label }
                                }
                            }
                        }
                        `article-id`("orkg") { +id.value }
                        identifiers.forEach { (type, ids) ->
                            ids.forEach { id ->
                                `article-id`(type) { +id }
                            }
                        }
                        `title-group` {
                            `article-title` { +title }
                        }
                        description?.let { description ->
                            description {
                                p { +description }
                            }
                        }
                        `contrib-group` {
                            attributes["content-type"] = "author"
                            authors.forEach { author ->
                                contrib {
                                    attributes["contrib-type"] = "person"
                                    `string-name` { +author.name }
                                    author.id?.let { authorId ->
                                        `contrib-id`("orkg") { +authorId.value }
                                    }
                                    author.identifiers?.forEach { (type, ids) ->
                                        ids.forEach { id ->
                                            `contrib-id`(type) { +id }
                                        }
                                    }
                                }
                            }
                        }
                        `pub-date` {
                            attributes["date-type"] = "pub"
                            day { +"--" }
                            month { +(publicationInfo.publishedMonth?.toString() ?: "--") }
                            year { +(publicationInfo.publishedYear?.toString() ?: "--") }
                        }
                        permissions {
                            attributes["id"] = "permission"
                            `copyright-year` { +Year.now(clock).value.toString() }
                            `copyright-holder` { +"Open Research Knowledge Graph" }
                            license {
                                license_ref { +"http://creativecommons.org/licenses/by-sa/4.0/" }
                                `license-p` { +"This work is licensed under a Creative Commons Attribution-ShareAlike 4.0 International License (CC BY-SA 4.0)" }
                            }
                        }
                    }
                }
                body {
                    attributes["id"] = "body"
                    comparisonTable(table)
                }
            }
        }.toXml()

    fun Tag.comparisonTable(table: ComparisonTable, state: DocumentState = DocumentState()) {
        `table-wrap` {
            attributes["id"] = state.nextTableId().toString()
            table {
                thead {
                    tableHead(table.titles, table.subtitles)
                }
                tbody {
                    tableBody(table.values, table.selectedPaths.toPredicateLabelLookup())
                }
            }
        }
    }

    private fun THEAD.tableHead(
        titles: List<Thing>,
        subtitles: List<Thing?>,
    ) {
        tr {
            style = "text-align: left;"
            th { +"Properties" }
            titles.zip(subtitles).forEach { (title, subtitle) ->
                th {
                    `ext-link` {
                        attributes["ext-link-type"] = "uri"
                        attributes["xlink:href"] = toUrl(title, subtitle)
                        +title.label
                        if (subtitle != null) {
                            italic { +subtitle.label }
                        }
                    }
                }
            }
        }
    }

    private fun TBODY.tableBody(
        data: Map<ThingId, List<ComparisonTableRow>>,
        predicateLabelLookup: Map<ThingId, String>,
        depth: Int = 0,
    ) {
        data.forEach { predicateId, rows ->
            val rowLabel = predicateLabelLookup[predicateId]!!
            rows.forEach { row ->
                tr {
                    th {
                        style = "padding-left: ${12 * depth}px; text-align: left;"
                        +rowLabel
                    }
                    row.values.forEach { value ->
                        td { tableValue(value) }
                    }
                }
                tableBody(row.children, predicateLabelLookup, depth + 1)
            }
        }
    }

    private fun toUrl(
        title: Thing,
        subtitle: Thing?,
    ): String = when {
        subtitle != null && title is Resource && Classes.paper in title.classes -> {
            "$frontendUri/papers/${title.id}/${subtitle.id}"
        }

        else -> {
            "$frontendUri/resources/${title.id}"
        }
    }

    fun TD.tableValue(value: Thing?) {
        val label = value?.label.orEmpty()
        if (label.isEmpty() || value !is Literal) {
            +label
        } else {
            val ranges = LINK_REGEX.matcher(label).results()
                .map { matchResult -> LinkRange(matchResult.start(), matchResult.end()) }
                .toList()
                .withTextRanges(label.length)
            ranges.forEach { range ->
                val substring = range.substring(label)
                when (range) {
                    is LinkRange -> {
                        `ext-link` {
                            attributes["ext-link-type"] = "uri"
                            attributes["xlink:href"] = substring
                            +substring
                        }
                    }

                    is TextRange -> {
                        +substring
                    }
                }
            }
        }
    }

    private fun List<Range>.withTextRanges(length: Int): List<Range> {
        val result = mutableListOf<Range>()
        var index = 0
        forEach { range ->
            if (index < range.start) {
                result += TextRange(
                    start = index,
                    end = range.start,
                )
            }
            index = range.end
            result += range
        }
        if (index < length) {
            result += TextRange(
                start = index,
                end = length,
            )
        }
        return result
    }

    private fun List<LabeledComparisonPath>.toPredicateLabelLookup(): Map<ThingId, String> =
        associate { it.id to it.label } + flatMap { it.children.toPredicateLabelLookup().toList() }.toMap()

    companion object {
        internal interface Range {
            val start: Int
            val end: Int

            fun substring(input: String): String = input.substring(start, end)
        }

        internal data class TextRange(
            override val start: Int,
            override val end: Int,
        ) : Range

        internal data class LinkRange(
            override val start: Int,
            override val end: Int,
        ) : Range
    }
}
