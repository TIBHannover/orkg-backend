package org.orkg.contenttypes.adapter.input.rest.jats

import kotlinx.html.HTMLTag
import kotlinx.html.HtmlBlockTag
import kotlinx.html.HtmlTagMarker
import kotlinx.html.Tag
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import kotlinx.html.visit

open class COPYRIGHTHOLDER(
    initialAttributes: Map<String, String>,
    override val consumer: TagConsumer<*>,
) : HTMLTag("copyright-holder", consumer, initialAttributes, null, false, false),
    HtmlBlockTag

@HtmlTagMarker
@Suppress("ktlint:standard:function-naming")
inline fun Tag.`copyright-holder`(classes: String? = null, crossinline block: COPYRIGHTHOLDER.() -> Unit = {}) {
    COPYRIGHTHOLDER(attributesMapOf("class", classes), consumer).visit(block)
}
