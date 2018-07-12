package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StatementTest {

    @Test
    fun orderBySubject_IfSubjectIsDifferent_ShouldSortBySubject() {
        val (smaller, greater) = createStatementsWithDifferentSubjects(
            "111",
            "222"
        )

        assertThat(smaller).isLessThan(greater)
        assertThat(greater).isGreaterThan(smaller)
    }

    @Test
    fun orderBySubject_IfSubjectIsEqualAndPredicateDiffers_ShouldSortByPredicate() {
        val (one, other) = createStatementsWithDifferentSubjects(
            "111",
            "111"
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
            "333",
            "444"
        )

        assertThat(smaller).isLessThan(greater)
        assertThat(greater).isGreaterThan(smaller)
    }

    private fun createStatementsWithDifferentSubjects(
        smallerId: String, greaterId: String
    ): Pair<Statement, Statement> {
        val smaller = Statement(
            null,
            ResourceId(smallerId),
            PredicateId("P222"),
            Object.Resource(ResourceId("333"))
        )
        val greater = Statement(
            null,
            ResourceId(greaterId),
            PredicateId("P222"),
            Object.Resource(ResourceId("333"))
        )
        return Pair(smaller, greater)
    }

    private fun createStatementsWithDifferentPredicates(
        smallerId: String, greaterId: String
    ): Pair<Statement, Statement> {
        val smaller = Statement(
            null,
            ResourceId("111"),
            PredicateId(smallerId),
            Object.Resource(ResourceId("333"))
        )
        val greater = Statement(
            null,
            ResourceId("111"),
            PredicateId(greaterId),
            Object.Resource(ResourceId("333"))
        )
        return Pair(smaller, greater)
    }

    private fun createStatementsWithDifferentObjectsWithResource(
        smallerId: String, greaterId: String
    ): Pair<Statement, Statement> {
        val smaller = Statement(
            null,
            ResourceId("111"),
            PredicateId("P222"),
            Object.Resource(ResourceId(smallerId))
        )
        val greater = Statement(
            null,
            ResourceId("111"),
            PredicateId("P222"),
            Object.Resource(ResourceId(greaterId))
        )
        return Pair(smaller, greater)
    }
}
