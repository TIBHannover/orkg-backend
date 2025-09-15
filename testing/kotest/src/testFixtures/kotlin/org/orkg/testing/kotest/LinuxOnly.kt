package org.orkg.testing.kotest

import io.kotest.core.annotation.Condition
import io.kotest.core.spec.Spec
import org.apache.commons.lang3.SystemUtils.IS_OS_LINUX
import kotlin.reflect.KClass

class LinuxOnly : Condition {
    override fun evaluate(kclass: KClass<out Spec>): Boolean = IS_OS_LINUX
}
