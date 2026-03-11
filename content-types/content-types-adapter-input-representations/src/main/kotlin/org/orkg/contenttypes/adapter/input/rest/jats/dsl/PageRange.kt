package org.orkg.contenttypes.adapter.input.rest.jats.dsl

import kotlinx.html.HTMLTag
import kotlinx.html.HtmlBlockTag
import kotlinx.html.HtmlTagMarker
import kotlinx.html.Tag
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import kotlinx.html.visit

open class PAGERANGE(
    initialAttributes: Map<String, String>,
    override val consumer: TagConsumer<*>,
) : HTMLTag("page-range", consumer, initialAttributes, null, false, false),
    HtmlBlockTag

@HtmlTagMarker
@Suppress("ktlint:standard:function-naming")
inline fun Tag.`page-range`(classes: String? = null, crossinline block: PAGERANGE.() -> Unit = {}) {
    PAGERANGE(attributesMapOf("class", classes), consumer).visit(block)
}
