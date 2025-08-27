package org.orkg.dataimport.domain.csv

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.dataimport.domain.csv.CSV.State

internal class CSVTest {
    internal class StateTest {
        @Test
        fun `Given a state, when comparing order with isAfter, it returns the correct result`() {
            State.IMPORT_DONE isAfter State.UPLOADED shouldBe true
            State.UPLOADED isAfter State.IMPORT_DONE shouldBe false
        }

        @Test
        fun `Given a state, when comparing order with isBefore, it returns the correct result`() {
            State.UPLOADED isBefore State.IMPORT_DONE shouldBe true
            State.IMPORT_DONE isBefore State.UPLOADED shouldBe false
        }
    }
}
