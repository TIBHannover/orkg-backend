package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.application.OnlyOneOrganizationAllowed
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreatePaperCommand
import eu.tib.orkg.prototype.community.testing.fixtures.createOrganization
import eu.tib.orkg.prototype.community.testing.fixtures.toOrganizationEntity
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrganizationValidatorUnitTest {
    private val organizationRepository: PostgresOrganizationRepository = mockk()

    private val organizationValidator = OrganizationValidator(organizationRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(organizationRepository)
    }

    @Test
    fun `Given a paper create command, when validating its organization, it returns success`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()
        val organization = createOrganization(id = command.organizations[0]).toOrganizationEntity()

        every { organizationRepository.findById(organization.id!!) } returns Optional.of(organization)

        val result = organizationValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }

        verify(exactly = 1) { organizationRepository.findById(organization.id!!) }
    }

    @Test
    fun `Given a paper create command, when organization is missing, it throws an exception`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()

        every { organizationRepository.findById(command.organizations[0].value) } returns Optional.empty()

        assertThrows<OrganizationNotFound> { organizationValidator(command, state) }

        verify(exactly = 1) { organizationRepository.findById(command.organizations[0].value) }
    }

    @Test
    fun `Given a paper create command, when more than one organization is specified, it throws an exception`() {
        val command = dummyCreatePaperCommand().copy(
            organizations = listOf(OrganizationId(UUID.randomUUID()), OrganizationId(UUID.randomUUID()))
        )
        val state = PaperState()

        assertThrows<OnlyOneOrganizationAllowed> { organizationValidator(command, state) }
    }
}
