package org.orkg.common.json

import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase
import org.orkg.common.PageableRepresentation

/**
 * Analogue class to org.springframework.data.web.config.SpringDataJacksonConfiguration.UnpagedAsInstanceSerializer
 */
class UnpagedPageableRepresentationSerializer : ToStringSerializerBase(PageableRepresentation::class.java) {
    override fun valueToString(value: Any?): String = "INSTANCE"
}
