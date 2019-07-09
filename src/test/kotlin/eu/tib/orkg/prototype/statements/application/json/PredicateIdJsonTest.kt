package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

// @JsonTest
class PredicateIdJsonTest {

    private val mapper: ObjectMapper = ObjectMapper()

    @Test
    fun testDeserializeValuePassedAsString() {
        val deserializedId = mapper.readValue<PredicateId>("\"P123\"")
        assertThat(deserializedId).isEqualTo(PredicateId(123))
    }

    @Test
    fun testSerializeIdToString() {
        val serializedId = mapper.writeValueAsString(PredicateId(123))
        assertThat(serializedId).isEqualTo("\"P123\"")
    }
}
