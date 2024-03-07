package org.orkg.contenttypes.domain.actions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SustainableDevelopmentGoalNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resources

class SDGValidatorUnitTest {
    private val sdgCreateValidator = SDGValidator<Set<ThingId>?, Set<ThingId>>({ it }, { it })

    @Test
    fun `Given a list of sdgs, when validating, it returns success`() {
        sdgCreateValidator(Resources.sustainableDevelopmentGoals.toSet(), emptySet())
    }

    @Test
    fun `Given a list of sdgs, when sdg is unknown, it throws an exception`() {
        val id = Classes.paper
        assertThrows<SustainableDevelopmentGoalNotFound> { sdgCreateValidator(setOf(id), emptySet()) }
    }

    @Test
    fun `Given a list of sdgs, when old list of sdgs is identical, it does nothing`() {
        val ids = setOf(ThingId("SDG_1"), ThingId("SDG_2"))
        sdgCreateValidator(ids, ids)
    }

    @Test
    fun `Given a list of sdgs, when no new sdgs list is set, it does nothing`() {
        val ids = setOf(ThingId("SDG_1"), ThingId("SDG_2"))
        sdgCreateValidator(null, ids)
    }
}
