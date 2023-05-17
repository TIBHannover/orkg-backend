package eu.tib.orkg.prototype.testing.kotest

import io.kotest.core.annotation.EnabledCondition
import io.kotest.core.spec.Spec
import kotlin.reflect.KClass
import org.apache.commons.lang3.SystemUtils.IS_OS_LINUX

class LinuxOnly : EnabledCondition {
    override fun enabled(kclass: KClass<out Spec>): Boolean = IS_OS_LINUX
}
