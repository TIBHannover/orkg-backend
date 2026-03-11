package org.orkg.contenttypes.adapter.input.rest.jats.markdown

import org.commonmark.ext.gfm.strikethrough.internal.StrikethroughHtmlNodeRenderer
import org.commonmark.node.Node
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlWriter

class JatsStrikethroughRenderer(
    private val context: HtmlNodeRendererContext,
    private val html: HtmlWriter = context.writer,
) : StrikethroughHtmlNodeRenderer(context) {
    override fun render(node: Node) {
        html.tag("strike", context.getAttrs(node, "strike"))
        context.visitChildren(node)
        html.tag("/strike")
    }
}
