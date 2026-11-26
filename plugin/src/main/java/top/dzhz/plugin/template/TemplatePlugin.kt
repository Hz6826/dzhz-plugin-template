package top.dzhz.plugin.template

import com.xjtu.toolbox.plugin.api.Plugin
import com.xjtu.toolbox.plugin.api.PluginContext
import com.xjtu.toolbox.plugin.api.PluginEntry
import com.xjtu.toolbox.plugin.api.PluginPage
import com.xjtu.toolbox.plugin.api.PluginTool

/**
 * 插件入口：清单里 `dex.entryClass` 指向这里，必须有无参构造。
 * 宿主每个插件只建一份 [Plugin]，页面和工具都从它取。
 */
class TemplatePlugin : PluginEntry {
    override fun create(context: PluginContext): Plugin = TemplatePluginImpl(context)
}

private class TemplatePluginImpl(private val ctx: PluginContext) : Plugin {
    private val page = TemplatePage(ctx)

    override fun pages(): List<PluginPage> = listOf(page)

    override fun tools(): List<PluginTool> = listOf(TemplateTool(ctx))
}
