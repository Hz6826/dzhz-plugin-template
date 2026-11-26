#!/usr/bin/env python3
"""把构建出来的插件 APK 打成盒子能装的 .zip 包。

用法：
    python3 package.py plugin/build/outputs/apk/debug/plugin-debug.apk

它会：
1. 读仓库根目录的 plugin.manifest.json（你的元数据）；
2. 算 APK 的 sha256 和大小，写进 files[]；
3. 用 ECDSA P-256 私钥（signing/plugin-ec.pem，没有就现生成）签清单；
4. 输出 <id>-<version>.zip。

私钥必须自己留着：盒子按 TOFU 记住第一包的签名者，更新时换了钥匙会被拒。
签名文件是清单**原始字节**的 SHA256withECDSA，Base64 存在 dzhz.plugin.sig。
"""
import argparse
import base64
import hashlib
import json
import pathlib
import subprocess
import sys
import zipfile

ROOT = pathlib.Path(__file__).resolve().parent
SIGNING_DIR = ROOT / "signing"
DEFAULT_KEY = SIGNING_DIR / "plugin-ec.pem"
MANIFEST_FILE = ROOT / "plugin.manifest.json"
APK_ENTRY = "plugin.apk"


def openssl(*args, stdin=None):
    return subprocess.run(["openssl", *args], input=stdin, capture_output=True, check=True).stdout


def ensure_key(path: pathlib.Path) -> None:
    if path.is_file():
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    openssl("ecparam", "-name", "prime256v1", "-genkey", "-noout", "-out", str(path))
    print(f"生成了新的签名私钥：{path}\n（一定要留着，丢了就没法给这个插件发更新）", file=sys.stderr)


def public_key_b64(key: pathlib.Path) -> str:
    der = openssl("ec", "-in", str(key), "-pubout", "-outform", "DER")
    return base64.b64encode(der).decode()


def main() -> None:
    parser = argparse.ArgumentParser(description="打 .zip 插件包")
    parser.add_argument("apk", type=pathlib.Path, help="构建出来的插件 APK")
    parser.add_argument("-o", "--out", type=pathlib.Path, help="输出路径，默认 <id>-<version>.zip")
    parser.add_argument("-k", "--key", type=pathlib.Path, default=DEFAULT_KEY, help="签名私钥（P-256）")
    parser.add_argument("--manifest", type=pathlib.Path, default=MANIFEST_FILE, help="清单模板")
    parser.add_argument("--extra", action="append", default=[],
                        help="额外的包内文件，写成 包内路径=本地路径，可以给多次（图标、头像等）")
    args = parser.parse_args()

    apk_path: pathlib.Path = args.apk
    if not apk_path.is_file():
        sys.exit(f"找不到 APK：{apk_path}")
    manifest = json.loads(args.manifest.read_text(encoding="utf-8"))
    ensure_key(args.key)

    apk_bytes = apk_path.read_bytes()
    files = [{
        "path": APK_ENTRY,
        "sha256": hashlib.sha256(apk_bytes).hexdigest(),
        "size": len(apk_bytes),
    }]

    extras = []
    for spec in args.extra:
        name, _, local = spec.partition("=")
        local_path = pathlib.Path(local or name)
        if not local_path.is_file():
            sys.exit(f"找不到额外文件：{local_path}")
        data = local_path.read_bytes()
        files.append({"path": name, "sha256": hashlib.sha256(data).hexdigest(), "size": len(data)})
        extras.append((name, data))

    manifest["files"] = files
    manifest["pubkey"] = public_key_b64(args.key)

    manifest_bytes = json.dumps(manifest, ensure_ascii=False, indent=2).encode("utf-8")
    signature = openssl("dgst", "-sha256", "-sign", str(args.key), stdin=manifest_bytes)

    out = args.out or ROOT / f"{manifest['id']}-{manifest['version']}.zip"
    out.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("dzhz.plugin.json", manifest_bytes)
        z.writestr("dzhz.plugin.sig", base64.b64encode(signature))
        z.writestr(APK_ENTRY, apk_bytes)
        for name, data in extras:
            z.writestr(name, data)

    print(f"打好：{out}（{out.stat().st_size} 字节）")
    print(f"  {manifest['id']} v{manifest['version']}（versionCode {manifest['versionCode']}）")
    print(f"  APK {len(apk_bytes)} 字节，sha256 {files[0]['sha256'][:16]}…")


if __name__ == "__main__":
    main()
