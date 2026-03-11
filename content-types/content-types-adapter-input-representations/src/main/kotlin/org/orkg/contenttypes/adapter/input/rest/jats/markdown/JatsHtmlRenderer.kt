package org.orkg.contenttypes.adapter.input.rest.jats.markdown

import org.commonmark.node.AbstractVisitor
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Image
import org.commonmark.node.Link
import org.commonmark.node.ListBlock
import org.commonmark.node.ListItem
import org.commonmark.node.OrderedList
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.renderer.html.CoreHtmlNodeRenderer
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlWriter
import org.orkg.contenttypes.adapter.input.rest.jats.DocumentState

class JatsHtmlRenderer(
    private val state: DocumentState,
    context: HtmlNodeRendererContext,
    private val html: HtmlWriter = context.writer,
) : CoreHtmlNodeRenderer(context) {
    override fun visit(image: Image) {
        val figureId = state.nextFigureId()
        val titleVisitor = TitleVisitor()
        image.accept(titleVisitor)
        val title = titleVisitor.title.takeIf { it.isNotBlank() } ?: "Figure ${figureId.value}"
        html.tag("fig", mapOf("id" to figureId.toString()))
        html.line()
        html.tag("label")
        html.text("Figure ${figureId.value}")
        html.tag("/label")
        html.line()
        html.tag("caption")
        html.line()
        html.tag("title")
        html.text(title)
        html.tag("/title")
        html.tag("/caption")
        html.line()
        html.tag("graphic", mapOf("xlink:href" to image.destination, "id" to "graphic-${figureId.value}"))
        html.tag("/graphic")
        html.line()
        html.tag("/fig")
        html.line()
    }

    override fun visit(link: Link) {
        val attrs = mutableMapOf("ext-link-type" to "uri")
        var url = link.getDestination()

        if (context.shouldSanitizeUrls()) {
            url = context.urlSanitizer().sanitizeLinkUrl(url)
            attrs.put("rel", "nofollow")
        }

        url = context.encodeUrl(url)
        attrs.put("xlink:href", url)
        if (link.getTitle() != null) {
            attrs.put("title", link.getTitle())
        }
        html.tag("ext-link", context.getAttrs(link, "ext-link", attrs))
        visitChildren(link)
        html.tag("/ext-link")
    }

    override fun visit(emphasis: Emphasis) {
        html.tag("italic", context.getAttrs(emphasis, "italic"))
        visitChildren(emphasis)
        html.tag("/italic")
    }

    override fun visit(strong: StrongEmphasis) {
        html.tag("bold", context.getAttrs(strong, "bold"))
        visitChildren(strong)
        html.tag("/bold")
    }

    override fun visit(listItem: ListItem) {
        html.tag("list-item", context.getAttrs(listItem, "list-item"))
        visitChildren(listItem)
        html.tag("/list-item")
        html.line()
    }

    override fun visit(orderedList: OrderedList) {
        renderListBlock(orderedList, "order", context.getAttrs(orderedList, "ol"))
    }

    override fun visit(bulletList: BulletList) {
        renderListBlock(bulletList, "bullet", context.getAttrs(bulletList, "ul"))
    }

    private fun renderListBlock(listBlock: ListBlock, type: String, attributes: Map<String, String>) {
        html.line()
        html.tag("list", attributes + ("list-type" to type))
        html.line()
        visitChildren(listBlock)
        html.line()
        html.tag("/list")
        html.line()
    }

    private class TitleVisitor : AbstractVisitor() {
        private val sb = StringBuilder()

        val title: String
            get() = sb.toString()

        override fun visit(text: Text) {
            sb.append(text.getLiteral())
        }

        override fun visit(code: Code) {
            sb.append(code.getLiteral())
        }

        override fun visit(softLineBreak: SoftLineBreak) {
            sb.append('\n')
        }

        override fun visit(hardLineBreak: HardLineBreak) {
            sb.append('\n')
        }
    }
}
