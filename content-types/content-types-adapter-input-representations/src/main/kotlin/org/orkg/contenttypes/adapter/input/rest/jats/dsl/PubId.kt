package org.orkg.contenttypes.adapter.input.rest.jats.dsl

import kotlinx.html.HTMLTag
import kotlinx.html.HtmlBlockTag
import kotlinx.html.HtmlTagMarker
import kotlinx.html.Tag
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import kotlinx.html.visit

open class PUBID(
    initialAttributes: Map<String, String>,
    override val consumer: TagConsumer<*>,
) : HTMLTag("pub-id", consumer, initialAttributes, null, false, false),
    HtmlBlockTag

@HtmlTagMarker
@Suppress("ktlint:standard:function-naming")
inline fun Tag.`pub-id`(type: String, classes: String? = null, crossinline block: PUBID.() -> Unit = {}) {
    PUBID(attributesMapOf("class", classes, "pub-id-type", type), consumer).visit(block)
}
