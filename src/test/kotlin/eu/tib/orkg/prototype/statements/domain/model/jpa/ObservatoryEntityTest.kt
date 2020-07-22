package eu.tib.orkg.prototype.statements.domain.model.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

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

        @Nested
        @DisplayName("when converted to a domain object")
        inner class WhenConvertedToDomainObject {
            @Test
            @DisplayName("it should return the list of organization IDs")
            fun itShouldReturnTheListOfOrganizationIDs() {
                val entity = ObservatoryEntity().apply {
                    organizations = setOfRandomUUIDs.map(::OrganizationEntity).toSet()
                }
                val observatory = entity.toObservatory()
                assertThat(observatory.organization_ids).hasSize(3) // simple check for duplicates, should never trigger
                assertThat(observatory.organization_ids).containsExactlyInAnyOrder(*setOfRandomUUIDs)
            }
        }
    }
}
