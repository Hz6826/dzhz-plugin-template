package dev.dzhz.plugin.template

import com.xjtu.toolbox.plugin.api.PluginContext
import com.xjtu.toolbox.plugin.api.PluginTool

/**
 * 给小岱用的工具。名字和参数 schema 写在 `dzhz.plugin.json` 的 `dex.tools` 里，
 * 这里只按名字提供执行体；[execute] 在宿主的 IO 线程调用，可以阻塞。
 */
internal class TemplateTool(private val ctx: PluginContext) : PluginTool {

    override fun name(): String = "echo"

    override fun execute(argsJson: String): String {
        ctx.log("echo 收到：$argsJson")
        return "插件收到参数：${argsJson.ifBlank { "（空）" }}"
    }
}
