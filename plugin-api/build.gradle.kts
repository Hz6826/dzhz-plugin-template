// 宿主 API 的副本，**不要在这里改**：源码在盒子仓库的 plugin/api/，
// 构建前会自动从这里拉最新的一份（文件缺失时必拉，-PsyncApi 强制刷新）。
//
// 为什么是副本而不是依赖：插件只需要跟着当前宿主走，不需要版本化的 maven 坐标；
// 等 API 冻结了再发到 jitpack。
plugins {
    id("com.android.library")
}

android {
    namespace = "com.xjtu.toolbox.plugin.api"
    compileSdk {
        version = release(37) {
        }
    }
    defaultConfig {
        minSdk = 31
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

// ── 自动同步宿主 API ────────────────────────────────────────────────────────
// 分支合并到 main 之后，把下面的 feat/plugin-zip 换成 main。
val apiRawBase = "https://raw.githubusercontent.com/yeliqin666/xjtu-toolbox-android/feat/plugin-zip" +
    "/plugin/api/src/main/java/com/xjtu/toolbox/plugin/api"
val apiFileNames = listOf("PluginApi.kt", "PluginUi.kt")
val apiDirPath = layout.projectDirectory.dir("src/main/java/com/xjtu/toolbox/plugin/api").asFile.absolutePath
val forceSync = providers.gradleProperty("syncApi").isPresent

val syncPluginApi by tasks.registering {
    group = "plugin"
    description = "从宿主仓库拉最新的插件 API（缺文件时自动，-PsyncApi 强制）"
    doLast {
        val dir = File(apiDirPath).apply { mkdirs() }
        apiFileNames.forEach { name ->
            val target = File(dir, name)
            if (!forceSync && target.isFile && target.length() > 0L) return@forEach
            val url = java.net.URI("$apiRawBase/$name").toURL()
            println("同步插件 API：$name")
            url.openStream().use { input -> target.outputStream().use { input.copyTo(it) } }
            require(target.length() > 0L) { "拉 $name 失败：内容是空的" }
        }
    }
}

tasks.named("preBuild") { dependsOn(syncPluginApi) }
