package org.orkg.contenttypes.adapter.input.rest.jats.markdown

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.adapter.input.rest.jats.DocumentState

internal class JatsHtmlRendererTest : AbstractJatsRendererTest() {
    private val parser = Parser.builder().build()

    @Test
    fun `Given a markdown image without title, when rendered as JATS XML, it renders the image correctly`() {
        val markdown = """![](http://example.org/image.png)"""
        val renderer = createRenderer()
        val result = renderer.render(parser.parse(markdown))
        val expected =
            """
            <fig id="fig-1">
              <label>Figure 1</label>
              <caption>
                <title>Figure 1</title>
              </caption>
              <graphic id="graphic-1" xlink:href="http://example.org/image.png"></graphic>
            </fig>
            """.trimIndent()

        result shouldBeJatsXml expected
    }

    @Test
    fun `Given a markdown image with title, when rendered as JATS XML, it renders the image correctly`() {
        val markdown = """![Example image](http://example.org/image.png)"""
        val renderer = createRenderer()
        val result = renderer.render(parser.parse(markdown))
        val expected =
            """
            <fig id="fig-1">
              <label>Figure 1</label>
              <caption>
                <title>Example image</title>
              </caption>
              <graphic id="graphic-1" xlink:href="http://example.org/image.png"></graphic>
            </fig>
            """.trimIndent()

        result shouldBeJatsXml expected
    }

    @Test
    fun `Given several markdown images, when rendered as JATS XML, it assigns a unique id to every figure`() {
        val markdown = """![](http://example.org/image.png) ![](http://example.org/image.png)"""
        val renderer = createRenderer()
        val result = renderer.render(parser.parse(markdown))
        val expected =
            """
            <fig id="fig-1">
              <label>Figure 1</label>
              <caption>
                <title>Figure 1</title>
              </caption>
              <graphic id="graphic-1" xlink:href="http://example.org/image.png"></graphic>
            </fig>
            <fig id="fig-2">
              <label>Figure 2</label>
              <caption>
                <title>Figure 2</title>
              </caption>
              <graphic id="graphic-2" xlink:href="http://example.org/image.png"></graphic>
            </fig>
            """.trimIndent()

        result shouldBeJatsXml expected
    }

    @Test
    fun `Given a markdown link without title, when rendered as JATS XML, it renders the link correctly`() {
        val markdown = """[](http://example.org)"""
        val renderer = createRenderer()
        val result = renderer.render(parser.parse(markdown))
        val expected =
            """
            <ext-link ext-link-type="uri" xlink:href="http://example.org"></ext-link>
            """.trimIndent()

        result shouldBeJatsXml expected
    }

    @Test
    fun `Given a markdown link with title, when rendered as JATS XML, it renders the link correctly`() {
        val markdown = """[Example link](http://example.org)"""
        val renderer = createRenderer()
        val result = renderer.render(parser.parse(markdown))
        val expected =
            """
            <ext-link ext-link-type="uri" xlink:href="http://example.org">Example link</ext-link>
            """.trimIndent()

        result shouldBeJatsXml expected
    }

    @Test
    fun `Given a markdown text with emphasis formatting, when rendered as JATS XML, it renders the text correctly`() {
        val markdown = """*emphasis*"""
        val renderer = createRenderer()
        val result = renderer.render(parser.parse(markdown))
        val expected =
            """
            <italic>emphasis</italic>
            """.trimIndent()

        result shouldBeJatsXml expected
    }

    @Test
    fun `Given a markdown text with strong emphasis formatting, when rendered as JATS XML, it renders the text correctly`() {
        val markdown = """**strong emphasis**"""
        val renderer = createRenderer()
        val result = renderer.render(parser.parse(markdown))
        val expected =
            """
            <bold>strong emphasis</bold>
            """.trimIndent()

        result shouldBeJatsXml expected
    }

    @Test
    fun `Given a markdown list (ordered), when rendered as JATS XML, it renders the text correctly`() {
        val markdown =
            """
            1. First entry
            2. Second entry
            """.trimIndent()
        val renderer = createRenderer()
        val result = renderer.render(parser.parse(markdown))
        val expected =
            """
            <list list-type="order">
              <list-item>First entry</list-item>
              <list-item>Second entry</list-item>
            </list>
            """.trimIndent()

        result shouldBeJatsXml expected
    }

    @Test
    fun `Given a markdown list (unordered), when rendered as JATS XML, it renders the text correctly`() {
        val markdown =
            """
            * First entry
            * Second entry
            """.trimIndent()
        val renderer = createRenderer()
        val result = renderer.render(parser.parse(markdown))
        val expected =
            """
            <list list-type="bullet">
              <list-item>First entry</list-item>
              <list-item>Second entry</list-item>
            </list>
            """.trimIndent()

        result shouldBeJatsXml expected
    }

    private fun createRenderer(state: DocumentState = DocumentState()): HtmlRenderer =
        HtmlRenderer.builder()
            .omitSingleParagraphP(true)
            .nodeRendererFactory { JatsHtmlRenderer(state, it) }
            .build()
}
