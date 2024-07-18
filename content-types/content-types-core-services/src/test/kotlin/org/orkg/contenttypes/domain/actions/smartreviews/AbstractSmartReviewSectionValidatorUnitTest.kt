package org.orkg.contenttypes.domain.actions.smartreviews

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.mockk
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.ThingRepository

abstract class AbstractSmartReviewSectionValidatorUnitTest {
    protected val resourceRepository: ResourceRepository = mockk()
    protected val predicateRepository: PredicateRepository = mockk()
    protected val thingRepository: ThingRepository = mockk()

    protected val abstractSmartReviewSectionValidator = AbstractSmartReviewSectionValidator(
        resourceRepository, predicateRepository, thingRepository
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository, predicateRepository, thingRepository)
    }
}
