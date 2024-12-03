package org.orkg.openrewrite

import org.junit.jupiter.api.Test
import org.openrewrite.kotlin.Assertions.kotlin
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

internal class SuspendFunRecipeTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(SuspendFunRecipe("com.yourorg.FooBar"))
    }

    @Test
    fun addSuspendToFun() {
        rewriteRun(
            kotlin(
                """
                package com.yourorg

                class FooBar {
                    fun abc() {
                        
                    }
                }
                
                """.trimIndent(),
                """
                package com.yourorg

                class FooBar {
                    suspend fun abc() {
                        
                    }
                }
                
                """.trimIndent()
            )
        )
    }
}
