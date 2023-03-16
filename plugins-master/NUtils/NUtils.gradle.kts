version = "0.0.5"

project.extra["PluginName"] = "NUtils"
project.extra["PluginDescription"] = "Numb plugin utilities."

dependencies {
    annotationProcessor(Libraries.lombok)
    annotationProcessor(Libraries.pf4j)
    compileOnly("com.openosrs:runelite-api:4.31.2")
    compileOnly("com.openosrs:runelite-client:4.31.2")
    compileOnly(Libraries.guice)
    compileOnly(Libraries.lombok)
    compileOnly(Libraries.pf4j)
    compileOnly("org.mariadb.jdbc:mariadb-java-client:2.1.2")
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