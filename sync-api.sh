#!/usr/bin/env bash
# 从宿主仓库拉最新的插件 API，覆盖 plugin-api/src/main/java/.../api/。
#
# 平时不用手动跑：Gradle 构建前发现文件不在会自动拉（见 plugin-api/build.gradle.kts）。
# 想立刻刷新成最新一份，用它，或者 ./gradlew :plugin-api:syncPluginApi -PsyncApi。
#
# 插件 API 跟着宿主的 dev 分支走；等 dev 合进 main 再改成 main。
set -euo pipefail

BRANCH="${PLUGIN_API_BRANCH:-dev}"
# 国内访问 raw.githubusercontent.com 经常不通，先试 jsDelivr 镜像
MIRROR="https://cdn.jsdelivr.net/gh/xjtu-dzhz/dzhz@${BRANCH}/plugin/api/src/main/java/com/xjtu/toolbox/plugin/api"
RAW="https://raw.githubusercontent.com/xjtu-dzhz/dzhz/${BRANCH}/plugin/api/src/main/java/com/xjtu/toolbox/plugin/api"

ROOT="$(cd "$(dirname "$0")" && pwd)"
DIR="${ROOT}/plugin-api/src/main/java/com/xjtu/toolbox/plugin/api"
mkdir -p "${DIR}"

for f in PluginApi.kt PluginUi.kt; do
  echo "拉取 ${f}（分支 ${BRANCH}）"
  if ! curl -fsSL --max-time 30 "${MIRROR}/${f}" -o "${DIR}/${f}"; then
    echo "  镜像不通，改用 GitHub raw"
    curl -fsSL --max-time 30 "${RAW}/${f}" -o "${DIR}/${f}"
  fi
  test -s "${DIR}/${f}"
done

echo "完成。这两个文件是副本，不要在这里改；改动请提到盒子仓库的 plugin/api/。"
