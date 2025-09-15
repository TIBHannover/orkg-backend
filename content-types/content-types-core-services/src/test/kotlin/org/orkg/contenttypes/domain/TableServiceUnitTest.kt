package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import org.springframework.data.domain.Sort
import java.util.Optional
import java.util.UUID

internal class TableServiceUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val thingRepository: ThingRepository = mockk()
    private val classRepository: ClassRepository = mockk()
    private val unsafeClassUseCases: UnsafeClassUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val unsafePredicateUseCases: UnsafePredicateUseCases = mockk()
    private val listService: ListUseCases = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val statementUseCases: StatementUseCases = mockk()

    private val service = TableService(
        resourceRepository,
        statementRepository,
        thingRepository,
        classRepository,
        unsafeClassUseCases,
        unsafeResourceUseCases,
        unsafeStatementUseCases,
        unsafeLiteralUseCases,
        unsafePredicateUseCases,
        listService,
        observatoryRepository,
        organizationRepository,
        statementUseCases,
    )

    @Test
    fun `Given a table exists, when fetching it by id, then it is returned`() {
        //       | Column 0 | Column 1 |
        // ----- | -------- | -------- |
        // Row 0 | -        | L1       |
        // Row 1 | R1       | C1       |

        val expected = createResource(
            classes = setOf(Classes.table),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val row0 = createResource(
            id = ThingId("row0"),
            classes = setOf(Classes.row),
        )
        val row0Data = listOf(
            null,
            createLiteral()
        )
        val row1 = createResource(
            id = ThingId("row1"),
            classes = setOf(Classes.row),
        )
        val row1Data = listOf(
            createResource(),
            createClass()
        )
        val bundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 3,
            blacklist = emptyList(),
            whitelist = emptyList()
        )
        val column0 = createResource(
            id = ThingId("column0"),
            classes = setOf(Classes.column),
        )
        val column1 = createResource(
            id = ThingId("column1"),
            classes = setOf(Classes.column),
        )
        val cell00 = createResource(id = ThingId("cell00"), classes = setOf(Classes.cell))
        val cell10 = createResource(id = ThingId("cell10"), classes = setOf(Classes.cell))
        val cell01 = createResource(id = ThingId("cell01"), classes = setOf(Classes.cell))
        val cell11 = createResource(id = ThingId("cell11"), classes = setOf(Classes.cell))

        every { resourceRepository.findById(expected.id) } returns Optional.of(expected)
        every {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = bundleConfiguration,
                sort = Sort.unsorted()
            )
        } returns pageOf(
            // declare columns
            // declare column 0
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.csvwColumns),
                `object` = column0
            ),
            createStatement(
                subject = column0,
                predicate = createPredicate(Predicates.csvwNumber),
                `object` = createLiteral(label = "1", datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = column0,
                predicate = createPredicate(Predicates.csvwTitles),
                `object` = createLiteral(label = "Column 0")
            ),
            // declare column 1
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.csvwColumns),
                `object` = column1
            ),
            createStatement(
                subject = column1,
                predicate = createPredicate(Predicates.csvwNumber),
                `object` = createLiteral(label = "2", datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = column1,
                predicate = createPredicate(Predicates.csvwTitles),
                `object` = createLiteral(label = "Column 1")
            ),
            // declare rows
            // declare row 0
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.csvwRows),
                `object` = row0
            ),
            createStatement(
                subject = row0,
                predicate = createPredicate(Predicates.csvwNumber),
                `object` = createLiteral(label = "1", datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = row0,
                predicate = createPredicate(Predicates.csvwTitles),
                `object` = createLiteral(label = "Row 0")
            ),
            // declare row 1
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.csvwRows),
                `object` = row1
            ),
            createStatement(
                subject = row1,
                predicate = createPredicate(Predicates.csvwNumber),
                `object` = createLiteral(label = "2", datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = row1,
                predicate = createPredicate(Predicates.csvwTitles),
                `object` = createLiteral(label = "Row 1")
            ),
            // declare cell contents
            // declare cell 00
            createStatement(
                subject = row0,
                predicate = createPredicate(Predicates.csvwCells),
                `object` = cell00
            ),
            createStatement(
                subject = cell00,
                predicate = createPredicate(Predicates.csvwColumn),
                `object` = column0
            ),
            // no data
            // declare cell 01
            createStatement(
                subject = row0,
                predicate = createPredicate(Predicates.csvwCells),
                `object` = cell01
            ),
            createStatement(
                subject = cell01,
                predicate = createPredicate(Predicates.csvwColumn),
                `object` = column1
            ),
            createStatement(
                subject = cell01,
                predicate = createPredicate(Predicates.csvwValue),
                `object` = row0Data[1]!!
            ),
            // declare cell 10
            createStatement(
                subject = row1,
                predicate = createPredicate(Predicates.csvwCells),
                `object` = cell10
            ),
            createStatement(
                subject = cell10,
                predicate = createPredicate(Predicates.csvwColumn),
                `object` = column0
            ),
            createStatement(
                subject = cell10,
                predicate = createPredicate(Predicates.csvwValue),
                `object` = row1Data[0]
            ),
            // declare cell 11
            createStatement(
                subject = row1,
                predicate = createPredicate(Predicates.csvwCells),
                `object` = cell11
            ),
            createStatement(
                subject = cell11,
                predicate = createPredicate(Predicates.csvwColumn),
                `object` = column1
            ),
            createStatement(
                subject = cell11,
                predicate = createPredicate(Predicates.csvwValue),
                `object` = row1Data[1]
            ),
        )

        val actual = service.findById(expected.id)

        actual.isPresent shouldBe true
        actual.get() shouldNotBe null
        actual.get().asClue { table ->
            table.id shouldBe expected.id
            table.label shouldBe expected.label
            table.rows.asClue { rows ->
                rows.size shouldBe 3
                rows[0].asClue { header ->
                    header.label shouldBe null
                    header.data.size shouldBe 2
                    header.data[0] shouldBe createLiteral(label = "Column 0")
                    header.data[1] shouldBe createLiteral(label = "Column 1")
                }
                rows[1].asClue { row0 ->
                    row0.label shouldBe "Row 0"
                    row0.data.size shouldBe 2
                    row0.data[0] shouldBe row0Data[0]
                    row0.data[1] shouldBe row0Data[1]
                }
                rows[2].asClue { row1 ->
                    row1.label shouldBe "Row 1"
                    row1.data.size shouldBe 2
                    row1.data[0] shouldBe row1Data[0]
                    row1.data[1] shouldBe row1Data[1]
                }
            }
            table.observatories shouldBe setOf(expected.observatoryId)
            table.organizations shouldBe setOf(expected.organizationId)
            table.extractionMethod shouldBe expected.extractionMethod
            table.createdAt shouldBe expected.createdAt
            table.createdBy shouldBe expected.createdBy
            table.visibility shouldBe Visibility.DEFAULT
            table.unlistedBy shouldBe expected.unlistedBy
        }

        verify(exactly = 1) { resourceRepository.findById(expected.id) }
        verify(exactly = 1) {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = bundleConfiguration,
                sort = Sort.unsorted()
            )
        }
    }
}
