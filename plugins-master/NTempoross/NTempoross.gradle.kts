

version = "0.0.1"

project.extra["PluginName"] = "NTempoross"
project.extra["PluginDescription"] = "Automatically fights Tempoross"
project.extra["PluginProvider"] = "ZTD#1147"
project.extra["PluginSupportUrl"] = "https://github.com/N1147/download"

dependencies {
    compileOnly(project(":NUtils"))
}

tasks {
    jar {
        manifest {
            attributes(mapOf(
                "Plugin-Version" to project.version,
                "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                "Plugin-Provider" to project.extra["PluginProvider"],
                "Plugin-Dependencies" to
                        arrayOf(
                            nameToId("NUtils")).joinToString(),
                "Plugin-Description" to project.extra["PluginDescription"],
                "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}