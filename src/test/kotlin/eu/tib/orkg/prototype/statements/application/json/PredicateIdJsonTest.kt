package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.*
import eu.tib.orkg.prototype.statements.domain.model.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*


//@JsonTest
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
