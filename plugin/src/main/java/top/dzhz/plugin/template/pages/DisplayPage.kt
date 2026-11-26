package top.dzhz.plugin.template.pages

import com.xjtu.toolbox.plugin.api.PluginContext
import com.xjtu.toolbox.plugin.api.PluginPage
import com.xjtu.toolbox.plugin.api.UiBadgeNode
import com.xjtu.toolbox.plugin.api.UiBadgeTone
import com.xjtu.toolbox.plugin.api.UiColumnNode
import com.xjtu.toolbox.plugin.api.UiGridNode
import com.xjtu.toolbox.plugin.api.UiIconNode
import com.xjtu.toolbox.plugin.api.UiImageNode
import com.xjtu.toolbox.plugin.api.UiKeyValueNode
import com.xjtu.toolbox.plugin.api.UiListItemNode
import com.xjtu.toolbox.plugin.api.UiNode
import com.xjtu.toolbox.plugin.api.UiProgressNode
import com.xjtu.toolbox.plugin.api.UiRowNode
import com.xjtu.toolbox.plugin.api.UiTextNode
import com.xjtu.toolbox.plugin.api.UiTextStyle
import top.dzhz.plugin.template.caption
import top.dzhz.plugin.template.section

/** 数据展示：列表项、键值行、进度、徽章、图片、图标。 */
internal class DisplayPage(private val ctx: PluginContext) : PluginPage() {

    override fun id(): String = "display"

    override fun title(): String = "数据展示"

    /** 宿主白名单里的全部 20 个名字，外加一个不认识的名字（渲染成灰色占位）。 */
    private val icons = listOf(
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
        "not_in_list" to "未知",
    )

    override fun render(): UiNode = UiColumnNode(
        spacingDp = 12,
        children = listOf(
            section(
                "列表项",
                "标题 + 可选副标题 + 可选右侧文字；actionId 非空时整行可点",
                UiListItemNode(title = "有副标题和右侧文字", subtitle = "副标题写这里", trailing = "右侧"),
                UiListItemNode(title = "可以点", subtitle = "点了会 toast", actionId = "row_click"),
                UiListItemNode(title = "只有标题"),
            ),
            section(
                "键值行",
                "左边标签右边值，成绩、余额一行一条",
                UiKeyValueNode("学号", "2023012345"),
                UiKeyValueNode("本学期学分", "23.5"),
                UiKeyValueNode("绩点", "3.82", secondary = true),
                UiKeyValueNode("校园卡余额", "¥ 86.40"),
            ),
            section(
                "进度条",
                "value 夹到 0..1；label 可省",
                UiProgressNode(value = 0.68f, label = "借阅额度 13/20"),
                UiProgressNode(value = 0.24f, label = "本月消费 ¥24 / ¥100"),
                UiProgressNode(value = 1f, label = "已完成"),
            ),
            section(
                "徽章",
                "三种色调：中性 / 主色 / 警告",
                UiRowNode(
                    children = listOf(
                        UiBadgeNode("可预约"),
                        UiBadgeNode("已选", UiBadgeTone.Accent),
                        UiBadgeNode("已满", UiBadgeTone.Warning),
                    ),
                ),
            ),
            section(
                "图片",
                "宿主用 Coil 加载，断网时显示占位块",
                UiImageNode(url = "https://picsum.photos/id/1025/900/420", heightDp = 140),
            ),
            section(
                "图标（${icons.size} 个）",
                "名字是宿主白名单里的语义名；不在名单里的画灰色占位（最后那个）",
                UiGridNode(
                    columns = 3,
                    children = icons.map { (name, label) ->
                        UiColumnNode(
                            spacingDp = 6,
                            children = listOf(
                                UiIconNode(name),
                                UiTextNode(label, style = UiTextStyle.Caption, secondary = true),
                            ),
                        )
                    },
                ),
            ),
            caption("完整名单见宿主 PluginUiRenderer.kt 里的 PluginIconSet。"),
        ),
    )

    override fun onAction(actionId: String, value: String?) {
        if (actionId == "row_click") ctx.toast("列表项被点了")
    }
}
