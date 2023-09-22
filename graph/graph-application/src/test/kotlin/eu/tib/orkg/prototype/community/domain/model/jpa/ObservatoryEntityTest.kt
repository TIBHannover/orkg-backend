package eu.tib.orkg.prototype.community.domain.model.jpa

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.ObservatoryEntity
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.OrganizationEntity
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.toObservatory
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import java.util.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Given a ObservatoryEntity")
internal class ObservatoryEntityTest {

    @Nested
    @DisplayName("that has several organizations")
    inner class ThatHasSeveralOrganizations {
        private val setOfRandomUUIDs = arrayOf(
            UUID.fromString("1804ae87-8ea6-4d29-bdc6-bf008a942471"),
            UUID.fromString("f270f9fe-6326-4b8c-a627-72a45a152a7f"),
            UUID.fromString("410d7d35-c8cc-43ba-8d84-283be9a20067")
        )
        private val setOfOrganizationIds =
            setOfRandomUUIDs.map(::OrganizationId).toTypedArray()

        @Nested
        @DisplayName("when converted to a domain object")
        inner class WhenConvertedToDomainObject {
            @Test
            @DisplayName("it should return the list of organization IDs")
            fun itShouldReturnTheListOfOrganizationIDs() {
                val entity = ObservatoryEntity().apply {
                    name = "Observatory Name"
                    id = UUID.fromString("66ca8f3f-e34b-4489-a2dc-8d6095f89983")
                    organizations = setOfRandomUUIDs.map(::OrganizationEntity).toMutableSet()
                    displayId = "display_id"
                }
                val observatory = entity.toObservatory()
                assertThat(observatory.organizationIds).hasSize(3) // simple check for duplicates, should never trigger
                assertThat(observatory.organizationIds).containsExactlyInAnyOrder(*setOfOrganizationIds)
            }
        }
    }
}
