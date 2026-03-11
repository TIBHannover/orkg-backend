package org.orkg.contenttypes.adapter.input.rest.jats

import kotlinx.html.HTMLTag
import kotlinx.html.HtmlBlockTag
import kotlinx.html.HtmlTagMarker
import kotlinx.html.Tag
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import kotlinx.html.visit

open class TITLEGROUP(
    initialAttributes: Map<String, String>,
    override val consumer: TagConsumer<*>,
) : HTMLTag("title-group", consumer, initialAttributes, null, false, false),
    HtmlBlockTag

@HtmlTagMarker
@Suppress("ktlint:standard:function-naming")
inline fun Tag.`title-group`(classes: String? = null, crossinline block: TITLEGROUP.() -> Unit = {}) {
    TITLEGROUP(attributesMapOf("class", classes), consumer).visit(block)
}
