package top.dzhz.plugin.template.pages

import com.xjtu.toolbox.plugin.api.PluginContext
import com.xjtu.toolbox.plugin.api.PluginPage
import com.xjtu.toolbox.plugin.api.UiButtonNode
import com.xjtu.toolbox.plugin.api.UiButtonStyle
import com.xjtu.toolbox.plugin.api.UiCardNode
import com.xjtu.toolbox.plugin.api.UiColumnNode
import com.xjtu.toolbox.plugin.api.UiFieldNode
import com.xjtu.toolbox.plugin.api.UiNode
import com.xjtu.toolbox.plugin.api.UiOption
import com.xjtu.toolbox.plugin.api.UiRowNode
import com.xjtu.toolbox.plugin.api.UiSelectNode
import com.xjtu.toolbox.plugin.api.UiSwitchNode
import com.xjtu.toolbox.plugin.api.UiTextNode
import com.xjtu.toolbox.plugin.api.UiTextStyle
import top.dzhz.plugin.template.caption
import top.dzhz.plugin.template.section

/** 输入与操作：按钮、输入框、开关、下拉、可点卡片。 */
internal class InputPage(private val ctx: PluginContext) : PluginPage() {

    @Volatile
    private var count = 0

    @Volatile
    private var name = ""

    @Volatile
    private var reminder = true

    @Volatile
    private var term = "2026-2027-1"

    override fun id(): String = "input"

    override fun title(): String = "输入与操作"

    private val terms = listOf(
        UiOption("2026-2027-1", "2026–2027 学年 第一学期"),
        UiOption("2025-2026-2", "2025–2026 学年 第二学期"),
        UiOption("2025-2026-1", "2025–2026 学年 第一学期"),
    )

    override fun render(): UiNode = UiColumnNode(
        spacingDp = 12,
        children = listOf(
            section(
                "按钮",
                "Filled / Tonal 两种外观，enabled = false 置灰；更轻的操作用可点击文本",
                UiRowNode(
                    spacingDp = 8,
                    children = listOf(
                        UiButtonNode("主要", "count"),
                        UiButtonNode("次要", "count", style = UiButtonStyle.Tonal),
                        UiButtonNode("禁用", "count", enabled = false),
                        UiButtonNode("清零", "reset", style = UiButtonStyle.Tonal),
                    ),
                ),
                caption("点了 $count 次"),
            ),
            section(
                "输入框",
                "每改一个字宿主都把整段文本回传（value 非 null），值以插件为准",
                UiFieldNode(label = "你的名字", value = name, actionId = "name", hint = "随便写点什么"),
                caption(if (name.isBlank()) "还没有输入" else "你好，$name"),
            ),
            section(
                "开关",
                "点击回传目标值 \"true\" / \"false\"，插件自己存",
                UiSwitchNode(
                    label = "每天提醒",
                    summary = "早上八点推一条当天的课",
                    checked = reminder,
                    actionId = "reminder",
                ),
            ),
            section(
                "下拉单选",
                "最多 8 个选项，再多就该自己开一页；回传的是 key，不是下标",
                UiSelectNode(label = "学期", value = term, options = terms, actionId = "term"),
                caption(terms.firstOrNull { it.key == term }?.label ?: term),
            ),
            UiCardNode(
                UiColumnNode(
                    spacingDp = 6,
                    children = listOf(
                        UiTextNode("可点的卡片", style = UiTextStyle.Section),
                        caption("整张卡可点，按下有反馈；比塞一个按钮自然"),
                    ),
                ),
                actionId = "card_click",
            ),
        ),
    )

    override fun onAction(actionId: String, value: String?) {
        when (actionId) {
            "count" -> count++
            "reset" -> count = 0
            "name" -> name = value.orEmpty()
            "reminder" -> reminder = value == "true"
            "term" -> term = value.orEmpty()
            "card_click" -> ctx.toast("卡片被点了")
        }
        invalidate()
    }
}
