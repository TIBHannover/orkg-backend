package org.orkg.contenttypes.adapter.input.rest.jats.dsl

import kotlinx.html.HTMLTag
import kotlinx.html.HtmlBlockTag
import kotlinx.html.HtmlTagMarker
import kotlinx.html.Tag
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import kotlinx.html.visit

open class DESCRIPTION(
    initialAttributes: Map<String, String>,
    override val consumer: TagConsumer<*>,
) : HTMLTag("description", consumer, initialAttributes, null, false, false),
    HtmlBlockTag

@HtmlTagMarker
inline fun Tag.description(classes: String? = null, crossinline block: DESCRIPTION.() -> Unit = {}) {
    DESCRIPTION(attributesMapOf("class", classes), consumer).visit(block)
}
