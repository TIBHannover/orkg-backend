package org.orkg.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MD5Test {
    @Test
    fun `calculates the MD5 of the empty string correctly`() {
        assertThat("".md5).isEqualTo("d41d8cd98f00b204e9800998ecf8427e")
    }

    @Test
    fun `calculates the MD5 of an example string correctly`() {
        assertThat("Hello, world!\n".md5).isEqualTo("746308829575e17c3331bbcb00c0898b")
    }

    @Test
    fun `prefixes MD5 with zero`() {
        val checksum = "a".md5
        assertThat(checksum).hasSize(32)
        assertThat(checksum).startsWith("0")
    }
}
