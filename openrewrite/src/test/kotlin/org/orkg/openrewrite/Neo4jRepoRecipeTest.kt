package org.orkg.openrewrite

import org.junit.jupiter.api.Test
import org.openrewrite.kotlin.Assertions.kotlin
import org.openrewrite.kotlin.KotlinParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

internal class Neo4jRepoRecipeTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(Neo4jRepoRecipe("com.yourorg.FooBar"))
            .parser(
                KotlinParser.builder()
                    .classpath("spring-data-commons", "spring-data-neo4j")
            )
    }

    @Test
    fun splitPageQueryMethod() {
        rewriteRun(
            kotlin(
                """
                package com.yourorg

                import org.springframework.data.domain.Page
                import org.springframework.data.neo4j.repository.Neo4jRepository
                import org.springframework.data.neo4j.repository.query.Query
                
                private const val subsitution: String = "${'$'}subsitution"

                interface Neo4jResearchFieldRepository : Neo4jRepository<String, String> {
                
                    @Query(""${'"'}QUERY ${'$'}subsitution""${'"'},
                        countQuery = ""${'"'}COUNT""${'"'}
                    )
                    fun repoMethod(fieldId: String): Page<String>
                }
                """.trimIndent(),
                """
                package com.yourorg

                import org.springframework.data.domain.Page
                import org.springframework.data.neo4j.repository.Neo4jRepository
                import org.springframework.data.neo4j.repository.query.Query
                
                private const val subsitution: String = "${'$'}subsitution"

                interface Neo4jResearchFieldRepository : Neo4jRepository<String, String> {
                    @Query(""${'"'}QUERY ${'$'}subsitution""${'"'})
                    fun repoMethod(fieldId: String): List<String>
                    
                    @Query(""${'"'}QUERY ${'$'}subsitution""${'"'})
                    fun repoMethodCount(fieldId: String): Int
                }
                
                """.trimIndent()
            )
        )
    }
}
