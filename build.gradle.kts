import org.orkg.gradle.plugins.PrintCoverageTask

plugins {
    id("org.orkg.gradle.root")

    base // required, otherwise report aggregation plugins will not work
    id("test-report-aggregation")
    id("jacoco-report-aggregation")
    id("org.orkg.gradle.print-coverage")

    id("io.github.janbarari.gradle-analytics-plugin") version "1.0.1"
}

gradleAnalyticsPlugin {
    database {
        local = sqlite {
            name = "gradle-analytics"
            path = layout.projectDirectory.asFile.path
        }
    }
    trackingTasks = setOf(
        // IDEA executes the "root" tasks without the leading colon. If we add those here, it does not record tasks
        // started from the IDE, although it would be the correct configuration.
        "assemble",
        "build",
        "compileAll",
        "test",
    )
    trackAllBranchesEnabled = true
}

dependencies {
    // Configure all projects for test and coverage aggregation. Ignore parent projects, or those without (tested) code.
    val parentProjects = subprojects
        .map(Project::getPath)
        .filter { path -> path.count { it == ':' } < 2 }
        .filterNot { it == ":rest-api-server" } // Not a parent project
        .toSet()
    val ignoredProjects = setOf(":platform", ":testing:kotest", ":testing:spring") union parentProjects
    subprojects
        .filterNot { it.path in ignoredProjects }
        .onEach(::jacocoAggregation)
        .onEach(::testReportAggregation)
}

// Test reports
reporting {
    reports {
        val testAggregateTestReport by creating(AggregateTestReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}

// JaCoCo Code Coverage Report
reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}

val testCodeCoverageReport by tasks.getting(JacocoReport::class) {
    reports {
        xml.required.set(true)
    }
}

tasks.check {
    dependsOn(tasks.named<TestReport>("testAggregateTestReport"))
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
    finalizedBy(tasks.named<PrintCoverageTask>("printCoverage"))
}
