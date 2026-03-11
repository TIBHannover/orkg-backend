package org.orkg.contenttypes.adapter.input.rest.jats

import kotlinx.html.BODY
import kotlinx.html.HtmlTagMarker
import kotlinx.html.Tag
import kotlinx.html.attributesMapOf
import kotlinx.html.visit

@HtmlTagMarker
inline fun Tag.body(classes: String? = null, crossinline block: BODY.() -> Unit = {}) {
    BODY(attributesMapOf("class", classes), consumer).visit(block)
}
