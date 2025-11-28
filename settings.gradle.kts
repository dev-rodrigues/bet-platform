rootProject.name = "bet-platform"

fun includeChildren(relativePath: String) {
    file(relativePath).listFiles()
        ?.filter { it.isDirectory }
        ?.forEach { dir ->
            val hasBuildFile = dir.resolve("build.gradle.kts").exists() || dir.resolve("build.gradle").exists()
            val hasSources = dir.resolve("src").exists()

            if (hasBuildFile || hasSources) {
                val projectPath = ":${relativePath.replace("/", ":")}:${dir.name}"
                include(projectPath)
                project(projectPath).projectDir = dir
            }
        }
}

includeChildren("services")
includeChildren("libs")
