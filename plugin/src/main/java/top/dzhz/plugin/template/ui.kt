package top.dzhz.plugin.template

import com.xjtu.toolbox.plugin.api.UiCardNode
import com.xjtu.toolbox.plugin.api.UiColumnNode
import com.xjtu.toolbox.plugin.api.UiNode
import com.xjtu.toolbox.plugin.api.UiTextNode
import com.xjtu.toolbox.plugin.api.UiTextStyle

/**
 * 一张卡片：标题 + 一句可选的用法提示 + 内容。
 *
 * 所有页面都用它拼，省掉重复的那几行 Column/Text。提示只写"用法要点"，
 * 不写百科——要讲原理就写代码注释里。
 */
internal fun section(
    title: String,
    hint: String = "",
    vararg children: UiNode,
): UiNode = UiCardNode(
    UiColumnNode(
        spacingDp = 10,
        children = buildList {
            add(UiTextNode(title, style = UiTextStyle.Section))
            if (hint.isNotBlank()) {
                add(UiTextNode(hint, style = UiTextStyle.Caption, secondary = true))
            }
            addAll(children)
        },
    ),
)

/** 一句话说明，次要色。 */
internal fun caption(text: String): UiNode = UiTextNode(text, style = UiTextStyle.Caption, secondary = true)
