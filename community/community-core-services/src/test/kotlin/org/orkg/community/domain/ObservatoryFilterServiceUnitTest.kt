package org.orkg.community.domain

import org.orkg.community.output.ObservatoryFilterRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.community.input.CreateObservatoryFilterUseCase
import org.orkg.community.input.UpdateObservatoryFilterUseCase
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.testing.fixtures.createObservatory
import org.orkg.community.testing.fixtures.createObservatoryFilter
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate

class ObservatoryFilterServiceUnitTest {

    private val repository: ObservatoryFilterRepository = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val predicateRepository: PredicateRepository = mockk()
    private val classRepository: ClassRepository = mockk()
    private val fixedTime = OffsetDateTime.of(2023, 8, 29, 13, 37, 35, 12345, ZoneOffset.ofHours(1))
    private val staticClock = Clock.fixed(Instant.from(fixedTime), ZoneId.systemDefault())

    private val service = ObservatoryFilterService(
        repository, observatoryRepository, predicateRepository, classRepository, staticClock
    )

    @Test
    fun `Given an observatory filter is created, when no id is given, it gets an id from the repository`() {
        val command = CreateObservatoryFilterUseCase.CreateCommand(
            id = null,
            observatoryId = ObservatoryId(UUID.randomUUID()),
            label = "label",
            contributorId = ContributorId(UUID.randomUUID()),
            path = listOf(Predicates.hasResearchProblem),
            range = Classes.resources,
            exact = false,
            featured = false
        )
        val id = ObservatoryFilterId(UUID.randomUUID())
        val observatory = createObservatory(id = command.observatoryId)
        val range = createClass(command.range)
        val path = createPredicate(command.path[0])

        every { repository.nextIdentity() } returns id
        every { observatoryRepository.findById(command.observatoryId) } returns Optional.of(observatory)
        every { classRepository.findById(command.range) } returns Optional.of(range)
        every { predicateRepository.findById(command.path[0]) } returns Optional.of(path)
        every { repository.save(any()) } just runs

        service.create(command)

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) { observatoryRepository.findById(command.observatoryId) }
        verify(exactly = 1) { classRepository.findById(command.range) }
        verify(exactly = 1) { predicateRepository.findById(command.path[0]) }
        verify(exactly = 1) {
            repository.save(withArg {
                it.id shouldBe id
                it.observatoryId shouldBe command.observatoryId
                it.createdAt shouldBe LocalDateTime.now(staticClock)
                it.createdBy shouldBe command.contributorId
                it.path shouldBe command.path
                it.label shouldBe command.label
                it.range shouldBe command.range
                it.exact shouldBe command.exact
                it.featured shouldBe command.featured
            })
        }
    }

    @Test
    fun `Given an observatory filter is created, when an id is given, it does not get a new id from the repository`() {
        val command = CreateObservatoryFilterUseCase.CreateCommand(
            id = ObservatoryFilterId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID()),
            label = "label",
            contributorId = ContributorId(UUID.randomUUID()),
            path = listOf(Predicates.hasResearchProblem),
            range = Classes.resources,
            exact = false,
            featured = false
        )
        val observatory = createObservatory(id = command.observatoryId)
        val range = createClass(command.range)
        val path = createPredicate(command.path[0])

        every { observatoryRepository.findById(command.observatoryId) } returns Optional.of(observatory)
        every { classRepository.findById(command.range) } returns Optional.of(range)
        every { predicateRepository.findById(command.path[0]) } returns Optional.of(path)
        every { repository.findById(command.id!!) } returns Optional.empty()
        every { repository.save(any()) } just runs

        service.create(command) shouldBe command.id

        verify(exactly = 1) { observatoryRepository.findById(command.observatoryId) }
        verify(exactly = 1) { classRepository.findById(command.range) }
        verify(exactly = 1) { predicateRepository.findById(command.path[0]) }
        verify(exactly = 1) { repository.findById(command.id!!) }
        verify(exactly = 1) {
            repository.save(withArg {
                it.id shouldBe command.id
                it.observatoryId shouldBe command.observatoryId
                it.createdAt shouldBe LocalDateTime.now(staticClock)
                it.createdBy shouldBe command.contributorId
                it.path shouldBe command.path
                it.label shouldBe command.label
                it.range shouldBe command.range
                it.exact shouldBe command.exact
                it.featured shouldBe command.featured
            })
        }
    }

    @Test
    fun `Given an observatory filter is created, when observatory does not exist, it throws an exception`() {
        val command = CreateObservatoryFilterUseCase.CreateCommand(
            id = ObservatoryFilterId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID()),
            label = "label",
            contributorId = ContributorId(UUID.randomUUID()),
            path = listOf(Predicates.hasResearchProblem),
            range = Classes.resources,
            exact = false,
            featured = false
        )

        every { observatoryRepository.findById(command.observatoryId) } returns Optional.empty()

        shouldThrow<ObservatoryNotFound> { service.create(command) }

        verify(exactly = 1) { observatoryRepository.findById(command.observatoryId) }
    }

    @Test
    fun `Given an observatory filter is created, when range class does not exist, it throws an exception`() {
        val command = CreateObservatoryFilterUseCase.CreateCommand(
            observatoryId = ObservatoryId(UUID.randomUUID()),
            label = "label",
            contributorId = ContributorId(UUID.randomUUID()),
            path = listOf(Predicates.hasResearchProblem),
            range = ThingId("Missing"),
            exact = false,
            featured = false
        )
        val observatory = createObservatory(id = command.observatoryId)

        every { observatoryRepository.findById(command.observatoryId) } returns Optional.of(observatory)
        every { classRepository.findById(command.range) } returns Optional.empty()

        shouldThrow<ClassNotFound> { service.create(command) }

        verify(exactly = 1) { observatoryRepository.findById(command.observatoryId) }
        verify(exactly = 1) { classRepository.findById(command.range) }
    }

    @Test
    fun `Given an observatory filter is created, when path is invalid, it throws an exception`() {
        val command = CreateObservatoryFilterUseCase.CreateCommand(
            observatoryId = ObservatoryId(UUID.randomUUID()),
            label = "label",
            contributorId = ContributorId(UUID.randomUUID()),
            path = listOf(Predicates.hasResearchProblem),
            range = Classes.resources,
            exact = false,
            featured = false
        )
        val observatory = createObservatory(id = command.observatoryId)
        val range = createClass(command.range)

        every { observatoryRepository.findById(command.observatoryId) } returns Optional.of(observatory)
        every { classRepository.findById(command.range) } returns Optional.of(range)
        every { predicateRepository.findById(command.path[0]) } returns Optional.empty()

        shouldThrow<PredicateNotFound> { service.create(command) }

        verify(exactly = 1) { observatoryRepository.findById(command.observatoryId) }
        verify(exactly = 1) { classRepository.findById(command.range) }
        verify(exactly = 1) { predicateRepository.findById(command.path[0]) }
    }

    @Test
    fun `Given an observatory filter is created, when observatory filter with id already exists, it throws an exception`() {
        val command = CreateObservatoryFilterUseCase.CreateCommand(
            id = ObservatoryFilterId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID()),
            label = "label",
            contributorId = ContributorId(UUID.randomUUID()),
            path = listOf(Predicates.hasResearchProblem),
            range = Classes.resources,
            exact = false,
            featured = false
        )
        val observatory = createObservatory(id = command.observatoryId)
        val range = createClass(command.range)
        val path = createPredicate(command.path[0])
        val filter = createObservatoryFilter()

        every { observatoryRepository.findById(command.observatoryId) } returns Optional.of(observatory)
        every { classRepository.findById(command.range) } returns Optional.of(range)
        every { predicateRepository.findById(command.path[0]) } returns Optional.of(path)
        every { repository.findById(command.id!!) } returns Optional.of(filter)

        shouldThrow<ObservatoryFilterAlreadyExists> { service.create(command) }

        verify(exactly = 1) { observatoryRepository.findById(command.observatoryId) }
        verify(exactly = 1) { classRepository.findById(command.range) }
        verify(exactly = 1) { predicateRepository.findById(command.path[0]) }
        verify(exactly = 1) { repository.findById(command.id!!) }
    }

    @Test
    fun `Given an observatory filter is updated, it returns success`() {
        val filter = createObservatoryFilter()
        val command = UpdateObservatoryFilterUseCase.UpdateCommand(
            id = filter.id,
            label = "label",
            path = listOf(Predicates.hasResearchProblem),
            range = Classes.resources,
            exact = true,
            featured = true
        )
        val range = createClass(command.range!!)
        val path = createPredicate(command.path!![0])

        every { repository.findById(command.id) } returns Optional.of(filter)
        every { classRepository.findById(command.range!!) } returns Optional.of(range)
        every { predicateRepository.findById(command.path!![0]) } returns Optional.of(path)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(command.id) }
        verify(exactly = 1) { classRepository.findById(command.range!!) }
        verify(exactly = 1) { predicateRepository.findById(command.path!![0]) }
        verify(exactly = 1) {
            repository.save(withArg {
                it.id shouldBe filter.id
                it.observatoryId shouldBe filter.observatoryId
                it.createdAt shouldBe filter.createdAt
                it.createdBy shouldBe filter.createdBy
                it.path shouldBe command.path
                it.label shouldBe command.label
                it.range shouldBe command.range
                it.exact shouldBe command.exact
                it.featured shouldBe command.featured
            })
        }
    }

    @Test
    fun `Given an observatory filter is updated, when observatory filter does not exist, it throws an exception`() {
        val filter = createObservatoryFilter()
        val command = UpdateObservatoryFilterUseCase.UpdateCommand(
            id = filter.id,
            label = "label",
            path = listOf(Predicates.hasResearchProblem),
            range = Classes.resources,
            exact = false,
            featured = false
        )

        every { repository.findById(command.id) } returns Optional.empty()

        shouldThrow<ObservatoryFilterNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(command.id) }
    }

    @Test
    fun `Given an observatory filter is updated, when range class does not exist, it throws an exception`() {
        val filter = createObservatoryFilter()
        val command = UpdateObservatoryFilterUseCase.UpdateCommand(
            id = filter.id,
            label = "label",
            path = listOf(Predicates.hasResearchProblem),
            range = Classes.resources,
            exact = false,
            featured = false
        )

        every { repository.findById(command.id) } returns Optional.of(filter)
        every { classRepository.findById(command.range!!) } returns Optional.empty()

        shouldThrow<ClassNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(command.id) }
        verify(exactly = 1) { classRepository.findById(command.range!!) }
    }

    @Test
    fun `Given an observatory filter is updated, when path is invalid, it throws an exception`() {
        val filter = createObservatoryFilter()
        val command = UpdateObservatoryFilterUseCase.UpdateCommand(
            id = filter.id,
            label = "label",
            path = listOf(Predicates.hasResearchProblem),
            range = Classes.resources,
            exact = false,
            featured = false
        )
        val range = createClass(command.range!!)

        every { repository.findById(command.id) } returns Optional.of(filter)
        every { classRepository.findById(command.range!!) } returns Optional.of(range)
        every { predicateRepository.findById(command.path!![0]) } returns Optional.empty()

        shouldThrow<PredicateNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(command.id) }
        verify(exactly = 1) { classRepository.findById(command.range!!) }
        verify(exactly = 1) { predicateRepository.findById(command.path!![0]) }
    }
}
