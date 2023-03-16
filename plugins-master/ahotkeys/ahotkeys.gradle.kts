

version = "1.0.2"

project.extra["PluginName"] = "AHotkey" // This is the name that is used in the external plugin manager panel
project.extra["PluginDescription"] = "Anarchise' Widget Hotkeys" // This is the description that is used in the external plugin manager panel
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
    dependencies {
        classpath("com.android.tools.build:gradle:3.0.0")
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