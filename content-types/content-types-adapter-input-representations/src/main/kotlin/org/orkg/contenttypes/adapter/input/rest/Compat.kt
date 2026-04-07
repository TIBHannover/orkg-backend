package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.MediaTypeCapability
import org.orkg.graph.domain.FormattedLabelVersion
import java.util.Optional

val TRANSPOSED_CAPABILITY = MediaTypeCapability("transposed", false) { it.toBoolean() }
