plugins {
  kotlin("jvm")
  id("com.diffplug.spotless")
  // id("io.gitlab.arturbosch.detekt")
}

repositories { jcenter() }

dependencies {
  api(platform(project(":platform")))

  val kotestVersion = "4.4.3"
  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}

spotless {
  kotlin { ktfmt() }
  kotlinGradle { ktfmt() }
}

tasks.withType<Test> { useJUnitPlatform() }
