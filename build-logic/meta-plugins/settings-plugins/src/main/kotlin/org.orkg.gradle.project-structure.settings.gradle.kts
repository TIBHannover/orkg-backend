// Include all subfolders that contain a 'build.gradle.kts' as subprojects, if they are not part of an included build
rootDir
    .listFiles()
    ?.filter { File(it, "settings.gradle.kts").exists().not() && File(it, "build.gradle.kts").exists() }
    ?.forEach { subproject ->
        include(subproject.name)
    }
