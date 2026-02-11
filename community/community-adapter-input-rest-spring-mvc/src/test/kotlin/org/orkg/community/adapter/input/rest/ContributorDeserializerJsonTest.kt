package org.orkg.community.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.adapter.input.rest.json.CommunityJacksonModule
import org.orkg.community.domain.Contributor
import org.orkg.community.testing.fixtures.createContributor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.test.context.ContextConfiguration

@JsonTest
@ContextConfiguration(classes = [CommonJacksonModule::class, CommunityJacksonModule::class])
internal class ContributorDeserializerJsonTest {
    @Autowired
    private lateinit var json: JacksonTester<Contributor>

    @Test
    fun `Given a contributor, it gets deserialized correctly`() {
        assertThat(json.parse(serializedContributor().json)).isEqualTo(createContributor())
    }

    private fun serializedContributor() = json.write(createContributor())
}
