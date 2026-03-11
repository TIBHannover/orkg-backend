package org.orkg.contenttypes.adapter.input.rest.jats

import kotlinx.html.HTMLTag
import kotlinx.html.HtmlBlockTag
import kotlinx.html.HtmlTagMarker
import kotlinx.html.Tag
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import kotlinx.html.visit

open class ARTICLEID(
    initialAttributes: Map<String, String>,
    override val consumer: TagConsumer<*>,
) : HTMLTag("article-id", consumer, initialAttributes, null, false, false),
    HtmlBlockTag

@HtmlTagMarker
@Suppress("ktlint:standard:function-naming")
inline fun Tag.`article-id`(type: String, classes: String? = null, crossinline block: ARTICLEID.() -> Unit = {}) {
    ARTICLEID(attributesMapOf("class", classes, "pub-id-type", type), consumer).visit(block)
}
