package org.orkg.graph.adapter.input.rest

import org.orkg.common.MediaTypeCapability
import org.orkg.graph.domain.FormattedLabelVersion
import java.util.Optional

val FORMATTED_LABELS_CAPABILITY = MediaTypeCapability("formatted-labels", Optional.empty<FormattedLabelVersion>()) {
    if (it.isEmpty()) {
        return@MediaTypeCapability Optional.empty()
    }
    try {
        Optional.of(FormattedLabelVersion.valueOf(it.uppercase()))
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("""Value "$it" is not a valid formatted label version.""")
    }
}
