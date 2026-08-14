package org.orkg.contenttypes.domain.actions

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.thing
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import java.util.UUID

internal class AbstractGraphDeleterUnitTest : MockkBaseTest {
    private val statementRepository: StatementRepository = mockk()
    private val resourceUseCases: ResourceUseCases = mockk()
    private val predicateUseCases: PredicateUseCases = mockk()

    private val abstractGraphDeleter = AbstractGraphDeleter(statementRepository, resourceUseCases, predicateUseCases)

    @Test
    fun `Given a graph spec, when deleting, it matches and deletes the graph correctly`() {
        val spec = thing {
            classId(Classes.list)
            statementTo {
                predicateId(Predicates.hasListElement)
            }
        }
        //        literal
        //           ↑
        // class <- list -> resource
        //           ↓
        //       predicate
        val contributorId = ContributorId(UUID.randomUUID())
        val list = createResource(id = ThingId("R123"), classes = setOf(Classes.list))
        val statementId1 = StatementId(1)
        val statementId2 = StatementId(2)
        val statementId3 = StatementId(3)
        val statementId4 = StatementId(4)
        val resource = createResource()
        val predicate = createPredicate()
        val `class` = createClass()
        val literal = createLiteral()
        val statement1 = createStatement(
            id = statementId1,
            subject = list,
            predicate = createPredicate(Predicates.hasListElement),
            `object` = resource,
        )
        val statement2 = createStatement(
            id = statementId2,
            subject = list,
            predicate = createPredicate(Predicates.hasListElement),
            `object` = predicate,
        )
        val statement3 = createStatement(
            id = statementId3,
            subject = list,
            predicate = createPredicate(Predicates.hasListElement),
            `object` = `class`,
        )
        val statement4 = createStatement(
            id = statementId4,
            subject = list,
            predicate = createPredicate(Predicates.hasListElement),
            `object` = literal,
        )
        val statements = listOf(statement1, statement2, statement3, statement4)

        every {
            statementRepository.findAll(
                objectId = list.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf()
        every {
            statementRepository.findAll(
                objectId = resource.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf(statement1)
        every {
            statementRepository.findAll(
                objectId = predicate.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf(statement2)
        every {
            statementRepository.findAll(
                objectId = `class`.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf(statement3)
        every {
            statementRepository.findAll(
                objectId = literal.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf(statement4)

        every { statementRepository.deleteByStatementIds(setOf(statementId1, statementId2, statementId3, statementId4)) } just runs
        every { resourceUseCases.delete(any(), contributorId) } just runs
        every { predicateUseCases.delete(any(), contributorId) } just runs

        abstractGraphDeleter.delete(spec, list, statements, contributorId)

        verify(exactly = 5) {
            statementRepository.findAll(
                objectId = any(),
                pageable = PageRequests.ALL,
            )
        }
        verify(exactly = 1) { statementRepository.deleteByStatementIds(setOf(statementId1, statementId2, statementId3, statementId4)) }
        verify(exactly = 2) { resourceUseCases.delete(any(), contributorId) }
        verify(exactly = 1) { predicateUseCases.delete(any(), contributorId) }
    }

    @Test
    fun `Given a graph spec, when matching the deletion subgraph, it matches outgoing statements correctly`() {
        val spec = thing {
            classId(Classes.list)
            statementTo {
                predicateId(Predicates.hasListElement)
                `object` { literal() }
            }
        }
        // list -> authorLiteral
        val list = createResource(id = ThingId("R123"), classes = setOf(Classes.list))
        val authorLiteral = createLiteral()
        val statementId = StatementId(1)
        val statements = listOf(
            createStatement(
                id = statementId,
                subject = list,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = authorLiteral,
            ),
        )

        every {
            statementRepository.findAll(
                objectId = list.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf()
        every {
            statementRepository.findAll(
                objectId = authorLiteral.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf(statements)

        val result = abstractGraphDeleter.matchGraph(spec, list, statements)
        result shouldBe AbstractGraphDeleter.MatchResult(
            thingsToDelete = setOf(list.id, authorLiteral.id),
            statementsToDelete = setOf(statementId),
        )

        verify(exactly = 2) {
            statementRepository.findAll(
                objectId = any(),
                pageable = PageRequests.ALL,
            )
        }
    }

    @Test
    fun `Given a graph spec, when matching the deletion subgraph, it matches incoming statements correctly`() {
        val spec = thing {
            classId(Classes.author)
            statementFrom {
                predicateId(Predicates.hasListElement)
                subject {
                    classId(Classes.list)
                }
            }
        }
        // list -> author
        val list = createResource(id = ThingId("R123"), classes = setOf(Classes.list))
        val author = createResource(id = ThingId("R456"), classes = setOf(Classes.author))
        val statementId = StatementId(1)
        val statements = listOf(
            createStatement(
                id = statementId,
                subject = list,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = author,
            ),
        )

        every {
            statementRepository.findAll(
                objectId = author.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf(statements)
        every {
            statementRepository.findAll(
                objectId = list.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf()

        val result = abstractGraphDeleter.matchGraph(spec, author, statements)
        result shouldBe AbstractGraphDeleter.MatchResult(
            thingsToDelete = setOf(list.id, author.id),
            statementsToDelete = setOf(statementId),
        )

        verify(exactly = 2) {
            statementRepository.findAll(
                objectId = any(),
                pageable = PageRequests.ALL,
            )
        }
    }

    @Test
    fun `Given a graph spec, when matching the deletion subgraph, it does not match things with unrelated incoming statements`() {
        val spec = thing {
            classId(Classes.list)
        }
        val author = createResource(id = ThingId("R456"), classes = setOf(Classes.author))
        val list = createResource(id = ThingId("R123"), classes = setOf(Classes.list))
        val statementId = StatementId(1)
        // author -> list
        val statements = emptyList<GeneralStatement>()

        every {
            statementRepository.findAll(
                objectId = any(),
                pageable = PageRequests.ALL,
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = author,
                predicate = createPredicate(Predicates.hasContent),
                `object` = list,
            ),
        )

        val result = abstractGraphDeleter.matchGraph(spec, list, statements)
        result shouldBe AbstractGraphDeleter.MatchResult(
            thingsToDelete = emptySet(),
            statementsToDelete = emptySet(),
        )

        verify(exactly = 1) {
            statementRepository.findAll(
                objectId = any(),
                pageable = PageRequests.ALL,
            )
        }
    }

    @Test
    fun `Given a graph spec, when matching the deletion subgraph, it does not run into infinite loops`() {
        val spec = thing {
            classId(Classes.list)
            statementTo {
                predicateId(Predicates.hasListElement)
                `object` {
                    classId(Classes.list)
                    statementFrom {
                        predicateId(Predicates.hasListElement)
                        subject {
                            classId(Classes.list)
                        }
                    }
                }
            }
        }
        // list -> list2
        val list1 = createResource(id = ThingId("R123"), classes = setOf(Classes.list))
        val list2 = createResource(id = ThingId("R456"), classes = setOf(Classes.list))
        val statementId = StatementId(1)
        val statements = listOf(
            createStatement(
                id = statementId,
                subject = list1,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = list2,
            ),
        )

        every {
            statementRepository.findAll(
                objectId = list1.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf()
        every {
            statementRepository.findAll(
                objectId = list2.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf(statements)

        val result = abstractGraphDeleter.matchGraph(spec, list1, statements)
        result shouldBe AbstractGraphDeleter.MatchResult(
            thingsToDelete = setOf(list1.id, list2.id),
            statementsToDelete = setOf(statementId),
        )

        verify(exactly = 2) {
            statementRepository.findAll(
                objectId = any(),
                pageable = PageRequests.ALL,
            )
        }
    }

    @Test
    fun `Given a graph spec, when matching the deletion subgraph, it does not consider internally connected statements as shared`() {
        val spec = thing {
            classId(Classes.list)
            statementTo {
                predicateId(Predicates.hasListElement)
                `object` { literal() }
            }
            statementFrom {
                predicateId(Predicates.hasAuthors)
                subject {
                    classId(Classes.comparison)
                }
            }
        }
        // list <-> comparison
        //    ↓     ↓
        //    author
        val list = createResource(id = ThingId("R123"), classes = setOf(Classes.list))
        val author = createLiteral()
        val comparison = createResource(id = ThingId("R789"), classes = setOf(Classes.comparison))
        val statement1 = createStatement(
            id = StatementId(1),
            subject = list,
            predicate = createPredicate(Predicates.hasListElement),
            `object` = author,
        )
        val statement2 = createStatement(
            id = StatementId(2),
            subject = comparison,
            predicate = createPredicate(Predicates.hasAuthors),
            `object` = list,
        )
        val statement3 = createStatement(
            id = StatementId(3),
            subject = list,
            `object` = comparison,
        )
        val statement4 = createStatement(
            id = StatementId(4),
            subject = comparison,
            `object` = author,
        )
        val statements = listOf(statement1, statement2, statement3, statement4)

        every {
            statementRepository.findAll(
                objectId = list.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf(statement2)
        every {
            statementRepository.findAll(
                objectId = author.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf(statement1, statement4)
        every {
            statementRepository.findAll(
                objectId = comparison.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf(statement3)

        val result = abstractGraphDeleter.matchGraph(spec, list, statements)
        result shouldBe AbstractGraphDeleter.MatchResult(
            thingsToDelete = setOf(list.id, author.id, comparison.id),
            statementsToDelete = setOf(statement1.id, statement2.id, statement3.id, statement4.id),
        )

        verify(exactly = 3) {
            statementRepository.findAll(
                objectId = any(),
                pageable = PageRequests.ALL,
            )
        }
    }

    @Test
    fun `Given a graph spec, when matching the deletion subgraph, it does not match indirectly shared statements`() {
        val spec = thing {
            classId(Classes.list)
            statementFrom {
                predicateId(Predicates.hasAuthors)
                subject {
                    classId(Classes.comparison)
                }
            }
        }
        // list <- comparison <- paper
        val list = createResource(id = ThingId("R123"), classes = setOf(Classes.list))
        val comparison = createResource(id = ThingId("R456"), classes = setOf(Classes.comparison))
        val paper = createResource(id = ThingId("R789"), classes = setOf(Classes.paper))
        val statement1 = createStatement(
            id = StatementId(1),
            subject = comparison,
            predicate = createPredicate(Predicates.hasAuthors),
            `object` = list,
        )
        val statement2 = createStatement(
            id = StatementId(2),
            subject = paper,
            predicate = createPredicate(Predicates.hasContent),
            `object` = comparison,
        )
        val statements = listOf(statement1, statement2)

        every {
            statementRepository.findAll(
                objectId = comparison.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf(statement2)
        every {
            statementRepository.findAll(
                objectId = list.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf(statement1)

        val result = abstractGraphDeleter.matchGraph(spec, list, statements)
        result shouldBe AbstractGraphDeleter.MatchResult(
            thingsToDelete = setOf(),
            statementsToDelete = setOf(),
        )

        verify(exactly = 2) {
            statementRepository.findAll(
                objectId = any(),
                pageable = PageRequests.ALL,
            )
        }
    }

    @Test
    fun `Given a graph spec, when matching the deletion subgraph, it matches first level unrelated outgoing statements`() {
        val spec = thing {
            classId(Classes.list)
            statementTo {
                predicateId(Predicates.hasListElement)
                `object` { literal() }
            }
        }
        // list -> author
        //   ↓
        // comparison -> contributor
        val list = createResource(id = ThingId("R123"), classes = setOf(Classes.list))
        val author = createLiteral()
        val comparison = createResource(id = ThingId("R789"), classes = setOf(Classes.comparison))
        val contribution = createResource(id = ThingId("R159"), classes = setOf(Classes.contribution))
        val statement1 = createStatement(
            id = StatementId(1),
            subject = list,
            predicate = createPredicate(Predicates.hasListElement),
            `object` = author,
        )
        val statement2 = createStatement(
            id = StatementId(3),
            subject = list,
            `object` = comparison,
        )
        val statement3 = createStatement(
            id = StatementId(3),
            subject = comparison,
            `object` = contribution,
        )
        val statements = listOf(statement1, statement2, statement3)

        every {
            statementRepository.findAll(
                objectId = list.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf()
        every {
            statementRepository.findAll(
                objectId = author.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf(statement1)

        val result = abstractGraphDeleter.matchGraph(spec, list, statements)
        result shouldBe AbstractGraphDeleter.MatchResult(
            thingsToDelete = setOf(list.id, author.id),
            statementsToDelete = setOf(statement1.id, statement2.id),
        )

        verify(exactly = 2) {
            statementRepository.findAll(
                objectId = any(),
                pageable = PageRequests.ALL,
            )
        }
    }

    @Test
    fun `Given a graph spec, when matching the deletion subgraph, it ignores things and their subgraphs marked as shared`() {
        val spec = thing {
            classId(Classes.list)
            statementTo {
                predicateId(Predicates.hasListElement)
                `object` {
                    classId(Classes.author)
                    shared()
                }
            }
        }
        // list -> author -> doi
        val list = createResource(id = ThingId("R123"), classes = setOf(Classes.list))
        val author = createResource(id = ThingId("R456"), classes = setOf(Classes.author))
        val doi = createLiteral()
        val statement1 = createStatement(
            id = StatementId(1),
            subject = list,
            predicate = createPredicate(Predicates.hasListElement),
            `object` = author,
        )
        val statement2 = createStatement(
            id = StatementId(2),
            subject = author,
            `object` = doi,
        )
        val statements = listOf(statement1, statement2)

        every {
            statementRepository.findAll(
                objectId = list.id,
                pageable = PageRequests.ALL,
            )
        } returns pageOf()

        val result = abstractGraphDeleter.matchGraph(spec, list, statements)
        result shouldBe AbstractGraphDeleter.MatchResult(
            thingsToDelete = setOf(list.id),
            statementsToDelete = setOf(statement1.id),
        )

        verify(exactly = 1) {
            statementRepository.findAll(
                objectId = any(),
                pageable = PageRequests.ALL,
            )
        }
    }
}
