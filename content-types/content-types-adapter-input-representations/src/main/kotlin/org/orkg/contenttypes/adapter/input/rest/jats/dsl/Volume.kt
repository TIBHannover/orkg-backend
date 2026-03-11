package org.orkg.contenttypes.adapter.input.rest.jats.dsl

import kotlinx.html.HTMLTag
import kotlinx.html.HtmlBlockTag
import kotlinx.html.HtmlTagMarker
import kotlinx.html.Tag
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import kotlinx.html.visit

open class VOLUME(
    initialAttributes: Map<String, String>,
    override val consumer: TagConsumer<*>,
) : HTMLTag("volume", consumer, initialAttributes, null, false, false),
    HtmlBlockTag

@HtmlTagMarker
inline fun Tag.volume(classes: String? = null, crossinline block: VOLUME.() -> Unit = {}) {
    VOLUME(attributesMapOf("class", classes), consumer).visit(block)
}
