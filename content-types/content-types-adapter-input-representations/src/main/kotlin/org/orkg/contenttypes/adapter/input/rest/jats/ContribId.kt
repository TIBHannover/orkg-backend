package org.orkg.contenttypes.adapter.input.rest.jats

import kotlinx.html.HTMLTag
import kotlinx.html.HtmlBlockTag
import kotlinx.html.HtmlTagMarker
import kotlinx.html.Tag
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import kotlinx.html.visit

open class CONTRIBID(
    initialAttributes: Map<String, String>,
    override val consumer: TagConsumer<*>,
) : HTMLTag("contrib-id", consumer, initialAttributes, null, false, false),
    HtmlBlockTag

@HtmlTagMarker
@Suppress("ktlint:standard:function-naming")
inline fun Tag.`contrib-id`(type: String, classes: String? = null, crossinline block: CONTRIBID.() -> Unit = {}) {
    CONTRIBID(attributesMapOf("class", classes, "contrib-id-type", type), consumer).visit(block)
}
