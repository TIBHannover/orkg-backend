plugins {
    scala
}

repositories {
    jcenter()
}

dependencies {
    // Karate Testing Framework
    val karateVersion = "1.0.0"
    testImplementation("com.intuit.karate:karate-junit5:$karateVersion")
    testImplementation("com.intuit.karate:karate-gatling:$karateVersion")
}

tasks {
    withType(Test::class.java).configureEach {
        useJUnitPlatform()

        // Make Karate env vars available (to Gatling)
        systemProperty("karate.options", System.getProperties().getProperty("karate.options"))
        systemProperty("karate.env", System.getProperties().getProperty("karate.env"))
        //outputs.upToDateWhen { false }
    }

    val runGatling by creating(JavaExec::class) {
        File("$buildDir/reports/gatling").mkdirs()

        group = "Web tests"
        description = "Run Gatling tests"
        classpath = sourceSets["test"].output.plus(configurations.testRuntimeClasspath.get())
        main = "io.gatling.app.Gatling"
        args = listOf(
            // change this to suit your simulation entry-point
            "-s", "performance.SimpleSimulation",
            "-rf", "${buildDir}/reports/gatling"
        )
        systemProperties = System.getProperties().mapKeys { it.key.toString() }
    }
}

sourceSets {
    test {
        resources {
            srcDir(file("src/test/scala"))
            exclude("**/*.java")
            exclude("**/*.kt")
            exclude("**/*.scala")
        }
    }
}
