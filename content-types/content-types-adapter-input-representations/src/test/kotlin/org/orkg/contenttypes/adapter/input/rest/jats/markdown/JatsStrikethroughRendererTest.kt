package org.orkg.contenttypes.adapter.input.rest.jats.markdown

import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.junit.jupiter.api.Test

internal class JatsStrikethroughRendererTest : AbstractJatsRendererTest() {
    private val parser = Parser.builder()
        .extensions(listOf(StrikethroughExtension.builder().requireTwoTildes(true).build()))
        .build()

    @Test
    fun `Given a markdown text with strikethrough formatting, when rendered as JATS XML, it renders the text correctly`() {
        val markdown = """~~strikethrough~~"""
        val renderer = createRenderer()
        val result = renderer.render(parser.parse(markdown))
        val expected =
            """
            <strike>strikethrough</strike>
            """.trimIndent()

        result shouldBeJatsXml expected
    }

    private fun createRenderer(): HtmlRenderer =
        HtmlRenderer.builder()
            .omitSingleParagraphP(true)
            .nodeRendererFactory { JatsStrikethroughRenderer(it) }
            .build()
}
