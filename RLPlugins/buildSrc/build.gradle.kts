

plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

dependencies {
    implementation(gradleApi())
    implementation(group = "org.json", name = "json", version = "20190722")
    implementation(group = "com.savvasdalkitsis", name = "json-merge", version = "0.0.4")
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "4.2.2")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}