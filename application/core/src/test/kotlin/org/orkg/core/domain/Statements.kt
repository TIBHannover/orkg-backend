package org.orkg.core.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

internal class StatementSpec :
    DescribeSpec({
      describe("equality") {
        val one = Statement.WithResource("a".toIRI(), "b".toIRI(), "c".toIRI())
        describe("statements with equal subject, predicate and object") {
          val same = Statement.WithResource("a".toIRI(), "b".toIRI(), "c".toIRI())
          it("should be equal") { one shouldBe same }
        }
      }
      xdescribe("sorting") {}
    })
