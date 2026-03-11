package org.orkg.contenttypes.adapter.input.rest.mapping

import kotlinx.html.HTMLTag
import kotlinx.html.a
import kotlinx.html.article
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jbibtex.BibTeXEntry
import org.jbibtex.BibTeXParser
import org.jbibtex.Key
import org.jbibtex.LaTeXParser
import org.jbibtex.LaTeXPrinter
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.jats.DocumentState
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`article-categories`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`article-id`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`article-meta`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`article-title`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.back
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.body
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.contrib
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`contrib-group`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`contrib-id`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`copyright-holder`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`copyright-year`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.day
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.edition
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`element-citation`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.fpage
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.front
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.issue
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.license
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`license-p`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.license_ref
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.month
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`page-range`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.permissions
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`pub-date`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`pub-id`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.publisher
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`publisher-name`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.ref
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`ref-list`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.sec
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.series
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`series-title`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.source
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`string-name`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`subj-group`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.subject
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`table-wrap`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.title
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.`title-group`
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.volume
import org.orkg.contenttypes.adapter.input.rest.jats.dsl.year
import org.orkg.contenttypes.adapter.input.rest.jats.markdown.JatsHtmlRenderer
import org.orkg.contenttypes.adapter.input.rest.jats.markdown.JatsStrikethroughRenderer
import org.orkg.contenttypes.adapter.input.rest.jats.markdown.JatsTableRenderer
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.SmartReview
import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.contenttypes.domain.ThingReference
import org.orkg.contenttypes.input.SmartReviewUseCases
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.input.StatementUseCases
import java.io.StringReader
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.Optional

private val markdownParser = Parser.builder()
    .extensions(
        listOf(
            TablesExtension.create(),
            AutolinkExtension.create(),
            StrikethroughExtension.builder().requireTwoTildes(true).build(),
        ),
    )
    .build()
private val laTeXParser = LaTeXParser()
private val latexPrinter = LaTeXPrinter()

interface SmartReviewJatsXmlAdapter : ComparisonJatsXmlAdapter {
    val smartReviewUseCases: SmartReviewUseCases
    val statementUseCases: StatementUseCases

    fun Optional<SmartReview>.mapToSmartReviewJatsXml() =
        map { it.toJatsXml() }

    fun SmartReview.toJatsXml(): String =
        document {
            val state = DocumentState()
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
                            attributes["iso-8601-date"] = DateTimeFormatter.ISO_DATE.format(createdAt)
                            day { +(createdAt.dayOfMonth.toString()) }
                            month { +(createdAt.month.value.toString()) }
                            year { +(createdAt.year.toString()) }
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
                    val markdownRenderer = createMarkdownRenderer(state)
                    sections.forEach { section ->
                        sec {
                            title { +section.heading }
                            when (section) {
                                is SmartReviewComparisonSection -> comparisonSection(section, state)
                                is SmartReviewOntologySection -> ontologySection(section, this@toJatsXml, state)
                                is SmartReviewPredicateSection -> thingSection(section.predicate, this@toJatsXml, state)
                                is SmartReviewResourceSection -> thingSection(section.resource, this@toJatsXml, state)
                                is SmartReviewTextSection -> textSection(section, markdownParser, markdownRenderer)
                                is SmartReviewVisualizationSection -> visualizationSection(section)
                            }
                        }
                    }
                }
                if (references.isNotEmpty()) {
                    back {
                        `ref-list` {
                            val bibTeXParser = BibTeXParser()
                            references.forEach { reference ->
                                val database = bibTeXParser.parse(StringReader(reference))
                                val (key, entry) = database.entries.toList().singleOrNull()
                                    ?: return@forEach
                                reference(key, entry)
                            }
                        }
                    }
                }
            }
        }.toXml()

    private fun HTMLTag.comparisonSection(section: SmartReviewComparisonSection, state: DocumentState) {
        section.comparison?.let {
            comparisonTableUseCases.findByComparisonId(it.id)
                .map { comparisonTable(it, state) }
                .orElseGet { ComparisonTable(it.id) }
        }
    }

    private fun HTMLTag.ontologySection(section: SmartReviewOntologySection, smartReview: SmartReview, state: DocumentState) {
        `table-wrap` {
            attributes["id"] = state.nextTableId().toString()
            table {
                thead {
                    tr {
                        th { +"Label" }
                        th { +"Property" }
                        th { +"Value" }
                    }
                }
                tbody {
                    section.entities.forEach { entity ->
                        val contents = smartReview.findContentsById(entity.id!!)
                        var skipLabel = false
                        section.predicates.forEach { predicate ->
                            val values = contents.filter { it.predicate.id == predicate.id }
                                .map { it.`object` }
                            if (values.isEmpty()) {
                                return@forEach
                            }
                            values.forEach { value ->
                                tr {
                                    td {
                                        if (!skipLabel) {
                                            +entity.label
                                            skipLabel = true
                                        }
                                    }
                                    td { +predicate.label }
                                    td { +value.label }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun HTMLTag.thingSection(thing: ThingReference?, smartReview: SmartReview, state: DocumentState) {
        if (thing != null) {
            `table-wrap` {
                attributes["id"] = state.nextTableId().toString()
                table {
                    tbody {
                        val contents = smartReview.findContentsById(thing.id!!)
                        contents.forEach { content ->
                            tr {
                                td { +content.predicate.label }
                                td { +content.`object`.label }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun HTMLTag.textSection(
        section: SmartReviewTextSection,
        markdownParser: Parser,
        markdownRenderer: HtmlRenderer,
    ) {
        unsafe {
            val document = markdownParser.parse(section.text)
            val html = markdownRenderer.render(document)
            raw(html)
        }
    }

    private fun HTMLTag.visualizationSection(section: SmartReviewVisualizationSection) {
        if (section.visualization != null) {
            text("Visualization can be viewed via ")
            consumer.a {
                href = "$frontendUri/resources/${section.visualization!!.id}"
                +"the ORKG website"
            }
            text(".")
        }
    }

    private fun HTMLTag.reference(key: Key, entry: BibTeXEntry) {
        ref {
            attributes["id"] = key.value
            val entries = entry.fields.mapValues { (_, value) ->
                val userString = value.toUserString().replace("\\r", "")
                val objects = laTeXParser.parse(StringReader(userString))
                val printed = latexPrinter.print(objects)
                printed.replace("\\n", " ").replace("\\r", "").trim()
            }
            `element-citation` {
                entries[BibTeXEntry.KEY_TYPE]?.let { type ->
                    attributes["publication-type"] = type
                }
                entries[BibTeXEntry.KEY_AUTHOR]?.let { author ->
                    contrib {
                        attributes["contrib-type"] = "author"
                        `string-name` { +author }
                    }
                }
                entries[BibTeXEntry.KEY_DOI]?.let { doi ->
                    `pub-id`("doi") { +doi }
                }
                entries[BibTeXEntry.KEY_EDITION]?.let { edition ->
                    edition { +edition }
                }
                entries[BibTeXEntry.KEY_EDITOR]?.let { editor ->
                    contrib {
                        attributes["contrib-type"] = "editor"
                        `string-name` { +editor }
                    }
                }
                entries[BibTeXEntry.KEY_JOURNAL]?.let { journal ->
                    source { +journal }
                }
                entries[BibTeXEntry.KEY_MONTH]?.let { month ->
                    month { +month }
                }
                entries[BibTeXEntry.KEY_NUMBER]?.let { number ->
                    issue { +number }
                }
                entries[BibTeXEntry.KEY_PAGES]?.let { pages ->
                    if (!pages.replace(Regex("\\s"), "").contains(Regex("[^0-9]"))) {
                        `page-range` { +pages }
                    } else {
                        fpage { +pages }
                    }
                }
                entries[BibTeXEntry.KEY_PUBLISHER]?.let { publisher ->
                    publisher {
                        `publisher-name` { +publisher }
                    }
                }
                entries[BibTeXEntry.KEY_SERIES]?.let { series ->
                    series {
                        `series-title` { +series }
                    }
                }
                entries[BibTeXEntry.KEY_TITLE]?.let { title ->
                    `article-title` { +title }
                }
                entries[BibTeXEntry.KEY_VOLUME]?.let { volume ->
                    volume { +volume }
                }
                entries[BibTeXEntry.KEY_YEAR]?.let { year ->
                    year { +year }
                }
            }
        }
    }

    private fun SmartReview.findContentsById(id: ThingId): List<GeneralStatement> {
        val contents = if (published) {
            smartReviewUseCases.findPublishedContentById(this@findContentsById.id, id).fold({ emptyList() }, { it })
        } else {
            statementUseCases.findAll(subjectId = id, pageable = PageRequests.ALL).content
        }
        return contents
    }

    private fun createMarkdownRenderer(state: DocumentState): HtmlRenderer =
        HtmlRenderer.builder()
            .sanitizeUrls(true)
            .escapeHtml(true)
            .nodeRendererFactory { JatsHtmlRenderer(state, it) }
            .nodeRendererFactory { JatsTableRenderer(state, it) }
            .nodeRendererFactory { JatsStrikethroughRenderer(it) }
            .build()
}
