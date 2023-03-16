buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.0.0")
        classpath("org.mariadb.jdbc:mariadb-java-client:2.1.2")
    }
}

plugins {
    java
    checkstyle
}

project.extra["GithubUrl"] = "https://github.com/N1147/RLPlugins"

apply<BootstrapPlugin>()

allprojects {
    group = "net.runelite.externals"
    apply<MavenPublishPlugin>()
}

allprojects {
    apply<MavenPublishPlugin>()

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
}

subprojects {
    group = "net.runelite.externals"

    project.extra["PluginProvider"] = "ZTD#1147"
    project.extra["ProjectSupportUrl"] = "https://discord.gg/rzaXeZdeNu"
    project.extra["PluginLicense"] = "3-Clause BSD License"

    repositories {
        jcenter {
            content {
                excludeGroupByRegex("net\\.runelite*")
            }
        }

        exclusiveContent {
            forRepository {
                mavenLocal()
            }
            filter {
                includeGroupByRegex("net\\.runelite*")
            }
        }
    }

    apply<JavaPlugin>()

    dependencies {
        annotationProcessor(Libraries.lombok)
        annotationProcessor(Libraries.pf4j)
        compileOnly("net.runelite:runelite-api:1.9.14-SNAPSHOT")
        compileOnly("net.runelite:client:1.9.14-SNAPSHOT")
        compileOnly(Libraries.findbugs)
        compileOnly(Libraries.apacheCommonsText)
        compileOnly(Libraries.guava)
        compileOnly(Libraries.gson)
        compileOnly(Libraries.guice)
        compileOnly(Libraries.lombok)
        compileOnly(Libraries.okhttp3)
        compileOnly(Libraries.pf4j)
        compileOnly(Libraries.rxjava)
        compileOnly("org.mariadb.jdbc:mariadb-java-client:2.1.2")
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                url = uri("$buildDir/repo")
            }
        }
        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])
            }
        }
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        register<Copy>("copyDeps") {
            into("./build/deps/")
            from(configurations["runtimeClasspath"])
        }

        withType<Jar> {
            doLast {
                copy {
                    from("./build/libs/out/")
                    into(System.getProperty("user.home") + "/.runelite/sideloaded-plugins")
                }
            }
        }

        withType<AbstractArchiveTask> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
            dirMode = 493
            fileMode = 420
        }
    }
}
