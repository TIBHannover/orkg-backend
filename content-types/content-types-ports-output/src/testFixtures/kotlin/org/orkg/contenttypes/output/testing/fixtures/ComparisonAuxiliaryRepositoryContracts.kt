package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.shouldBe
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonColumnData
import org.orkg.contenttypes.domain.ComparisonPath
import org.orkg.contenttypes.domain.ComparisonTableValue
import org.orkg.contenttypes.domain.LabeledComparisonPath
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.SimpleComparisonPath
import org.orkg.contenttypes.domain.testing.fixtures.withRosettaStoneStatementMappings
import org.orkg.contenttypes.output.ComparisonAuxiliaryRepository
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.graph.testing.fixtures.withGraphMappings

fun <
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository,
    U : RosettaStoneStatementRepository,
    T : ComparisonAuxiliaryRepository,
> comparisonAuxiliaryContract(
    statementRepository: S,
    classRepository: C,
    literalRepository: L,
    resourceRepository: R,
    predicateRepository: P,
    rosettaStoneStatementRepository: U,
    comparisonAuxiliaryRepository: T,
) = describeSpec {
    beforeTest {
        statementRepository.deleteAll()
        classRepository.deleteAll()
        literalRepository.deleteAll()
        resourceRepository.deleteAll()
        predicateRepository.deleteAll()
        rosettaStoneStatementRepository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull, // FIXME: because "id" is nullable
        )
            .withStandardMappings()
            .withGraphMappings()
            .withRosettaStoneStatementMappings(),
    )

    val saveThing: (Thing) -> Unit = {
        when (it) {
            is Class -> classRepository.save(it)
            is Literal -> literalRepository.save(it)
            is Resource -> resourceRepository.save(it)
            is Predicate -> predicateRepository.save(it)
        }
    }

    val saveStatement: (GeneralStatement) -> Unit = {
        saveThing(it.subject)
        saveThing(it.predicate)
        saveThing(it.`object`)
        statementRepository.save(it)
    }

    fun List<LabeledComparisonPath>.toSimpleComparisonPaths(): List<SimpleComparisonPath> =
        map { SimpleComparisonPath(it.id, it.type, it.children.toSimpleComparisonPaths()) }

    fun List<LabeledComparisonPath>.withoutSources(): List<LabeledComparisonPath> =
        map { LabeledComparisonPath(it.id, it.label, it.description, it.type, it.children.withoutSources()) }

    describe("finding all labeled comparison paths") {
        val comparison = fabricator.random<Resource>().copy(classes = setOf(Classes.comparison))
        val contribution1 = fabricator.random<Resource>().copy(classes = setOf(Classes.contribution))
        val contribution2 = fabricator.random<Resource>().copy(classes = setOf(Classes.contribution))
        val contribution3 = fabricator.random<Resource>().copy(classes = setOf(Classes.contribution))

        val hasContribution = createPredicate(id = Predicates.comparesContribution, label = "has contribution")
        val hasRosettaStoneContribution = createPredicate(id = Predicates.comparesRosettaStoneContribution, label = "has rosetta stone contribution")
        val hasResult = createPredicate(id = ThingId("hasResult"), label = "has result")
        val hasOtherResult = createPredicate(id = ThingId("hasOtherResult"), label = "has other result")
        val hasLabel = createPredicate(id = ThingId("hasLabel"), label = "has label")
        val shProperty = createPredicate(id = Predicates.shProperty, label = "sh:property")
        val shOrder = createPredicate(id = Predicates.shOrder, label = "sh:order")
        val placeholder = createPredicate(id = Predicates.placeholder, label = "has placeholder")
        val description = createPredicate(id = Predicates.description, label = "has description")

        val r1 = fabricator.random<Resource>().copy(id = ThingId("R1"))
        val r2 = fabricator.random<Resource>().copy(id = ThingId("R2"))
        val r3 = fabricator.random<Resource>().copy(id = ThingId("R3"))
        val r4 = fabricator.random<Resource>().copy(id = ThingId("R4"))
        val r5 = fabricator.random<Resource>().copy(id = ThingId("R5"))

        val rosettaStoneTemplate1 = fabricator.random<Resource>().copy(
            id = ThingId("Template1"),
            label = "rosettaStoneTemplate1",
            classes = setOf(Classes.rosettaNodeShape),
        )
        val templateProperty1 = fabricator.random<Resource>().copy(id = ThingId("TP1"), classes = setOf(Classes.propertyShape))
        val templateProperty2 = fabricator.random<Resource>().copy(id = ThingId("TP2"), classes = setOf(Classes.propertyShape))
        val templateProperty3 = fabricator.random<Resource>().copy(id = ThingId("TP3"), classes = setOf(Classes.propertyShape))
        val templateProperty1Placeholder = fabricator.random<Literal>().copy(id = ThingId("placeholder1"), label = "subject")
        val templateProperty2Placeholder = fabricator.random<Literal>().copy(id = ThingId("placeholder2"), label = "object1")
        val templateProperty3Placeholder = fabricator.random<Literal>().copy(id = ThingId("placeholder3"), label = "object2")
        val templateProperty1Order = fabricator.random<Literal>().copy(id = ThingId("order1"), label = "0")
        val templateProperty2Order = fabricator.random<Literal>().copy(id = ThingId("order2"), label = "1")
        val templateProperty3Order = fabricator.random<Literal>().copy(id = ThingId("order3"), label = "2")

        val hasOtherResultDescription = fabricator.random<Literal>().copy(label = "hasOtherResult description")
        val rosettaStoneTemplate1Description = fabricator.random<Literal>().copy(label = "Template1 description")
        val templateProperty2Description = fabricator.random<Literal>().copy(label = "template property 2 description")

        val rosettaStoneStatement1 = fabricator.random<RosettaStoneStatement>().copy(
            contextId = contribution2.id,
            templateId = rosettaStoneTemplate1.id,
        )
        val createGraph = {
            // comparison
            //  comparesContribution
            //      contribution1
            //          hasResult
            //              R1
            //                  hasOtherResult
            //                      R2
            //              R2
            //                  hasResult
            //                      R1
            //          hasOtherResult
            //              R3
            //  comparesContribution
            //      contribution2
            //          hasOtherResult
            //              R4
            //          hasLabel
            //              R5
            //  comparesContribution
            //      contribution3
            // comparesRosettaStoneContriution
            //  contribution2
            //      rosettaStoneStatement1
            //          CONTEXT
            //              rosettaStoneStatement1

            // description statements

            saveStatement(createStatement(id = fabricator.random(), subject = hasOtherResult, predicate = description, `object` = hasOtherResultDescription))
            saveStatement(createStatement(id = fabricator.random(), subject = rosettaStoneTemplate1, predicate = description, `object` = rosettaStoneTemplate1Description))
            saveStatement(createStatement(id = fabricator.random(), subject = templateProperty2, predicate = description, `object` = templateProperty2Description))

            // comparison

            saveStatement(createStatement(id = fabricator.random(), subject = comparison, predicate = hasContribution, `object` = contribution1))
            saveStatement(createStatement(id = fabricator.random(), subject = comparison, predicate = hasContribution, `object` = contribution2))
            saveStatement(createStatement(id = fabricator.random(), subject = comparison, predicate = hasContribution, `object` = contribution3))
            saveStatement(createStatement(id = fabricator.random(), subject = comparison, predicate = hasRosettaStoneContribution, `object` = contribution2))

            // contribution 1

            saveStatement(createStatement(id = fabricator.random(), subject = contribution1, predicate = hasResult, `object` = r1))
            saveStatement(createStatement(id = fabricator.random(), subject = contribution1, predicate = hasResult, `object` = r2))
            saveStatement(createStatement(id = fabricator.random(), subject = contribution1, predicate = hasOtherResult, `object` = r3))

            // contribution 2

            saveStatement(createStatement(id = fabricator.random(), subject = contribution2, predicate = hasOtherResult, `object` = r4))
            saveStatement(createStatement(id = fabricator.random(), subject = contribution2, predicate = hasLabel, `object` = r5))

            // r1

            saveStatement(createStatement(id = fabricator.random(), subject = r1, predicate = hasOtherResult, `object` = r2))

            // r2

            saveStatement(createStatement(id = fabricator.random(), subject = r2, predicate = hasResult, `object` = r1))

            // rosettaStoneTemplate1

            saveStatement(createStatement(id = fabricator.random(), subject = rosettaStoneTemplate1, predicate = shProperty, `object` = templateProperty1))
            saveStatement(createStatement(id = fabricator.random(), subject = rosettaStoneTemplate1, predicate = shProperty, `object` = templateProperty2))
            saveStatement(createStatement(id = fabricator.random(), subject = rosettaStoneTemplate1, predicate = shProperty, `object` = templateProperty3))

            saveStatement(createStatement(id = fabricator.random(), subject = templateProperty1, predicate = placeholder, `object` = templateProperty1Placeholder))
            saveStatement(createStatement(id = fabricator.random(), subject = templateProperty2, predicate = placeholder, `object` = templateProperty2Placeholder))
            saveStatement(createStatement(id = fabricator.random(), subject = templateProperty3, predicate = placeholder, `object` = templateProperty3Placeholder))

            saveStatement(createStatement(id = fabricator.random(), subject = templateProperty1, predicate = shOrder, `object` = templateProperty1Order))
            saveStatement(createStatement(id = fabricator.random(), subject = templateProperty2, predicate = shOrder, `object` = templateProperty2Order))
            saveStatement(createStatement(id = fabricator.random(), subject = templateProperty3, predicate = shOrder, `object` = templateProperty3Order))

            // rosettaStoneStatement1

            rosettaStoneStatement1.requiredEntities(fabricator)
                .filter { it.id != rosettaStoneTemplate1.id }
                .forEach(saveThing)
            rosettaStoneStatementRepository.save(rosettaStoneStatement1)
        }

        // hasLabel
        // hasOtherResult
        // hasResult
        //  hasOtherResult
        //      hasResult
        //  hasResult
        //      hasOtherResult
        // rosettaStoneTemplate1
        //  subject
        //  object1
        //  object2
        val labeledComparisonPaths = listOf(
            LabeledComparisonPath(
                id = hasLabel.id,
                label = hasLabel.label,
                description = null,
                type = ComparisonPath.Type.PREDICATE,
                sources = 1,
                children = emptyList(),
            ),
            LabeledComparisonPath(
                id = hasOtherResult.id,
                label = hasOtherResult.label,
                description = hasOtherResultDescription.label,
                type = ComparisonPath.Type.PREDICATE,
                sources = 2,
                children = emptyList(),
            ),
            LabeledComparisonPath(
                id = hasResult.id,
                label = hasResult.label,
                description = null,
                type = ComparisonPath.Type.PREDICATE,
                sources = 1,
                children = listOf(
                    LabeledComparisonPath(
                        id = hasOtherResult.id,
                        label = hasOtherResult.label,
                        description = hasOtherResultDescription.label,
                        type = ComparisonPath.Type.PREDICATE,
                        sources = 1,
                        children = listOf(
                            LabeledComparisonPath(
                                id = hasResult.id,
                                label = hasResult.label,
                                description = null,
                                type = ComparisonPath.Type.PREDICATE,
                                sources = 1,
                                children = emptyList(),
                            ),
                        ),
                    ),
                    LabeledComparisonPath(
                        id = hasResult.id,
                        label = hasResult.label,
                        description = null,
                        type = ComparisonPath.Type.PREDICATE,
                        sources = 1,
                        children = listOf(
                            LabeledComparisonPath(
                                id = hasOtherResult.id,
                                label = hasOtherResult.label,
                                description = hasOtherResultDescription.label,
                                type = ComparisonPath.Type.PREDICATE,
                                sources = 1,
                                children = emptyList(),
                            ),
                        ),
                    ),
                ),
            ),
            LabeledComparisonPath(
                id = rosettaStoneTemplate1.id,
                label = rosettaStoneTemplate1.label,
                description = rosettaStoneTemplate1Description.label,
                type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT,
                sources = 1,
                children = listOf(
                    LabeledComparisonPath(
                        id = ThingId("hasSubjectPosition"),
                        label = templateProperty1Placeholder.label,
                        description = null,
                        type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE,
                        sources = 1,
                        children = emptyList(),
                    ),
                    LabeledComparisonPath(
                        id = ThingId("hasObjectPosition1"),
                        label = templateProperty2Placeholder.label,
                        description = templateProperty2Description.label,
                        type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE,
                        sources = 1,
                        children = emptyList(),
                    ),
                    LabeledComparisonPath(
                        id = ThingId("hasObjectPosition2"),
                        label = templateProperty3Placeholder.label,
                        description = null,
                        type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE,
                        sources = 1,
                        children = emptyList(),
                    ),
                ),
            ),
        )

        context("by comparison id") {
            createGraph()

            val expected = labeledComparisonPaths
            val result = comparisonAuxiliaryRepository.findAllLabeledComparisonPathsByComparisonId(comparison.id, 4)

            it("returns the correct result") {
                result shouldBe expected
            }
        }
        context("by simple comparison paths") {
            context("when all paths exists in the graph") {
                createGraph()

                val simplePaths = labeledComparisonPaths.toSimpleComparisonPaths()
                val expected = labeledComparisonPaths.withoutSources()
                val result = comparisonAuxiliaryRepository.findAllLabeledComparisonPathsBySimpleComparionPaths(simplePaths)

                it("returns the correct result") {
                    result shouldBe expected
                }
            }
            context("when some path do not exist in the graph") {
                createGraph()

                val simplePaths = labeledComparisonPaths.toSimpleComparisonPaths() + listOf(
                    SimpleComparisonPath(
                        id = ThingId("Missing"),
                        type = ComparisonPath.Type.PREDICATE,
                        children = emptyList(),
                    ),
                    SimpleComparisonPath(
                        id = ThingId("MissingTemplate"),
                        type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT,
                        children = emptyList(),
                    ),
                )
                val expected = labeledComparisonPaths.withoutSources()
                val result = comparisonAuxiliaryRepository.findAllLabeledComparisonPathsBySimpleComparionPaths(simplePaths)

                it("returns the correct result") {
                    result shouldBe expected
                }
            }
        }
    }

    describe("finding comparison column data") {
        context("by root ids and paths") {
            val preprocessing = fabricator.random<Predicate>().copy(id = ThingId("preprocessing"))
            val description = fabricator.random<Predicate>().copy(id = Predicates.description)
            val hasNextSteps = fabricator.random<Predicate>().copy(id = ThingId("hasNextSteps"))
            val hasResult = fabricator.random<Predicate>().copy(id = ThingId("hasResult"))
            val hasContribution = fabricator.random<Predicate>().copy(id = Predicates.hasContribution)

            val paper = fabricator.random<Resource>().copy(id = ThingId("Paper"), classes = setOf(Classes.paper))
            val r1 = fabricator.random<Resource>().copy(id = ThingId("R1"), classes = setOf(Classes.contribution))
            val r2 = fabricator.random<Resource>().copy(
                id = ThingId("R2"),
                label = "first",
            )
            val r3 = fabricator.random<Resource>().copy(
                id = ThingId("R3"),
                label = "second",
            )
            val r4 = fabricator.random<Resource>().copy(id = ThingId("R4"))
            val r5 = fabricator.random<Resource>().copy(id = ThingId("R5"))
            val r6 = fabricator.random<Resource>().copy(id = ThingId("R6"))
            val r7 = fabricator.random<Resource>().copy(id = ThingId("R7"))
            val r8 = fabricator.random<Resource>().copy(id = ThingId("R8"))

            val l1 = fabricator.random<Literal>().copy(id = ThingId("L1"))
            val l2 = fabricator.random<Literal>().copy(id = ThingId("L2"))
            val l3 = fabricator.random<Literal>().copy(id = ThingId("L3"))
            val l4 = fabricator.random<Literal>().copy(id = ThingId("L4"))

            val rootIds = listOf(r1.id, r4.id, r8.id)
            val paths = listOf(
                SimpleComparisonPath(
                    id = preprocessing.id,
                    type = ComparisonPath.Type.PREDICATE,
                    children = listOf(
                        SimpleComparisonPath(
                            id = hasNextSteps.id,
                            type = ComparisonPath.Type.PREDICATE,
                            children = listOf(
                                SimpleComparisonPath(
                                    id = hasResult.id,
                                    type = ComparisonPath.Type.PREDICATE,
                                    children = emptyList(),
                                ),
                            ),
                        ),
                        SimpleComparisonPath(
                            id = description.id,
                            type = ComparisonPath.Type.PREDICATE,
                            children = emptyList(),
                        ),
                    ),
                ),
            )
            val createGraph = {
                // paper
                //  R1
                //      preprocessing
                //          R2
                //              description
                //                  L1
                //          R3
                //              description
                //                  L2
                // R4
                //  preprocessing
                //      R5
                //          hasNextSteps
                //              R6
                //                  hasResult
                //                      R7
                //          description
                //              L3
                // R8
                //  description
                //      L4

                // Paper

                saveStatement(createStatement(id = fabricator.random(), subject = paper, predicate = hasContribution, `object` = r1))

                // R1

                saveStatement(createStatement(id = fabricator.random(), subject = r1, predicate = preprocessing, `object` = r2))
                saveStatement(createStatement(id = fabricator.random(), subject = r2, predicate = description, `object` = l1))
                saveStatement(createStatement(id = fabricator.random(), subject = r1, predicate = preprocessing, `object` = r3))
                saveStatement(createStatement(id = fabricator.random(), subject = r3, predicate = description, `object` = l2))

                // R4

                saveStatement(createStatement(id = fabricator.random(), subject = r4, predicate = preprocessing, `object` = r5))
                saveStatement(createStatement(id = fabricator.random(), subject = r5, predicate = hasNextSteps, `object` = r6))
                saveStatement(createStatement(id = fabricator.random(), subject = r6, predicate = hasResult, `object` = r7))
                saveStatement(createStatement(id = fabricator.random(), subject = r5, predicate = description, `object` = l3))

                // R8

                saveStatement(createStatement(id = fabricator.random(), subject = r8, predicate = description, `object` = l4))
            }

            context("when parameters are not empty") {
                createGraph()
                val expected = mapOf(
                    r1.id to ComparisonColumnData(
                        title = paper,
                        subtitle = r1,
                        values = mapOf(
                            preprocessing.id to listOf(
                                ComparisonTableValue(
                                    value = r2,
                                    children = mapOf(
                                        description.id to listOf(
                                            ComparisonTableValue(
                                                value = l1,
                                                children = emptyMap(),
                                            ),
                                        ),
                                    ),
                                ),
                                ComparisonTableValue(
                                    value = r3,
                                    children = mapOf(
                                        description.id to listOf(
                                            ComparisonTableValue(
                                                value = l2,
                                                children = emptyMap(),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    r4.id to ComparisonColumnData(
                        title = r4,
                        subtitle = null,
                        values = mapOf(
                            preprocessing.id to listOf(
                                ComparisonTableValue(
                                    value = r5,
                                    children = mapOf(
                                        hasNextSteps.id to listOf(
                                            ComparisonTableValue(
                                                value = r6,
                                                children = mapOf(
                                                    hasResult.id to listOf(
                                                        ComparisonTableValue(
                                                            value = r7,
                                                            children = emptyMap(),
                                                        ),
                                                    ),
                                                ),
                                            ),
                                        ),
                                        description.id to listOf(
                                            ComparisonTableValue(
                                                value = l3,
                                                children = emptyMap(),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    r8.id to ComparisonColumnData(
                        title = r8,
                        subtitle = null,
                        values = emptyMap(),
                    ),
                )

                val result = comparisonAuxiliaryRepository.findComparisonColumnDataByRootIdsAndPaths(rootIds, paths)

                it("returns the correct result") {
                    result shouldBe expected
                }
            }
            context("when root ids are empty") {
                createGraph()
                val expected = emptyMap<ThingId, ComparisonColumnData>()
                val result = comparisonAuxiliaryRepository.findComparisonColumnDataByRootIdsAndPaths(emptyList(), paths)

                it("returns empty result") {
                    result shouldBe expected
                }
            }
            context("when paths are empty") {
                createGraph()
                val expected = mapOf(
                    r1.id to ComparisonColumnData(paper, r1, emptyMap()),
                    r4.id to ComparisonColumnData(r4, null, emptyMap()),
                    r8.id to ComparisonColumnData(r8, null, emptyMap()),
                )
                val result = comparisonAuxiliaryRepository.findComparisonColumnDataByRootIdsAndPaths(rootIds, emptyList())

                it("returns table headers") {
                    result shouldBe expected
                }
            }
        }
    }
}
