package top.dzhz.plugin.template.pages

import com.xjtu.toolbox.plugin.api.PluginContext
import com.xjtu.toolbox.plugin.api.PluginPage
import com.xjtu.toolbox.plugin.api.UiColumnNode
import com.xjtu.toolbox.plugin.api.UiListItemNode
import com.xjtu.toolbox.plugin.api.UiNode
import com.xjtu.toolbox.plugin.api.UiTextNode
import top.dzhz.plugin.template.caption
import top.dzhz.plugin.template.section

/** 关于：怎么把这个模板改成自己的插件。 */
internal class AboutPage(private val ctx: PluginContext) : PluginPage() {

    override fun id(): String = "about"

    override fun title(): String = "关于"

    override fun render(): UiNode = UiColumnNode(
        spacingDp = 12,
        children = listOf(
            UiTextNode(
                "这是岱宗盒子的插件模板。左边目录里的每一页都是清单 pages[] 里的一项，"
                    + "插件只负责返回界面树——导航、顶栏、主题、毛玻璃都由宿主处理。",
                secondary = true,
            ),
            section(
                "改成你自己的",
                "",
                UiListItemNode(title = "1. plugin.manifest.json", subtitle = "id、名字、作者、版本号、页面与目录分组"),
                UiListItemNode(title = "2. plugin/build.gradle.kts", subtitle = "namespace 和 applicationId"),
                UiListItemNode(title = "3. pages/", subtitle = "每个页面一个类，render() 返回 UiNode 树"),
                UiListItemNode(title = "4. python3 package.py <apk>", subtitle = "打成 zip 并签名"),
            ),
            caption("插件 id ${ctx.pluginId} · App ${ctx.appVersionCode} · 深色模式 ${ctx.isDarkTheme}"),
        ),
    )
}
