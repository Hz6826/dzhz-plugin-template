package top.dzhz.plugin.template

import com.xjtu.toolbox.plugin.api.PluginContext
import com.xjtu.toolbox.plugin.api.PluginPage
import com.xjtu.toolbox.plugin.api.UiBadgeNode
import com.xjtu.toolbox.plugin.api.UiBadgeTone
import com.xjtu.toolbox.plugin.api.UiButtonNode
import com.xjtu.toolbox.plugin.api.UiButtonStyle
import com.xjtu.toolbox.plugin.api.UiCardNode
import com.xjtu.toolbox.plugin.api.UiColumnNode
import com.xjtu.toolbox.plugin.api.UiDividerNode
import com.xjtu.toolbox.plugin.api.UiEmptyNode
import com.xjtu.toolbox.plugin.api.UiFieldNode
import com.xjtu.toolbox.plugin.api.UiGridNode
import com.xjtu.toolbox.plugin.api.UiIconNode
import com.xjtu.toolbox.plugin.api.UiImageNode
import com.xjtu.toolbox.plugin.api.UiKeyValueNode
import com.xjtu.toolbox.plugin.api.UiListItemNode
import com.xjtu.toolbox.plugin.api.UiLoadingNode
import com.xjtu.toolbox.plugin.api.UiNode
import com.xjtu.toolbox.plugin.api.UiOption
import com.xjtu.toolbox.plugin.api.UiProgressNode
import com.xjtu.toolbox.plugin.api.UiRichTextNode
import com.xjtu.toolbox.plugin.api.UiRowNode
import com.xjtu.toolbox.plugin.api.UiSelectNode
import com.xjtu.toolbox.plugin.api.UiSpacerNode
import com.xjtu.toolbox.plugin.api.UiSwitchNode
import com.xjtu.toolbox.plugin.api.UiTextColor
import com.xjtu.toolbox.plugin.api.UiTextNode
import com.xjtu.toolbox.plugin.api.UiTextSpan
import com.xjtu.toolbox.plugin.api.UiTextStyle

/**
 * 模板页：把宿主支持的所有 [UiNode] 都摆一遍，照着抄就行。
 *
 * 一个类同时是好几个页面（清单里 `pages[]` 的每一项）——宿主按 id 打开对应实例，
 * 侧栏目录也从清单生成，插件不用写任何导航代码。
 *
 * 写法要点：
 * - 状态放在字段里，`@Volatile` 因为宿主可能在别的线程拿；
 * - 改完字段调一次 `invalidate()`，宿主会重新要一次 [render]；
 * - [render] 在主线程被调用，别在里面做 IO。
 */
internal class TemplatePage(
    private val ctx: PluginContext,
    private val pageId: String = "home",
) : PluginPage() {

    @Volatile
    private var count = 0

    @Volatile
    private var name = ""

    /** 0 = 什么都不显示，1 = 加载中，2 = 空状态。 */
    @Volatile
    private var state = 0

    /** 下拉选中的 key。 */
    @Volatile
    private var term = "2026-2027-1"

    @Volatile
    private var reminder = true

    override fun id(): String = pageId

    override fun title(): String = when (pageId) {
        "text" -> "文字与样式"
        "data" -> "数据与图标"
        "about" -> "关于"
        else -> "组件一览"
    }

    override fun render(): UiNode = when (pageId) {
        "text" -> textPage()
        "data" -> dataPage()
        "about" -> aboutPage()
        else -> homePage()
    }

    override fun onAction(actionId: String, value: String?) {
        when (actionId) {
            "count" -> count++
            "reset" -> count = 0
            "name" -> name = value.orEmpty()
            "row_click" -> ctx.toast("列表项被点了")
            "text_click" -> ctx.toast("可点击文本被点了")
            "rich_click" -> ctx.toast("富文本里的链接被点了")
            "term" -> term = value.orEmpty()
            "reminder" -> reminder = value == "true"
            else -> ctx.log("未处理的动作：$actionId")
        }
        // 改完状态必须 invalidate，宿主才会重新要一次 render()
        invalidate()
    }

    // ── 组件一览 ────────────────────────────────────────────────────────────

    private fun homePage(): UiNode = UiColumnNode(
        spacingDp = 12,
        children = listOf(
            UiTextNode(
                "这一页把常用的节点摆了一遍；文字样式、图标和数据类节点在左边目录的另外两页。",
                secondary = true,
            ),
            textCard(),
            buttonCard(),
            listCard(),
            fieldCard(),
            selectCard(),
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
                UiDividerNode(),
                // 富文本：一段话里混样式和链接，段与段之间没有间距
                UiRichTextNode(
                    spans = listOf(
                        UiTextSpan("这一段是混排："),
                        UiTextSpan("加粗", bold = true),
                        UiTextSpan("、"),
                        UiTextSpan("斜体", italic = true),
                        UiTextSpan("、"),
                        UiTextSpan("下划线", underline = true),
                        UiTextSpan("、"),
                        UiTextSpan("删除线", strikeThrough = true),
                        UiTextSpan("，还能嵌一个 "),
                        UiTextSpan("点我", color = UiTextColor.Primary, underline = true, actionId = "rich_click"),
                        UiTextSpan(" 这样的链接。"),
                    ),
                ),
                UiTextNode(
                    "这段文字整段可点（actionId），不用做成按钮。",
                    color = UiTextColor.Primary,
                    actionId = "text_click",
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
                    "Filled / Tonal 两种外观，enabled = false 置灰；四个按钮放一排。" +
                        "更轻的操作用可点击文本，别都做成按钮。",
                    style = UiTextStyle.Caption,
                    secondary = true,
                ),
                UiRowNode(
                    spacingDp = 8,
                    children = listOf(
                        UiButtonNode("主要", "count"),
                        UiButtonNode("次要", "count", style = UiButtonStyle.Tonal),
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

    private fun selectCard(): UiNode {
        val options = listOf(
            UiOption("2026-2027-1", "2026–2027 学年 第一学期"),
            UiOption("2025-2026-2", "2025–2026 学年 第二学期"),
            UiOption("2025-2026-1", "2025–2026 学年 第一学期"),
        )
        val current = options.firstOrNull { it.key == term }?.label
        return UiCardNode(
            UiColumnNode(
                spacingDp = 10,
                children = listOf(
                    UiTextNode("下拉单选", style = UiTextStyle.Section),
                    UiTextNode(
                        "最多 8 个选项，再多就该自己开一页；选中的 key 通过 onAction 回来。",
                        style = UiTextStyle.Caption,
                        secondary = true,
                    ),
                    UiSelectNode(label = "学期", value = term, options = options, actionId = "term"),
                    UiTextNode(
                        if (current == null) "当前：$term" else "当前：$current",
                        style = UiTextStyle.Caption,
                        secondary = true,
                    ),
                ),
            ),
        )
    }

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

    // ── 文字与样式 ──────────────────────────────────────────────────────────

    private fun textPage(): UiNode = UiColumnNode(
        spacingDp = 12,
        children = listOf(
            UiCardNode(
                UiColumnNode(
                    spacingDp = 8,
                    children = listOf(
                        UiTextNode("颜色", style = UiTextStyle.Section),
                        UiTextNode("Default：跟主题走", color = UiTextColor.Default),
                        UiTextNode("Secondary：次要信息", color = UiTextColor.Secondary),
                        UiTextNode("Primary：主色", color = UiTextColor.Primary),
                        UiTextNode("Success：成功", color = UiTextColor.Success),
                        UiTextNode("Warning：警告", color = UiTextColor.Warning),
                        UiTextNode("Error：错误", color = UiTextColor.Error),
                        UiTextNode(
                            "argb 直接给颜色（0xFFFF8800），深浅色要插件自己适配。",
                            argb = 0xFFFF8800.toInt(),
                            style = UiTextStyle.Caption,
                        ),
                    ),
                ),
            ),
            UiCardNode(
                UiColumnNode(
                    spacingDp = 8,
                    children = listOf(
                        UiTextNode("样式", style = UiTextStyle.Section),
                        UiTextNode("加粗 bold = true", bold = true),
                        UiTextNode("斜体 italic = true", italic = true),
                        UiTextNode("下划线 underline = true", underline = true),
                        UiTextNode("删除线 strikeThrough = true", strikeThrough = true),
                        UiTextNode("几个可以叠在一起", bold = true, italic = true, underline = true),
                    ),
                ),
            ),
            UiCardNode(
                UiColumnNode(
                    spacingDp = 8,
                    children = listOf(
                        UiTextNode("可点击文本", style = UiTextStyle.Section),
                        UiTextNode(
                            "actionId 非空时整段可点，比按钮轻；适合「查看全部 ›」这种。",
                            style = UiTextStyle.Caption,
                            secondary = true,
                        ),
                        UiTextNode("点我一下", color = UiTextColor.Primary, actionId = "text_click"),
                    ),
                ),
            ),
            UiCardNode(
                UiColumnNode(
                    spacingDp = 8,
                    children = listOf(
                        UiTextNode("富文本混排", style = UiTextStyle.Section),
                        UiTextNode(
                            "同一段话里混不同样式和链接，段与段之间没有间距。",
                            style = UiTextStyle.Caption,
                            secondary = true,
                        ),
                        UiRichTextNode(
                            spans = listOf(
                                UiTextSpan("成绩已更新："),
                                UiTextSpan("高等数学 92", bold = true),
                                UiTextSpan("、"),
                                UiTextSpan("线性代数 88", bold = true),
                                UiTextSpan("，"),
                                UiTextSpan("查看详情", color = UiTextColor.Primary, underline = true, actionId = "rich_click"),
                                UiTextSpan("。"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )

    // ── 数据与图标 ──────────────────────────────────────────────────────────

    private val iconNames = listOf(
        "calendar" to "课表",
        "grade" to "成绩",
        "book" to "图书馆",
        "card" to "校园卡",
        "bell" to "通知",
        "school" to "教务",
        "exam" to "考试",
        "task" to "作业",
        "classroom" to "教室",
        "seat" to "座位",
        "canteen" to "食堂",
        "bus" to "校车",
        "doc" to "文档",
        "download" to "下载",
        "upload" to "上传",
        "user" to "用户",
        "time" to "时间",
        "star" to "收藏",
        "warning" to "警告",
        "check" to "完成",
        // 不在白名单里的名字：宿主画灰色占位图标，不崩
        "not_in_list" to "未知",
    )

    private fun dataPage(): UiNode = UiColumnNode(
        spacingDp = 12,
        children = listOf(
            UiCardNode(
                UiColumnNode(
                    spacingDp = 10,
                    children = listOf(
                        UiTextNode("图标（${iconNames.size} 个）", style = UiTextStyle.Section),
                        UiTextNode(
                            "名字取自宿主白名单，语义名，不是图标库的类名；不在名单里的画灰色占位。",
                            style = UiTextStyle.Caption,
                            secondary = true,
                        ),
                        UiGridNode(
                            columns = 3,
                            children = iconNames.map { (name, label) -> iconItem(name, label) },
                        ),
                    ),
                ),
            ),
            UiCardNode(
                UiColumnNode(
                    spacingDp = 6,
                    children = listOf(
                        UiTextNode("键值行", style = UiTextStyle.Section),
                        UiKeyValueNode("学号", "2023012345"),
                        UiKeyValueNode("本学期学分", "23.5"),
                        UiKeyValueNode("绩点", "3.82", secondary = true),
                        UiKeyValueNode("校园卡余额", "¥ 86.40"),
                    ),
                ),
            ),
            UiCardNode(
                UiColumnNode(
                    spacingDp = 10,
                    children = listOf(
                        UiTextNode("进度条", style = UiTextStyle.Section),
                        UiProgressNode(value = 0.68f, label = "借阅额度 13/20"),
                        UiProgressNode(value = 0.24f, label = "本月消费 ¥24 / ¥100"),
                        UiProgressNode(value = 1f, label = "已完成"),
                    ),
                ),
            ),
            UiCardNode(
                UiColumnNode(
                    spacingDp = 10,
                    children = listOf(
                        UiTextNode("徽章", style = UiTextStyle.Section),
                        UiRowNode(
                            children = listOf(
                                UiBadgeNode("可预约"),
                                UiBadgeNode("已满", UiBadgeTone.Warning),
                                UiBadgeNode("已选", UiBadgeTone.Accent),
                                UiBadgeNode("待缴费", UiBadgeTone.Neutral),
                            ),
                        ),
                    ),
                ),
            ),
            UiCardNode(
                UiColumnNode(
                    spacingDp = 10,
                    children = listOf(
                        UiTextNode("开关行", style = UiTextStyle.Section),
                        UiSwitchNode(
                            label = "每天提醒",
                            summary = "早上八点推一条当天的课",
                            checked = reminder,
                            actionId = "reminder",
                        ),
                    ),
                ),
            ),
            UiCardNode(
                UiColumnNode(
                    spacingDp = 8,
                    children = listOf(
                        UiTextNode("可点的卡片", style = UiTextStyle.Section),
                        UiTextNode("整张卡可点，按下有反馈；比塞一个按钮自然。", style = UiTextStyle.Caption, secondary = true),
                    ),
                ),
                actionId = "card_click",
            ),
        ),
    )

    private fun iconItem(name: String, label: String): UiNode = UiColumnNode(
        spacingDp = 6,
        children = listOf(
            UiIconNode(name),
            UiTextNode(label, style = UiTextStyle.Caption, secondary = true),
        ),
    )

    // ── 关于 ────────────────────────────────────────────────────────────────

    private fun aboutPage(): UiNode = UiColumnNode(
        spacingDp = 12,
        children = listOf(
            UiTextNode(
                "这是岱宗盒子的插件模板。左边目录里的每一页都是清单 `pages[]` 里的一项，" +
                    "插件只负责返回界面树，导航、顶栏、主题都交给宿主。",
                secondary = true,
            ),
            UiCardNode(
                UiColumnNode(
                    spacingDp = 8,
                    children = listOf(
                        UiTextNode("怎么改成你自己的", style = UiTextStyle.Section),
                        UiListItemNode(title = "1. 改 plugin.manifest.json", subtitle = "id、名字、作者、版本号"),
                        UiListItemNode(title = "2. 改 plugin/build.gradle.kts", subtitle = "namespace 和 applicationId"),
                        UiListItemNode(title = "3. 写页面", subtitle = "TemplatePage.render() 返回 UiNode 树"),
                        UiListItemNode(title = "4. 打包", subtitle = "python3 package.py <apk>"),
                    ),
                ),
            ),
            UiTextNode(
                "插件 id ${ctx.pluginId} · App ${ctx.appVersionCode}",
                style = UiTextStyle.Caption,
                secondary = true,
            ),
        ),
    )
}
