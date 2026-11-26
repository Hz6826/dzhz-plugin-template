package top.dzhz.plugin.template

import com.xjtu.toolbox.plugin.api.Plugin
import com.xjtu.toolbox.plugin.api.PluginContext
import com.xjtu.toolbox.plugin.api.PluginEntry
import com.xjtu.toolbox.plugin.api.PluginPage
import com.xjtu.toolbox.plugin.api.PluginTool
import top.dzhz.plugin.template.pages.AboutPage
import top.dzhz.plugin.template.pages.BackendPage
import top.dzhz.plugin.template.pages.DisplayPage
import top.dzhz.plugin.template.pages.InputPage
import top.dzhz.plugin.template.pages.LayoutPage
import top.dzhz.plugin.template.pages.TextPage

/**
 * 插件入口：清单里 `dex.entryClass` 指向这里，必须有无参构造。
 *
 * 这里列出所有页面；侧栏目录由清单 `pages[]` 生成（`group` 相同归到一组），
 * 插件不用写任何导航代码。
 */
class TemplatePlugin : PluginEntry {
    override fun create(context: PluginContext): Plugin = TemplatePluginImpl(context)
}

private class TemplatePluginImpl(private val ctx: PluginContext) : Plugin {

    private val pages = listOf(
        TextPage(ctx),
        InputPage(ctx),
        DisplayPage(ctx),
        LayoutPage(ctx),
        BackendPage(ctx),
        AboutPage(ctx),
    )

    override fun pages(): List<PluginPage> = pages

    override fun tools(): List<PluginTool> = listOf(TemplateTool(ctx))
}
