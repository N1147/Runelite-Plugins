/*
 * Copyright (c) 2021-2022, Numb#1147 <https://github.com/NumbPlugins>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
version = "0.0.1"

project.extra["PluginName"] = "NQuickPot"
project.extra["PluginDescription"] = "Automatically drinks pots for you."
project.extra["PluginProvider"] = "ZTD#1147"
project.extra["PluginSupportUrl"] = "https://github.com/N1147/download"
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