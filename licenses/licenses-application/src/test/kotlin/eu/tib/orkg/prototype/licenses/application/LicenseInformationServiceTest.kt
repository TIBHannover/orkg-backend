package eu.tib.orkg.prototype.licenses.application

import eu.tib.orkg.prototype.licenses.domain.LicenseInformation
import eu.tib.orkg.prototype.licenses.domain.LicenseInformationProvider
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

class LicenseInformationServiceTest {
    private val licenseProvider: LicenseInformationProvider = mockk {
        every { id } returns "dummy"
    }
    private val service = LicenseInformationService(listOf(licenseProvider))

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(licenseProvider)
    }

    @Test
    fun `returns the correct result when provider succeeds`() {
        val uri = URI.create("https://www.github.com/github/docs")
        val license = LicenseInformation("dummy", "MIT")

        every { licenseProvider.canProcess(uri) } returns true
        every { licenseProvider.determineLicense(uri) } returns license

        assertThat(service.determineLicense(uri)).isEqualTo(license)

        verify(exactly = 1) { licenseProvider.canProcess(uri) }
        verify(exactly = 1) { licenseProvider.determineLicense(uri) }
    }

    @Test
    fun `throws an exception when no matching provider could be found`() {
        val uri = URI.create("https://www.github.com/github/docs")

        every { licenseProvider.canProcess(uri) } returns false

        val exception = assertThrows<UnsupportedURI> {
            service.determineLicense(uri)
        }
        assertThat(exception.message).isEqualTo("""Unsupported URI "$uri".""")

        verify(exactly = 1) { licenseProvider.canProcess(uri) }
        verify(exactly = 0) { licenseProvider.determineLicense(uri) }
    }

    @Test
    fun `throws an exception when no license could be found`() {
        val uri = URI.create("https://www.github.com/github/docs")

        every { licenseProvider.canProcess(uri) } returns true
        every { licenseProvider.determineLicense(uri) } returns null

        val exception = assertThrows<LicenseNotFound> {
            service.determineLicense(uri)
        }
        assertThat(exception.message).isEqualTo("""License not found for URI "$uri".""")

        verify(exactly = 1) { licenseProvider.canProcess(uri) }
        verify(exactly = 1) { licenseProvider.determineLicense(uri) }
    }
}
