package org.orkg.contenttypes.adapter.input.rest.jats

import kotlinx.html.HTMLTag
import kotlinx.html.HtmlBlockTag
import kotlinx.html.HtmlTagMarker
import kotlinx.html.Tag
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import kotlinx.html.visit

open class CONTRIBGROUP(
    initialAttributes: Map<String, String>,
    override val consumer: TagConsumer<*>,
) : HTMLTag("contrib-group", consumer, initialAttributes, null, false, false),
    HtmlBlockTag

@HtmlTagMarker
@Suppress("ktlint:standard:function-naming")
inline fun Tag.`contrib-group`(classes: String? = null, crossinline block: CONTRIBGROUP.() -> Unit = {}) {
    CONTRIBGROUP(attributesMapOf("class", classes), consumer).visit(block)
}
