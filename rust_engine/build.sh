#!/bin/bash
# Linux/macOS 构建脚本 - 编译并复制到模组资源目录

set -e

RELEASE=""
PROFILE="debug"

if [[ "$1" == "--release" || "$1" == "-r" ]]; then
    RELEASE="--release"
    PROFILE="release"
fi

echo "=== 编译 Rust 原生库 ($PROFILE) ==="
cargo build $RELEASE

TARGET_DIR="target/$PROFILE"

# 检测操作系统
if [[ "$OSTYPE" == "darwin"* ]]; then
    DEST_DIR="../common/src/main/resources/natives/macos"
    LIB_NAME="libmmd_engine.dylib"
else
    DEST_DIR="../common/src/main/resources/natives/linux"
    LIB_NAME="libmmd_engine.so"
fi

echo "=== 复制到模组资源目录 ==="
mkdir -p "$DEST_DIR"
cp "$TARGET_DIR/$LIB_NAME" "$DEST_DIR/"
echo "已复制: $LIB_NAME -> $DEST_DIR"

echo "=== 构建完成 ==="
