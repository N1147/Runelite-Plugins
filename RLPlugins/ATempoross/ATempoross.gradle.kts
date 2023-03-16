version = "1.0.1"

project.extra["PluginName"] = "ATempoross"
project.extra["PluginDescription"] = "Automated tempoross."

dependencies {
    annotationProcessor(Libraries.lombok)
    annotationProcessor(Libraries.pf4j)
    compileOnly("net.runelite:runelite-api:1.9.14-SNAPSHOT")
    compileOnly("net.runelite:client:1.9.14-SNAPSHOT")
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