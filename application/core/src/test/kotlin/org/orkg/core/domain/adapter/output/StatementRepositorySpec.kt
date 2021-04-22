package org.orkg.core.domain.adapter.output

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.core.adapter.output.StatementRepository

class StatementRepositorySpec :
    DescribeSpec({
      val repo: StatementRepository = object : StatementRepository {}

      describe("find statement") { describe("find by id") {} }
    })
