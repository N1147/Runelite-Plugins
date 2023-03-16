
version = "1.0.9"

project.extra["PluginName"] = "Anarchise' Rune Dragon Plugin" // This is the name that is used in the external plugin manager panel
project.extra["PluginDescription"] = "Auto Rune Dragons." // This is the description that is used in the external plugin manager panel
project.extra["PluginSupportUrl"] = "https://discord.com/invite/KwJnhKQJVc"
dependencies {
    compileOnly(project(":NUtils"))
}

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

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
                            nameToId("NUtils")
                        ).joinToString(),
                "Plugin-Description" to project.extra["PluginDescription"],
                "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}