# Windows 构建脚本 - 编译并复制到模组资源目录
param(
    [switch]$Release
)

$ErrorActionPreference = "Stop"

$profile = if ($Release) { "release" } else { "debug" }
$profileFlag = if ($Release) { "--release" } else { "" }

Write-Host "=== 编译 Rust 原生库 ($profile) ===" -ForegroundColor Cyan
cargo build $profileFlag

if ($LASTEXITCODE -ne 0) {
    Write-Host "编译失败!" -ForegroundColor Red
    exit 1
}

$targetDir = "target/$profile"
$destDir = "../common/src/main/resources/natives/windows"

Write-Host "=== 复制到模组资源目录 ===" -ForegroundColor Cyan
Copy-Item "$targetDir/mmd_engine.dll" -Destination $destDir -Force
Write-Host "已复制: mmd_engine.dll -> $destDir" -ForegroundColor Green

Write-Host "=== 构建完成 ===" -ForegroundColor Green
