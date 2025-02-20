package org.orkg.community.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.adapter.input.rest.json.CommunityJacksonModule
import org.orkg.community.domain.Organization
import org.orkg.community.testing.fixtures.createOrganization
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.test.context.ContextConfiguration

@JsonTest
@ContextConfiguration(classes = [CommonJacksonModule::class, CommunityJacksonModule::class])
internal class ObservatoryDeserializerJsonTest {
    @Autowired
    private lateinit var json: JacksonTester<Organization>

    @Test
    fun `Given a organization, it gets deserialized correctly`() {
        assertThat(json.parse(serializedOrganization().json)).isEqualTo(createOrganization())
    }

    private fun serializedOrganization() = json.write(createOrganization())
}
