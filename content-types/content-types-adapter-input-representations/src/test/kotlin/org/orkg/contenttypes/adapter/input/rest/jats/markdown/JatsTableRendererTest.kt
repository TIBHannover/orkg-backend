package org.orkg.contenttypes.adapter.input.rest.jats.markdown

import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.adapter.input.rest.jats.DocumentState

internal class JatsTableRendererTest : AbstractJatsRendererTest() {
    private val parser = Parser.builder()
        .extensions(listOf(TablesExtension.create()))
        .build()

    @Test
    fun `Given a markdown table, when rendered as JATS XML, it renders the table correctly`() {
        val markdown =
            """
            | Column 1 | Column 2 |
            | -------- | -------- |
            | Value 1  | Value 2  |
            """.trimIndent()
        val renderer = createRenderer()
        val result = renderer.render(parser.parse(markdown))
        val expected =
            """
            <table-wrap id="table-1">
              <table>
                <thead>
                  <tr>
                    <th>Column 1</th>
                    <th>Column 2</th>
                  </tr>
                </thead>
                <tbody>
                    <tr>
                      <td>Value 1</td>
                      <td>Value 2</td>
                    </tr>
                </tbody>
              </table>
            </table-wrap>
            """.trimIndent()

        result shouldBeJatsXml expected
    }

    @Test
    fun `Given several markdown tables, when rendered as JATS XML, it assigns a unique id to every table`() {
        val markdown =
            """
            | Column 1 | Column 2 |
            | -------- | -------- |
            | Value 1  | Value 2  |
            
            | Column 1 | Column 2 |
            | -------- | -------- |
            | Value 1  | Value 2  |
            """.trimIndent()
        val renderer = createRenderer()
        val result = renderer.render(parser.parse(markdown))
        val expected =
            """
            <table-wrap id="table-1">
              <table>
                <thead>
                  <tr>
                    <th>Column 1</th>
                    <th>Column 2</th>
                  </tr>
                </thead>
                <tbody>
                    <tr>
                      <td>Value 1</td>
                      <td>Value 2</td>
                    </tr>
                </tbody>
              </table>
            </table-wrap>
            <table-wrap id="table-2">
              <table>
                <thead>
                  <tr>
                    <th>Column 1</th>
                    <th>Column 2</th>
                  </tr>
                </thead>
                <tbody>
                    <tr>
                      <td>Value 1</td>
                      <td>Value 2</td>
                    </tr>
                </tbody>
              </table>
            </table-wrap>
            """.trimIndent()

        result shouldBeJatsXml expected
    }

    private fun createRenderer(state: DocumentState = DocumentState()): HtmlRenderer =
        HtmlRenderer.builder()
            .omitSingleParagraphP(true)
            .nodeRendererFactory { JatsTableRenderer(state, it) }
            .build()
}
