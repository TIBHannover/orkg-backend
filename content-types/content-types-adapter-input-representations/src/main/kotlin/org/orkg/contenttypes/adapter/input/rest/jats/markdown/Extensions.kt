package org.orkg.contenttypes.adapter.input.rest.jats.markdown

import org.commonmark.node.Node
import org.commonmark.renderer.html.HtmlNodeRendererContext

internal fun HtmlNodeRendererContext.getAttrs(
    node: Node,
    tagName: String,
    defaultAttributes: Map<String, String> = emptyMap(),
): Map<String, String> =
    extendAttributes(node, tagName, defaultAttributes)

internal fun HtmlNodeRendererContext.visitChildren(parent: Node) {
    var node = parent.getFirstChild()
    while (node != null) {
        val next = node.getNext()
        render(node)
        node = next
    }
}
