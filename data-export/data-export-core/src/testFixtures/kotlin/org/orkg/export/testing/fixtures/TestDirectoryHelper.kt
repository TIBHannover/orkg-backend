package org.orkg.export.testing.fixtures

import io.kotest.matchers.shouldBe
import java.io.File

fun verifyThatDirectoryExistsAndIsEmpty(dir: File): Unit = with(dir) {
    isDirectory shouldBe true
    exists() shouldBe true
    listFiles() shouldBe emptyArray()
}
