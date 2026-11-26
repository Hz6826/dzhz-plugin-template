// 宿主 API 的副本，**不要在这里改**：源码在盒子仓库的 plugin/api/，
// 构建前会自动从这里拉最新的一份（文件缺失时必拉，-PsyncApi 强制刷新）。
//
// 为什么是副本而不是依赖：插件只需要跟着当前宿主走，不需要版本化的 maven 坐标；
// 等 API 冻结了再发到 jitpack。
import java.io.File
import java.net.URI
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

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

/**
 * 从宿主仓库拉插件 API。用自定义 task 而不是在 `doLast` 里闭包捕获脚本变量：
 * 后者会让配置缓存没法序列化（Gradle 9 直接报错）。
 */
abstract class SyncPluginApiTask : DefaultTask() {

    /** 依次尝试的下载源：国内访问 raw.githubusercontent.com 经常不通，先用 jsDelivr 镜像。 */
    @get:Input
    abstract val rawBases: ListProperty<String>

    @get:Input
    abstract val fileNames: ListProperty<String>

    /** true = 不管本地有没有都重新拉。 */
    @get:Input
    abstract val force: Property<Boolean>

    @get:OutputDirectory
    abstract val target: DirectoryProperty

    @TaskAction
    fun sync() {
        val dir = target.get().asFile.apply { mkdirs() }
        fileNames.get().forEach { name ->
            val out = File(dir, name)
            if (!force.get() && out.isFile && out.length() > 0L) return@forEach
            val ok = rawBases.get().any { base ->
                runCatching {
                    URI("$base/$name").toURL().openStream().use { input ->
                        out.outputStream().use { input.copyTo(it) }
                    }
                    out.length() > 0L
                }.getOrDefault(false)
            }
            require(ok) { "拉 $name 失败：所有下载源都不通（检查网络，或者用 ./sync-api.sh）" }
            logger.lifecycle("同步插件 API：$name")
        }
    }
}

// 插件 API 跟着宿主的 dev 分支走；等 dev 合进 main 再换成 main。
val syncPluginApi by tasks.registering(SyncPluginApiTask::class) {
    group = "plugin"
    description = "从宿主仓库拉最新的插件 API（缺文件时自动，-PsyncApi 强制）"
    rawBases.set(
        listOf(
            // raw 优先：CI 在 GitHub runner 上一定通，拿到的也是当前分支最新内容
            "https://raw.githubusercontent.com/xjtu-dzhz/dzhz/dev" +
                "/plugin/api/src/main/java/com/xjtu/toolbox/plugin/api",
            // jsDelivr 兜底：国内访问 raw 经常不通，缺点是分支内容有缓存（可能是几小时前的）
            "https://cdn.jsdelivr.net/gh/xjtu-dzhz/dzhz@dev" +
                "/plugin/api/src/main/java/com/xjtu/toolbox/plugin/api",
        ),
    )
    fileNames.set(listOf("PluginApi.kt", "PluginUi.kt"))
    force.set(providers.gradleProperty("syncApi").isPresent)
    target.set(layout.projectDirectory.dir("src/main/java/com/xjtu/toolbox/plugin/api"))
}

tasks.named("preBuild") { dependsOn(syncPluginApi) }
