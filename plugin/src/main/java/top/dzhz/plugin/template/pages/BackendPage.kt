package top.dzhz.plugin.template.pages

import com.xjtu.toolbox.plugin.api.PluginContext
import com.xjtu.toolbox.plugin.api.PluginPage
import com.xjtu.toolbox.plugin.api.UiButtonNode
import com.xjtu.toolbox.plugin.api.UiButtonStyle
import com.xjtu.toolbox.plugin.api.UiColumnNode
import com.xjtu.toolbox.plugin.api.UiNode
import com.xjtu.toolbox.plugin.api.UiRowNode
import com.xjtu.toolbox.plugin.api.UiTextNode
import org.json.JSONObject
import top.dzhz.plugin.template.caption
import top.dzhz.plugin.template.section

/**
 * 后端能力：向宿主借三样东西——学校取数、包内文件、发通知。
 *
 * 取数走宿主的 [PluginContext.schoolTool]：登录、MFA、WebVPN、缓存、限流都是宿主处理，
 * 插件不碰 cookie。返回的 JSON 里 `text` 是给小岱看的自然语言，`widgets` 是结构化卡片
 * （课表、成绩、空教室……），画界面用后者。
 */
internal class BackendPage(private val ctx: PluginContext) : PluginPage() {

    @Volatile
    private var busy = false

    @Volatile
    private var result = ""

    @Volatile
    private var fileInfo = "还没读"

    override fun id(): String = "backend"

    override fun title(): String = "后端能力"

    override fun render(): UiNode = UiColumnNode(
        spacingDp = 12,
        children = listOf(
            section(
                "学校数据",
                "不用自己登录：宿主带着已有的会话去取，插件的调用在后台线程",
                UiTextNode(
                    if (result.isBlank()) "点下面的按钮取一次数据" else result,
                    secondary = result.isBlank(),
                    maxLines = 12,
                ),
                UiRowNode(
                    children = listOf(
                        UiButtonNode(if (busy) "取数中…" else "查课表", "fetch_schedule", enabled = !busy),
                        UiButtonNode("查校园卡", "fetch_card", style = UiButtonStyle.Tonal, enabled = !busy),
                    ),
                ),
            ),
            section(
                "包内文件",
                "读插件包里自己的文件（清单 files[] 里声明过的那些）",
                UiTextNode(fileInfo, style = com.xjtu.toolbox.plugin.api.UiTextStyle.Caption, secondary = true),
                UiRowNode(
                    children = listOf(
                        UiButtonNode("读 hello.json", "read_file", style = UiButtonStyle.Tonal),
                    ),
                ),
            ),
            section(
                "通知",
                "走宿主统一的渠道，不用自己建 NotificationChannel；同一个插件后一条盖前一条",
                UiRowNode(
                    children = listOf(
                        UiButtonNode("发一条通知", "notify", style = UiButtonStyle.Tonal),
                    ),
                ),
            ),
            caption(
                "插件和盒子同进程运行，权限等同 App；这些接口是「方便」，不是「隔离」。",
            ),
        ),
    )

    override fun onAction(actionId: String, value: String?) {
        when (actionId) {
            "fetch_schedule" -> fetch("get_schedule")
            "fetch_card" -> fetch("get_card_info")
            "read_file" -> readFile()
            "notify" -> ctx.notify("插件模板", "这条通知来自插件，不是盒子本身。")
        }
    }

    private fun fetch(tool: String) {
        if (busy) return
        busy = true
        result = ""
        invalidate()
        // schoolTool 必须在非主线程调用
        Thread {
            val json = ctx.schoolTool(tool, "{}")
            val parsed = runCatching {
                val obj = JSONObject(json)
                val ok = obj.optBoolean("ok")
                val text = obj.optString("text")
                val widgets = obj.optJSONArray("widgets")?.length() ?: 0
                val error = obj.optString("error")
                when {
                    !ok -> "失败：$error"
                    // 工具把「没登录 / 网络不通」这类情况写在 text 里，ok 仍然是 true
                    text.startsWith("error:") -> "取数失败（多半是没登录或网络不通）：\n\n${text.take(200)}"
                    widgets > 0 -> "取到 $widgets 张结构化卡片。\n\n${text.take(200)}"
                    else -> "这个工具没有结构化卡片，只有文本：\n\n${text.take(200)}"
                }
            }.getOrElse { "返回的不是预期 JSON：${json.take(120)}" }
            result = parsed
            busy = false
            invalidate()
        }.start()
    }

    private fun readFile() {
        val bytes = ctx.openPackageFile("hello.json")
        fileInfo = if (bytes == null) {
            "包里有没 hello.json？打包时用 --extra 把文件塞进去。"
        } else {
            "读到 ${bytes.size} 字节：" + bytes.toString(Charsets.UTF_8).take(120)
        }
        invalidate()
    }
}
