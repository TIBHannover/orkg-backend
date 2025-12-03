package org.orkg.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SHA256Test {
    @Test
    fun `calculates the SHA256 of the empty string correctly`() {
        assertThat("".sha256).isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    }

    @Test
    fun `calculates the SHA256 of an example string correctly`() {
        assertThat("Hello, world!\n".sha256).isEqualTo("d9014c4624844aa5bac314773d6b689ad467fa4e1d1a50a1b8a99d5a95f72ff5")
    }

    @Test
    fun `prefixes SHA256 with zero`() {
        val checksum = "abcde\n".sha256
        assertThat(checksum).hasSize(64)
        assertThat(checksum).startsWith("0")
    }
}
