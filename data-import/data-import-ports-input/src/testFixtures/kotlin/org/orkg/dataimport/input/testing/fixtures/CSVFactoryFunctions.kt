package org.orkg.dataimport.input.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.common.testing.fixtures.Assets.csv
import org.orkg.dataimport.domain.csv.CSV.Format
import org.orkg.dataimport.domain.csv.CSV.Type
import org.orkg.dataimport.input.CreateCSVUseCase

fun createCSVCommand() = CreateCSVUseCase.CreateCommand(
    contributorId = ContributorId("e7de95a8-d1f5-4837-9a1f-a2eb8b45a254"),
    name = "papers.csv",
    data = csv("papers"),
    type = Type.PAPER,
    format = Format.DEFAULT,
)
