package org.orkg.contenttypes.adapter.input.rest.jats.markdown

import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.internal.TableHtmlNodeRenderer
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlWriter
import org.orkg.contenttypes.adapter.input.rest.jats.DocumentState

class JatsTableRenderer(
    private val state: DocumentState,
    private val context: HtmlNodeRendererContext,
    private val html: HtmlWriter = context.writer,
) : TableHtmlNodeRenderer(context) {
    override fun renderBlock(tableBlock: TableBlock) {
        val tableId = state.nextTableId()
        html.tag("table-wrap", mapOf("id" to tableId.toString()))
        super.renderBlock(tableBlock)
        html.tag("/table-wrap")
    }
}
