version = "0.0.5"

project.extra["PluginName"] = "NumbSuite"
project.extra["PluginDescription"] = "Numb Plugin Suite"

dependencies {
    annotationProcessor(Libraries.lombok)
    annotationProcessor(Libraries.pf4j)
    compileOnly("com.openosrs:runelite-api:4.20.3")
    compileOnly("com.openosrs:runelite-client:4.20.3")
    compileOnly(Libraries.guice)
    compileOnly(Libraries.lombok)
    compileOnly(Libraries.pf4j)
}

tasks {
    jar {
        manifest {
            attributes(mapOf(
                "Plugin-Version" to project.version,
                "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                "Plugin-Provider" to project.extra["PluginProvider"],
                "Plugin-Description" to project.extra["PluginDescription"],
                "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}