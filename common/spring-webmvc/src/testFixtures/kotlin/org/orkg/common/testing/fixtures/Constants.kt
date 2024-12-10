package org.orkg.common.testing.fixtures

import org.orkg.common.MediaTypeCapability

val FORMATTED_LABEL_CAPABILITY = MediaTypeCapability("formatted-label", false) {
    it.toBooleanStrictOrNull() ?: throw RuntimeException(it)
}

val INCOMING_STATEMENTS_COUNT_CAPABILITY = MediaTypeCapability("incoming-statements-count", false) {
    it.toBooleanStrictOrNull() ?: throw RuntimeException(it)
}
