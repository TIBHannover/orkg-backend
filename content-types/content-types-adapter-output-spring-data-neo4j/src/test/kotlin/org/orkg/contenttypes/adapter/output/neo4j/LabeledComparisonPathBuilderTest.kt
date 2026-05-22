package org.orkg.contenttypes.adapter.output.neo4j

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.SpringDataNeo4jComparisonAuxiliaryAdapter.LabeledComparisonPathBuilder
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jComparisonPathEntry
import org.orkg.contenttypes.domain.ComparisonPath
import org.orkg.contenttypes.domain.LabeledComparisonPath

internal class LabeledComparisonPathBuilderTest {
    @Test
    fun `Given a list of path entries, when building a labeled comparison path tree, it successfully builds a tree`() {
        /*
         * ComparisonResource           hasPublishedVersion PublishedComparisonResource
         *                              compareContribution ContributionResource
         * PublishedComparisonResource  compareContribution ContributionResource
         */
        val entries = setOf(
            createPredicatePathEntry(
                subjectId = ThingId("ComparisonResource"),
                predicateId = ThingId("hasPublishedVersion"),
                objectIds = listOf(ThingId("PublishedComparisonResource")),
            ),
            createPredicatePathEntry(
                subjectId = ThingId("ComparisonResource"),
                predicateId = ThingId("compareContribution"),
                objectIds = listOf(ThingId("ContributionResource")),
            ),
            createPredicatePathEntry(
                subjectId = ThingId("PublishedComparisonResource"),
                predicateId = ThingId("compareContribution"),
                objectIds = listOf(ThingId("ContributionResource"), ThingId("ComparisonResource")),
            ),
        )
        val expected = listOf(
            LabeledComparisonPath(
                id = ThingId("compareContribution"),
                label = "compareContribution",
                description = null,
                type = ComparisonPath.Type.PREDICATE,
                children = emptyList(),
                sources = 1,
            ),
            LabeledComparisonPath(
                id = ThingId("hasPublishedVersion"),
                label = "hasPublishedVersion",
                description = null,
                type = ComparisonPath.Type.PREDICATE,
                children = listOf(
                    LabeledComparisonPath(
                        id = ThingId("compareContribution"),
                        label = "compareContribution",
                        description = null,
                        type = ComparisonPath.Type.PREDICATE,
                        children = emptyList(),
                        sources = 1,
                    ),
                ),
                sources = 1,
            ),
        )

        val result = LabeledComparisonPathBuilder.buildTree(
            entries = entries,
            rootIds = setOf(ThingId("ComparisonResource")),
            maxDepth = 10,
        )
        result shouldBe expected
    }

    @Test
    fun `Given a list of path entries, when building a labeled comparison path tree, it returns all available paths to a thing`() {
        /*
         * Root predicate1  ContributionResource
         *      predicate2  ContributionResource
         */
        val entries = setOf(
            createPredicatePathEntry(
                subjectId = ThingId("Root"),
                predicateId = ThingId("predicate1"),
                objectIds = listOf(ThingId("ContributionResource")),
            ),
            createPredicatePathEntry(
                subjectId = ThingId("Root"),
                predicateId = ThingId("predicate2"),
                objectIds = listOf(ThingId("ContributionResource")),
            ),
        )
        val expected = listOf(
            LabeledComparisonPath(
                id = ThingId("predicate1"),
                label = "predicate1",
                description = null,
                type = ComparisonPath.Type.PREDICATE,
                children = emptyList(),
                sources = 1,
            ),
            LabeledComparisonPath( // this should exist!
                id = ThingId("predicate2"),
                label = "predicate2",
                description = null,
                type = ComparisonPath.Type.PREDICATE,
                children = emptyList(),
                sources = 1,
            ),
        )

        val result = LabeledComparisonPathBuilder.buildTree(
            entries = entries,
            rootIds = setOf(ThingId("Root")),
            maxDepth = 10,
        )
        result shouldBe expected
    }

    @Test
    fun `Given a list of path entries, when building a labeled comparison path tree, it does not expand paths for things that are encountered in shorter paths`() {
        /*
         * Root predicate1  ContributionResource
         *      predicate3  SomeObject
         *      predicate2  Intermediate
         * Intermediate predicate1 ContributionResource
         */
        val entries = setOf(
            createPredicatePathEntry(
                subjectId = ThingId("Root"),
                predicateId = ThingId("predicate1"),
                objectIds = listOf(ThingId("ContributionResource")),
            ),
            createPredicatePathEntry(
                subjectId = ThingId("Root"),
                predicateId = ThingId("predicate2"),
                objectIds = listOf(ThingId("Intermediate")),
            ),
            createPredicatePathEntry(
                subjectId = ThingId("Intermediate"),
                predicateId = ThingId("predicate1"),
                objectIds = listOf(ThingId("ContributionResource")),
            ),
            createPredicatePathEntry(
                subjectId = ThingId("ContributionResource"),
                predicateId = ThingId("predicate3"),
                objectIds = listOf(ThingId("SomeObject")),
            ),
        )
        val expected = listOf(
            LabeledComparisonPath(
                id = ThingId("predicate1"),
                label = "predicate1",
                description = null,
                type = ComparisonPath.Type.PREDICATE,
                children = listOf(
                    LabeledComparisonPath(
                        id = ThingId("predicate3"),
                        label = "predicate3",
                        description = null,
                        type = ComparisonPath.Type.PREDICATE,
                        children = emptyList(),
                        sources = 1,
                    ),
                ),
                sources = 1,
            ),
            LabeledComparisonPath(
                id = ThingId("predicate2"),
                label = "predicate2",
                description = null,
                type = ComparisonPath.Type.PREDICATE,
                children = listOf(
                    LabeledComparisonPath(
                        id = ThingId("predicate1"),
                        label = "predicate1",
                        description = null,
                        type = ComparisonPath.Type.PREDICATE,
                        children = emptyList(), // this should be empty!
                        sources = 1,
                    ),
                ),
                sources = 1,
            ),
        )

        val result = LabeledComparisonPathBuilder.buildTree(
            entries = entries,
            rootIds = setOf(ThingId("Root")),
            maxDepth = 10,
        )
        result shouldBe expected
    }

    @Test
    fun `Given a list of path entries, when building a labeled comparison path tree, it merges paths correctly`() {
        /*
         * Root
         *   predicate1
         *     ContributionResource
         *        predicate3
         *          SomeThing
         *   predicate2
         *     ContributionResource
         *        predicate3
         *          SomeThing
         * Root2
         *   predicate1
         *     ContributionResource
         *        predicate3
         *          SomeThing
         *   predicate2
         *     ContributionResource
         *        predicate3
         *          SomeThing
         *   predicate3
         *     SomeThing
         *       predicate4
         *         SomeObject
         */
        val entries = setOf(
            createPredicatePathEntry(
                subjectId = ThingId("Root"),
                predicateId = ThingId("predicate1"),
                objectIds = listOf(ThingId("ContributionResource")),
            ),
            createPredicatePathEntry(
                subjectId = ThingId("Root"),
                predicateId = ThingId("predicate2"),
                objectIds = listOf(ThingId("ContributionResource")),
            ),
            createPredicatePathEntry(
                subjectId = ThingId("Root2"),
                predicateId = ThingId("predicate1"),
                objectIds = listOf(ThingId("ContributionResource")),
            ),
            createPredicatePathEntry(
                subjectId = ThingId("Root2"),
                predicateId = ThingId("predicate2"),
                objectIds = listOf(ThingId("ContributionResource")),
            ),
            createPredicatePathEntry(
                subjectId = ThingId("Root2"),
                predicateId = ThingId("predicate3"),
                objectIds = listOf(ThingId("SomeThing")),
            ),
            createPredicatePathEntry(
                subjectId = ThingId("ContributionResource"),
                predicateId = ThingId("predicate3"),
                objectIds = listOf(ThingId("SomeThing")),
            ),
            createPredicatePathEntry(
                subjectId = ThingId("SomeThing"),
                predicateId = ThingId("predicate4"),
                objectIds = listOf(ThingId("SomeObject")),
            ),
        )
        val expected = listOf(
            LabeledComparisonPath(
                id = ThingId("predicate1"),
                label = "predicate1",
                description = null,
                type = ComparisonPath.Type.PREDICATE,
                children = listOf(
                    LabeledComparisonPath(
                        id = ThingId("predicate3"),
                        label = "predicate3",
                        description = null,
                        type = ComparisonPath.Type.PREDICATE,
                        children = emptyList(), // this should be empty!
                        sources = 2,
                    ),
                ),
                sources = 2,
            ),
            LabeledComparisonPath( // this should exist!
                id = ThingId("predicate2"),
                label = "predicate2",
                description = null,
                type = ComparisonPath.Type.PREDICATE,
                children = listOf(
                    LabeledComparisonPath(
                        id = ThingId("predicate3"),
                        label = "predicate3",
                        description = null,
                        type = ComparisonPath.Type.PREDICATE,
                        children = emptyList(), // this should be empty!
                        sources = 2,
                    ),
                ),
                sources = 2,
            ),
            LabeledComparisonPath(
                id = ThingId("predicate3"),
                label = "predicate3",
                description = null,
                type = ComparisonPath.Type.PREDICATE,
                children = listOf( // this should not be empty!
                    LabeledComparisonPath(
                        id = ThingId("predicate4"),
                        label = "predicate4",
                        description = null,
                        type = ComparisonPath.Type.PREDICATE,
                        children = emptyList(),
                        sources = 1,
                    ),
                ),
                sources = 1,
            ),
        )

        val result = LabeledComparisonPathBuilder.buildTree(
            entries = entries,
            rootIds = setOf(ThingId("Root"), ThingId("Root2")),
            maxDepth = 10,
        )
        result shouldBe expected
    }

    private fun createPredicatePathEntry(subjectId: ThingId, predicateId: ThingId, objectIds: List<ThingId>) =
        Neo4jComparisonPathEntry(
            subjectId = subjectId,
            predicateId = predicateId,
            description = null,
            objectIds = objectIds,
            predicateLabel = predicateId.value,
            type = ComparisonPath.Type.PREDICATE,
        )
}
