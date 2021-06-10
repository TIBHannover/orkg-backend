package org.orkg.extensions

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.numericDoubles
import io.kotest.property.arbitrary.of
import io.kotest.property.checkAll
import java.util.Locale

@ExperimentalKotest
class EngineeringNotationSpecs : DescribeSpec({
    context("parsing valid strings") {
        val displayName: (Pair<String, Long>) -> String = { """should convert ${it.first} to ${it.second}""" }
        context("positive integers") {
            withData<Pair<String, Long>>(
                displayName,
                sequenceOf(
                    "0" to 0,
                    "42" to 42,
                    "1k" to 1000,
                    "2M" to 2_000_000,
                    "3G" to 3_000_000_000,
                    "4T" to 4_000_000_000_000,
                    "5P" to 5_000_000_000_000_000,
                    "6E" to 6_000_000_000_000_000_000,
                    // Zetta and Yotta do not fit into a Long anymore…
                )
            ) { (input, expected) ->
                input.parseEngineeringNotation() shouldBe expected
            }
        }
        context("negative integers") {
            withData<Pair<String, Long>>(
                displayName,
                sequenceOf(
                    "-1" to -1,
                    "-42G" to -42_000_000_000,
                )
            ) { (input, expected) ->
                input.parseEngineeringNotation() shouldBe expected
            }
        }
        context("positive floating point") {
            withData<Pair<String, Long>>(
                displayName,
                sequenceOf(
                    "2.7182818285k" to 2718, // TODO: truncated -> extend to BigDecimal?
                )
            ) { (input, expected) ->
                input.parseEngineeringNotation() shouldBe expected
            }
        }
        context("negative floating point") {
            withData<Pair<String, Long>>(
                displayName,
                sequenceOf(
                    "-3.141k" to -3_141,
                )
            ) { (input, expected) ->
                input.parseEngineeringNotation() shouldBe expected
            }
        }
        it("successfully converts randomly generated numbers") {
            checkAll(engineeringNotationGenerator) { input ->
                input.parseEngineeringNotation() shouldNotBe null
            }
        }
    }
})

val engineeringNotationGenerator = Arb.numericDoubles().flatMap { number ->
    Arb.of("", "k", "M", "G", "T", "P", "E").map { extension ->
        "%f".format(Locale.ENGLISH, number) + extension
    }
}
