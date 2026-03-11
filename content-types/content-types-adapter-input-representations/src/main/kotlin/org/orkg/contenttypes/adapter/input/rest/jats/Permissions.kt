package org.orkg.contenttypes.adapter.input.rest.jats

import kotlinx.html.HTMLTag
import kotlinx.html.HtmlBlockTag
import kotlinx.html.HtmlTagMarker
import kotlinx.html.Tag
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import kotlinx.html.visit

open class PERMISSIONS(
    initialAttributes: Map<String, String>,
    override val consumer: TagConsumer<*>,
) : HTMLTag("permissions", consumer, initialAttributes, null, false, false),
    HtmlBlockTag

@HtmlTagMarker
inline fun Tag.permissions(classes: String? = null, crossinline block: PERMISSIONS.() -> Unit = {}) {
    PERMISSIONS(attributesMapOf("class", classes), consumer).visit(block)
}
