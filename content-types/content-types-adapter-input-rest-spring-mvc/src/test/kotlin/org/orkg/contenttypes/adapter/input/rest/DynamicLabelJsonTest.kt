package org.orkg.contenttypes.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.graph.domain.DynamicLabel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.Jackson2Tester
import org.springframework.test.context.ContextConfiguration

@JsonTest
@ContextConfiguration(classes = [CommonJacksonModule::class, ContentTypeJacksonModule::class])
internal class DynamicLabelJsonTest {
    @Autowired
    private lateinit var json: Jackson2Tester<DynamicLabel>

    private val instance = DynamicLabel("[ {0} ]travels[by {1} ][from {2} ][to {3} ][on {4} ]")
    private val serializedLabel = "\"[ {0} ]travels[by {1} ][from {2} ][to {3} ][on {4} ]\""

    @Test
    fun `Given a dynamic label, it gets deserialized correctly`() {
        assertThat(json.parse(serializedLabel)).isEqualTo(instance)
    }

    @Test
    fun `Given a dynamic label, it gets serialized correctly`() {
        assertThat(json.write(instance)).isEqualTo(serializedLabel)
    }
}
