package top.dzhz.plugin.template.pages

import com.xjtu.toolbox.plugin.api.PluginContext
import com.xjtu.toolbox.plugin.api.PluginPage
import com.xjtu.toolbox.plugin.api.UiAlign
import com.xjtu.toolbox.plugin.api.UiBadgeNode
import com.xjtu.toolbox.plugin.api.UiButtonNode
import com.xjtu.toolbox.plugin.api.UiButtonStyle
import com.xjtu.toolbox.plugin.api.UiCardNode
import com.xjtu.toolbox.plugin.api.UiColumnNode
import com.xjtu.toolbox.plugin.api.UiFlowRowNode
import com.xjtu.toolbox.plugin.api.UiNode
import com.xjtu.toolbox.plugin.api.UiRowNode
import com.xjtu.toolbox.plugin.api.UiScrollRowNode
import com.xjtu.toolbox.plugin.api.UiTextNode
import com.xjtu.toolbox.plugin.api.UiTextStyle
import com.xjtu.toolbox.plugin.api.UiWeightNode
import top.dzhz.plugin.template.caption
import top.dzhz.plugin.template.section

/** 布局容器：对齐、权重、内边距、横滑、流式换行。 */
internal class LayoutPage(private val ctx: PluginContext) : PluginPage() {

    override fun id(): String = "layout"

    override fun title(): String = "布局容器"

    override fun render(): UiNode = UiColumnNode(
        spacingDp = 12,
        children = listOf(
            section(
                "对齐",
                "Column 的 alignment；Row 还有 horizontal / vertical 两个方向",
                alignDemo("Start", UiAlign.Start),
                alignDemo("Center", UiAlign.Center),
                alignDemo("End", UiAlign.End),
            ),
            section(
                "权重",
                "Row 里用 UiWeightNode 分宽度，这里是 1 : 2",
                UiRowNode(
                    spacingDp = 8,
                    children = listOf(
                        UiWeightNode(1f, UiCardNode(UiTextNode("1 份"), paddingDp = 12)),
                        UiWeightNode(2f, UiCardNode(UiTextNode("2 份"), paddingDp = 12)),
                    ),
                ),
            ),
            section(
                "卡片内边距",
                "paddingDp 默认 16；贴边设计可以调小",
                UiCardNode(UiTextNode("paddingDp = 0"), paddingDp = 0),
                UiCardNode(UiTextNode("paddingDp = 8"), paddingDp = 8),
                UiCardNode(UiTextNode("paddingDp = 16（默认）")),
            ),
            section(
                "横向滚动",
                "UiScrollRowNode：子节点按自身宽度排成一排，左右滑；宽度有上限，不会一张卡占满",
                UiScrollRowNode(
                    children = (1..6).map { index ->
                        UiCardNode(UiTextNode("卡片 $index", style = UiTextStyle.Body), paddingDp = 12)
                    },
                ),
            ),
            section(
                "流式换行",
                "UiFlowRowNode：放不下自动换行，行内左对齐",
                UiFlowRowNode(
                    children = listOf(
                        "课表", "成绩", "图书馆", "校园卡", "通知", "考试",
                        "作业", "空教室", "校车", "体测", "优惠券", "教材",
                    ).map { UiBadgeNode(it) },
                ),
            ),
            caption("布局容器都会吃满可用宽度；要"按内容"就用横滑行或者流式换行。"),
        ),
    )

    private fun alignDemo(label: String, align: UiAlign): UiNode = UiColumnNode(
        spacingDp = 6,
        alignment = align,
        children = listOf(
            UiTextNode(label, style = UiTextStyle.Caption, secondary = true),
            UiButtonNode("按钮", "noop", style = UiButtonStyle.Tonal),
        ),
    )

    override fun onAction(actionId: String, value: String?) {
        if (actionId == "noop") ctx.toast("按钮")
    }
}
