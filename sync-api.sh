#!/usr/bin/env bash
# 从宿主仓库拉最新的插件 API，覆盖 plugin-api/src/main/java/.../api/。
#
# 平时不用手动跑：Gradle 构建前发现文件不在会自动拉（见 plugin-api/build.gradle.kts）。
# 想立刻刷新成最新一份，用它，或者 ./gradlew :plugin-api:syncPluginApi -PsyncApi。
#
# 宿主那边合并到 main 之后，把 BRANCH 改成 main。
set -euo pipefail

BRANCH="${PLUGIN_API_BRANCH:-feat/plugin-zip}"
BASE="https://raw.githubusercontent.com/yeliqin666/xjtu-toolbox-android/${BRANCH}/plugin/api/src/main/java/com/xjtu/toolbox/plugin/api"

ROOT="$(cd "$(dirname "$0")" && pwd)"
DIR="${ROOT}/plugin-api/src/main/java/com/xjtu/toolbox/plugin/api"
mkdir -p "${DIR}"

for f in PluginApi.kt PluginUi.kt; do
  echo "拉取 ${f}（分支 ${BRANCH}）"
  curl -fsSL "${BASE}/${f}" -o "${DIR}/${f}"
  test -s "${DIR}/${f}"
done

echo "完成。这两个文件是副本，不要在这里改；改动请提到盒子仓库的 plugin/api/。"
