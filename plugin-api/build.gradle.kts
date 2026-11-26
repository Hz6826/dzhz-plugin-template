// 宿主 API 的副本：跟盒子的 plugin/api 同步，不要在这里加自己的东西。
// 宿主只提供 Java ABI 干净的这几个类，插件编译时依赖它们，运行时由宿主提供。
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
