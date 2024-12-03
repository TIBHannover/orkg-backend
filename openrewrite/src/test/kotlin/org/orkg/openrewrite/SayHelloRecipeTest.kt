package org.orkg.openrewrite

import org.junit.jupiter.api.Test
import org.openrewrite.kotlin.Assertions.kotlin
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

internal class SayHelloRecipeTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(SayHelloRecipe("com.yourorg.FooBar"))
    }

    @Test
    fun addsHelloToFooBar() {
        rewriteRun(
            kotlin(
                """
                package com.yourorg

                class FooBar {
                }
                
                """.trimIndent(),
                """
                package com.yourorg

                class FooBar {
                    fun hello(): String {
                        return "Hello from com.yourorg.FooBar!"
                    }
                }
                
                """.trimIndent()
            )
        )
    }

    @Test
    fun doesNotChangeExistingHello() {
        rewriteRun(
            kotlin(
                """
                package com.yourorg
    
                class FooBar {
                    fun hello(): String { return "" }
                }
                
                """.trimIndent()
            )
        )
    }

    @Test
    fun doesNotChangeOtherClasses() {
        rewriteRun(
            kotlin(
                """
                package com.yourorg
    
                class Bash {
                }
                
                """.trimIndent()
            )
        )
    }
}
