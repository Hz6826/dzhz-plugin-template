package top.dzhz.plugin.template.pages

import com.xjtu.toolbox.plugin.api.PluginContext
import com.xjtu.toolbox.plugin.api.PluginPage
import com.xjtu.toolbox.plugin.api.UiColumnNode
import com.xjtu.toolbox.plugin.api.UiNode
import com.xjtu.toolbox.plugin.api.UiRichTextNode
import com.xjtu.toolbox.plugin.api.UiTextColor
import com.xjtu.toolbox.plugin.api.UiTextNode
import com.xjtu.toolbox.plugin.api.UiTextSpan
import com.xjtu.toolbox.plugin.api.UiTextStyle
import top.dzhz.plugin.template.caption
import top.dzhz.plugin.template.section

/** 文字：字阶、颜色、样式、可点击、富文本混排。 */
internal class TextPage(private val ctx: PluginContext) : PluginPage() {

    override fun id(): String = "text"

    override fun title(): String = "文字与样式"

    override fun render(): UiNode = UiColumnNode(
        spacingDp = 12,
        children = listOf(
            section(
                "字阶",
                "宿主映射到自己的字号表，别假设像素",
                UiTextNode("Display 页面大标题", style = UiTextStyle.Display),
                UiTextNode("Title 分区标题", style = UiTextStyle.Title),
                UiTextNode("Section 卡片标题", style = UiTextStyle.Section),
                UiTextNode("Body 正文，默认就是这一档", style = UiTextStyle.Body),
                UiTextNode("Caption 脚注、说明", style = UiTextStyle.Caption),
                UiTextNode("secondary = true 用次要色", secondary = true),
                // maxLines = 2：超出的部分省略号截断
                UiTextNode(
                    "maxLines = 2 会截断：这一行故意写长，长到两行放不下，后面的内容会被省略号吃掉。",
                    maxLines = 2,
                ),
            ),
            section(
                "颜色",
                "枚举跟主题走；argb 精确覆盖，但深浅色要自己适配",
                UiTextNode("Default", color = UiTextColor.Default),
                UiTextNode("Secondary 次要", color = UiTextColor.Secondary),
                UiTextNode("Primary 主色", color = UiTextColor.Primary),
                UiTextNode("Success 成功", color = UiTextColor.Success),
                UiTextNode("Warning 警告", color = UiTextColor.Warning),
                UiTextNode("Error 错误", color = UiTextColor.Error),
                UiTextNode("argb = 0xFFFF8800", argb = 0xFFFF8800.toInt(), style = UiTextStyle.Caption),
            ),
            section(
                "样式",
                "可以叠着用",
                UiTextNode("加粗", bold = true),
                UiTextNode("斜体", italic = true),
                UiTextNode("下划线", underline = true),
                UiTextNode("删除线", strikeThrough = true),
                UiTextNode("三个一起", bold = true, italic = true, underline = true),
            ),
            section(
                "可点击文本",
                "给了 actionId 就自动用主色——不用自己写 color，用户才看得出能点",
                UiTextNode("点我一下", actionId = "text_click"),
                UiTextNode("只想让它可点、但保持普通颜色，就显式写 color = UiTextColor.Default"),
            ),
            section(
                "富文本混排",
                "同一段话里混样式和链接，段与段之间没有间距",
                UiRichTextNode(
                    spans = listOf(
                        UiTextSpan("成绩已更新："),
                        UiTextSpan("高等数学 92", bold = true),
                        UiTextSpan("、"),
                        UiTextSpan("线性代数 88", bold = true),
                        UiTextSpan("，"),
                        UiTextSpan("查看详情", underline = true, actionId = "rich_click"),
                        UiTextSpan("。"),
                    ),
                ),
            ),
            caption("文字都是纯文本，不解析 Markdown。"),
        ),
    )

    override fun onAction(actionId: String, value: String?) {
        when (actionId) {
            "text_click", "rich_click" -> ctx.toast("点了：$actionId")
        }
    }
}
