# 岱宗盒子插件模板

一个能直接跑起来的插件骨架：一个页面把所有宿主支持的界面组件都摆了一遍，照着改就是你的插件。

- **不依赖 Compose**：界面用宿主给的 `UiNode` 描述，盒子自己用 miuix 渲染 —— 深色模式、主题色、字阶、圆角、动效自动跟盒子一致。
- **一个小 APK**：整个插件约 2–3 MB，没有 Compose / lifecycle / 协程。
- **包格式是 zip**：`dzhz.plugin.json` + `dzhz.plugin.sig` + 你的 APK，由 `package.py` 生成并签名。

## 目录

```
plugin-api/       宿主 API 的副本（PluginApi.kt / PluginUi.kt），不要改
plugin/           你的插件：TemplatePlugin / TemplatePage / TemplateTool
plugin.manifest.json   清单模板（id、名字、页面、工具、版本号）
package.py        打 zip 包 + 签名
signing/          签名私钥（首次打包自动生成，不要提交）
```

## 三步

**1. 改身份**。`plugin.manifest.json` 里的 `id`（小写字母开头，2–32 位，能带 `.` `_`）、`name`（1–14 字）、`author`；
`plugin/build.gradle.kts` 里的 `namespace` / `applicationId`；
`plugin/src/main/AndroidManifest.xml` 的 `android:label`；
包名（`top.dzhz.plugin.template`）改了就同步改源码目录和 `dex.entryClass`。

**2. 写页面**。`TemplatePage.render()` 返回一棵 `UiNode` 树。状态放字段里，改完调 `invalidate()`；用户操作从 `onAction(actionId, value)` 回来。

**3. 构建打包**。

```bash
./gradlew :plugin:assembleDebug
python3 package.py plugin/build/outputs/apk/debug/plugin-debug.apk
```

产物是一个 zip。装进盒子：**插件 → 右上角设置 → 从文件安装**。

## 哪些页面能出现在首页 / 搜索里

`pages[]` 的每一项可以带 `"home": true`：标了的页面会进盒子的**服务目录**——首页最后那一组「插件」、首页编辑态的「添加卡片」面板、全局搜索都能找到它，点开直接是这一页。

- 没标的页面只能在插件内部从侧栏切换，别把「关于」「设置」这种也标上；
- 首页卡片是**小字插件名 + 大字页面名**，所以 `title` 要写清这一页是干什么的（「文字与样式」比「页一」好）；
- 第一次遇到带 `home` 的页面时，盒子会给首页补一组「插件」；之后加不加、放哪儿，都是用户自己在首页编辑态里定；
- web 插件只有一个网页，它天然就是公开页，不用标。

## 能用的组件

| 节点 | 作用 |
|---|---|
| `UiColumnNode(children, spacingDp)` | 竖排 |
| `UiRowNode(children, spacingDp)` | 横排，子节点垂直居中 |
| `UiTextNode(text, style, secondary, maxLines)` | 文字，`style` 有 Display / Title / Section / Body / Caption |
| `UiCardNode(child)` | 卡片，里面放任意节点 |
| `UiButtonNode(label, actionId, style, enabled)` | 按钮，三种外观 Filled / Tonal / Plain |
| `UiListItemNode(title, subtitle, trailing, actionId)` | 设置页那种行 |
| `UiFieldNode(label, value, actionId, hint)` | 输入框，每改一个字回传整段文本 |
| `UiImageNode(url, heightDp)` | 网络图片（宿主用 Coil 加载） |
| `UiDividerNode()` | 分隔线 |
| `UiSpacerNode(heightDp)` | 留白 |
| `UiLoadingNode(text)` | 加载中 |
| `UiEmptyNode(title, subtitle)` | 空状态 |

工具（给小岱调用）：清单里 `dex.tools[].definition` 是给模型的 JSON schema，代码里 `PluginTool.execute(argsJson)` 返回一行文本。

## 签名与更新

盒子对插件用 TOFU：**第一包装进来时记住签名者**（公钥 SPKI 的 sha256），之后同一个 `id` 的更新必须用同一把私钥签、`versionCode` 更大，否则拒装。

所以：

- `signing/plugin-ec.pem` 是你的身份，**别弄丢**，也别提交到仓库（`.gitignore` 已经挡了）；
- 换机器就把这把钥匙拷过去；
- 想换钥匙只能让用户先卸载再重装。

## plugin-api 是副本（不在本仓库里）

`plugin-api/src/main/java/.../api/` 下的 `PluginApi.kt` 和 `PluginUi.kt` **不在本仓库里**（`.gitignore` 挡了），它们是构建时从盒子仓库拉的副本：

- 平时不用管：`./gradlew :plugin:assembleDebug` 之前会自动补上缺的文件；
- 想立刻刷新到最新一份：`./sync-api.sh`，或者 `./gradlew :plugin-api:syncPluginApi -PsyncApi`；
- **不要在这两个文件里改东西**——宿主加了节点或字段，请去改盒子仓库的 `plugin/api/`，这边重新拉就同步了。字段只增不改，旧插件不会被弄坏。

## 注意

- 插件和盒子同进程、同 uid 运行，**权限等同 App**，能读到登录会话和本地数据。只装你信任来源的插件。
- 插件页在主线程 `render()`，别在里面做网络请求或读大文件；数据提前取好放字段里。
- 断网时 `UiImageNode` 会显示占位块，这是正常的。
