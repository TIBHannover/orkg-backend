package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StatementTest {

    @Test
    fun orderBySubject_IfSubjectIsDifferent_ShouldSortBySubject() {
        val (smaller, greater) = createStatementsWithDifferentSubjects(
            "R111",
            "R222"
        )

        assertThat(smaller).isLessThan(greater)
        assertThat(greater).isGreaterThan(smaller)
    }

    @Test
    fun orderBySubject_IfSubjectIsEqualAndPredicateDiffers_ShouldSortByPredicate() {
        val (one, other) = createStatementsWithDifferentSubjects(
            "R111",
            "R111"
        )

        assertThat(one).isEqualTo(other)
    }

    @Test
    fun orderByPredicate_IfSubjectIsEqualAndPredicateIsDifferent_ShouldSortByPredicate() {
        val (smaller, greater) = createStatementsWithDifferentPredicates(
            "P1",
            "P2"
        )

        assertThat(smaller).isLessThan(greater)
        assertThat(greater).isGreaterThan(smaller)
    }

    @Test
    fun orderByPredicate_IfSubjectIsEqualAndPredicateIsEqual_ShouldSortByPredicate() {
        val (one, other) = createStatementsWithDifferentPredicates(
            "P1",
            "P1"
        )

        assertThat(one).isEqualTo(other)
    }

    @Test
    fun orderByObject_IfSubjectAndPredicateAreEqual_ShouldSortByObject() {
        val (smaller, greater) = createStatementsWithDifferentObjectsWithResource(
            "R333",
            "R444"
        )

        assertThat(smaller).isLessThan(greater)
        assertThat(greater).isGreaterThan(smaller)
    }

    private fun createStatementsWithDifferentSubjects(
        smallerId: String,
        greaterId: String
    ): Pair<Statement, Statement> {
        val smaller = Statement(
            null,
            ResourceId(smallerId),
            PredicateId("P222"),
            Object.Resource(ResourceId("R333"))
        )
        val greater = Statement(
            null,
            ResourceId(greaterId),
            PredicateId("P222"),
            Object.Resource(ResourceId("R333"))
        )
        return Pair(smaller, greater)
    }

    private fun createStatementsWithDifferentPredicates(
        smallerId: String,
        greaterId: String
    ): Pair<Statement, Statement> {
        val smaller = Statement(
            null,
            ResourceId("R111"),
            PredicateId(smallerId),
            Object.Resource(ResourceId("R333"))
        )
        val greater = Statement(
            null,
            ResourceId("R111"),
            PredicateId(greaterId),
            Object.Resource(ResourceId("R333"))
        )
        return Pair(smaller, greater)
    }

    private fun createStatementsWithDifferentObjectsWithResource(
        smallerId: String,
        greaterId: String
    ): Pair<Statement, Statement> {
        val smaller = Statement(
            null,
            ResourceId("R111"),
            PredicateId("P222"),
            Object.Resource(ResourceId(smallerId))
        )
        val greater = Statement(
            null,
            ResourceId("R111"),
            PredicateId("P222"),
            Object.Resource(ResourceId(greaterId))
        )
        return Pair(smaller, greater)
    }
}
