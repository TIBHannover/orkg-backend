package eu.tib.orkg.prototype.export.testing.fixtures

import io.kotest.matchers.shouldBe
import java.io.File

internal fun verifyThatDirectoryExistsAndIsEmpty(dir: File): Unit = with(dir) {
    isDirectory shouldBe true
    exists() shouldBe true
    listFiles() shouldBe emptyArray()
}
