package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

// @JsonTest
class ResourceIdJsonTest {

    private val mapper: ObjectMapper = ObjectMapper()

    @Test
    fun testDeserializeValuePassedAsString() {
        val deserializedId = mapper.readValue<ResourceId>("\"R123\"")
        assertThat(deserializedId).isEqualTo(ResourceId(123))
    }

    @Test
    fun testSerializeIdToString() {
        val serializedId = mapper.writeValueAsString(ResourceId(123))
        assertThat(serializedId).isEqualTo("\"R123\"")
    }
}
