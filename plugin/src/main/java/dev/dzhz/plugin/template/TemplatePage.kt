package dev.dzhz.plugin.template

import com.xjtu.toolbox.plugin.api.PluginContext
import com.xjtu.toolbox.plugin.api.PluginPage
import com.xjtu.toolbox.plugin.api.UiButtonNode
import com.xjtu.toolbox.plugin.api.UiButtonStyle
import com.xjtu.toolbox.plugin.api.UiCardNode
import com.xjtu.toolbox.plugin.api.UiColumnNode
import com.xjtu.toolbox.plugin.api.UiDividerNode
import com.xjtu.toolbox.plugin.api.UiEmptyNode
import com.xjtu.toolbox.plugin.api.UiFieldNode
import com.xjtu.toolbox.plugin.api.UiImageNode
import com.xjtu.toolbox.plugin.api.UiListItemNode
import com.xjtu.toolbox.plugin.api.UiLoadingNode
import com.xjtu.toolbox.plugin.api.UiNode
import com.xjtu.toolbox.plugin.api.UiRowNode
import com.xjtu.toolbox.plugin.api.UiSpacerNode
import com.xjtu.toolbox.plugin.api.UiTextNode
import com.xjtu.toolbox.plugin.api.UiTextStyle

/**
 * 模板页：把宿主支持的所有 [UiNode] 都摆一遍，照着抄就行。
 *
 * 这个页面里**一行 Compose 都没有**——插件只交回数据，宿主用自己的组件画，
 * 所以深色模式、主题色、字阶、圆角都自动跟盒子一致。
 *
 * 写法要点：
 * - 状态放在字段里，`@Volatile` 因为宿主可能在别的线程拿；
 * - 改完字段调一次 `invalidate()`，宿主会重新要一次 [render]；
 * - [render] 在主线程被调用，别在里面做 IO。
 */
internal class TemplatePage(private val ctx: PluginContext) : PluginPage() {

    @Volatile
    private var count = 0

    @Volatile
    private var name = ""

    /** 0 = 什么都不显示，1 = 加载中，2 = 空状态。 */
    @Volatile
    private var state = 0

    override fun id(): String = "home"

    override fun title(): String = "组件一览"

    override fun render(): UiNode = UiColumnNode(
        spacingDp = 12,
        children = listOf(
            UiTextNode(
                "这一页把所有能用的节点都摆了一遍：文字、按钮、列表项、输入框、图片、" +
                    "加载与空状态。界面全部由宿主渲染，插件只负责交数据。",
                secondary = true,
            ),
            textCard(),
            buttonCard(),
            listCard(),
            fieldCard(),
            imageCard(),
            stateCard(),
            UiSpacerNode(4),
            UiDividerNode(),
            UiTextNode(
                "插件 ${ctx.pluginId} · 深色模式 ${ctx.isDarkTheme} · App ${ctx.appVersionCode}",
                style = UiTextStyle.Caption,
                secondary = true,
            ),
        ),
    )

    override fun onAction(actionId: String, value: String?) {
        when (actionId) {
            "count" -> count++
            "reset" -> count = 0
            "name" -> name = value.orEmpty()
            "row_click" -> ctx.toast("列表项被点了")
            "loading" -> state = 1
            "empty" -> state = 2
            "normal" -> state = 0
            else -> ctx.log("未处理的动作：$actionId")
        }
        // 改完状态必须 invalidate，宿主才会重新要一次 render()
        invalidate()
    }

    // ── 各个分区 ────────────────────────────────────────────────────────────

    private fun textCard(): UiNode = UiCardNode(
        UiColumnNode(
            spacingDp = 8,
            children = listOf(
                UiTextNode("文字与字阶", style = UiTextStyle.Section),
                UiTextNode("Display：页面大标题", style = UiTextStyle.Display),
                UiTextNode("Title：分区标题", style = UiTextStyle.Title),
                UiTextNode("Section：卡片标题", style = UiTextStyle.Section),
                UiTextNode("Body：正文，默认就是这一档。", style = UiTextStyle.Body),
                UiTextNode("Caption：脚注、说明", style = UiTextStyle.Caption),
                UiDividerNode(),
                UiTextNode("secondary = true 用次要色，说明性文字都用它。", secondary = true),
                UiTextNode(
                    "maxLines = 2 会截断并省略：这一行故意写得很长很长，长到两行放不下，" +
                        "后面的内容会被省略号吃掉，用来演示截断效果。",
                    maxLines = 2,
                ),
            ),
        ),
    )

    private fun buttonCard(): UiNode = UiCardNode(
        UiColumnNode(
            spacingDp = 10,
            children = listOf(
                UiTextNode("按钮", style = UiTextStyle.Section),
                UiTextNode(
                    "三种外观 Filled / Tonal / Plain，都能用 enabled = false 置灰。" +
                        "点击通过 onAction(actionId, value) 回到插件。",
                    style = UiTextStyle.Caption,
                    secondary = true,
                ),
                UiRowNode(
                    children = listOf(
                        UiButtonNode("主要", "count"),
                        UiButtonNode("次要", "count", style = UiButtonStyle.Tonal),
                        UiButtonNode("文字", "count", style = UiButtonStyle.Plain),
                    ),
                ),
                UiRowNode(
                    children = listOf(
                        UiButtonNode("禁用", "count", enabled = false),
                        UiButtonNode("清零", "reset", style = UiButtonStyle.Tonal),
                    ),
                ),
                UiTextNode("点了 $count 次", style = UiTextStyle.Caption, secondary = true),
            ),
        ),
    )

    private fun listCard(): UiNode = UiCardNode(
        UiColumnNode(
            spacingDp = 8,
            children = listOf(
                UiTextNode("列表项", style = UiTextStyle.Section),
                UiListItemNode(title = "有副标题和右侧文字", subtitle = "副标题写这里", trailing = "右侧"),
                UiDividerNode(),
                UiListItemNode(title = "可以点", subtitle = "actionId 非空时整行可点，点了会 toast", actionId = "row_click"),
                UiDividerNode(),
                UiListItemNode(title = "只有标题"),
            ),
        ),
    )

    private fun fieldCard(): UiNode = UiCardNode(
        UiColumnNode(
            spacingDp = 8,
            children = listOf(
                UiTextNode("输入框", style = UiTextStyle.Section),
                UiTextNode(
                    "每改一个字宿主都会把整段文本通过 onAction 回传（value 不为 null），" +
                        "值以插件为准——存下来再 invalidate()。",
                    style = UiTextStyle.Caption,
                    secondary = true,
                ),
                UiFieldNode(label = "你的名字", value = name, actionId = "name", hint = "随便写点什么"),
                UiTextNode(
                    if (name.isBlank()) "还没有输入" else "你好，$name",
                    style = UiTextStyle.Caption,
                    secondary = true,
                ),
            ),
        ),
    )

    private fun imageCard(): UiNode = UiCardNode(
        UiColumnNode(
            spacingDp = 8,
            children = listOf(
                UiTextNode("图片", style = UiTextStyle.Section),
                UiTextNode("宿主用 Coil 加载网络图片，失败时显示占位块。", style = UiTextStyle.Caption, secondary = true),
                UiImageNode(url = "https://picsum.photos/id/1025/900/420", heightDp = 150),
            ),
        ),
    )

    private fun stateCard(): UiNode = UiCardNode(
        UiColumnNode(
            spacingDp = 10,
            children = listOf(
                UiTextNode("加载 / 空状态", style = UiTextStyle.Section),
                when (state) {
                    1 -> UiLoadingNode("正在加载…")
                    2 -> UiEmptyNode("这里什么都没有", "空状态 = 标题 + 一行说明")
                    else -> UiTextNode("点下面的按钮切换这两种状态。", style = UiTextStyle.Caption, secondary = true)
                },
                UiRowNode(
                    children = listOf(
                        UiButtonNode("加载中", "loading", style = UiButtonStyle.Tonal),
                        UiButtonNode("空状态", "empty", style = UiButtonStyle.Tonal),
                        UiButtonNode("复原", "normal", style = UiButtonStyle.Plain),
                    ),
                ),
            ),
        ),
    )
}
