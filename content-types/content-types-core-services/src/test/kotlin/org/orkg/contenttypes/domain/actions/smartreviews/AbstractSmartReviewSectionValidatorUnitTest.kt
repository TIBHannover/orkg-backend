package org.orkg.contenttypes.domain.actions.smartreviews

import io.mockk.mockk
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.ThingRepository

abstract class AbstractSmartReviewSectionValidatorUnitTest : MockkBaseTest {
    protected val resourceRepository: ResourceRepository = mockk()
    protected val predicateRepository: PredicateRepository = mockk()
    protected val thingRepository: ThingRepository = mockk()

    protected val abstractSmartReviewSectionValidator = AbstractSmartReviewSectionValidator(
        resourceRepository,
        predicateRepository,
        thingRepository
    )
}
