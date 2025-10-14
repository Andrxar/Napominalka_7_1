Param(
    [switch]$Release
)

$ErrorActionPreference = 'Stop'

function Invoke-Docker {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Args
    )
    Write-Host "docker $($Args -join ' ')" -ForegroundColor Cyan
    & docker @Args
    if ($LASTEXITCODE -ne 0) {
        throw "❌ Ошибка при выполнении docker-команды: $($Args -join ' ')"
    }
}

function Ensure-DockerInstalled {
    try {
        Invoke-Docker -Args @('version') | Out-Null
    } catch {
        Write-Error "❌ Docker не установлен или недоступен. Установите Docker Desktop и включите WSL2."
        exit 1
    }
}

function Build-Image {
    $image = 'napominalka-android'
    Write-Host "📦 Собирается Docker-образ $image ..." -ForegroundColor Yellow
    Invoke-Docker -Args @('build', '-t', $image, '.')
    return $image
}

function Ensure-Wrapper {
    param([string]$Image)
    $proj = (Get-Location).Path
    $cmd = 'if [ ! -f gradlew ]; then gradle wrapper; fi'
    Invoke-Docker -Args @(
        'run', '--rm',
        '-v', "${proj}:/workspace",
        '-w', '/workspace',
        $Image,
        'bash', '-lc', $cmd
    )
}

function Build-APK {
    param([string]$Image, [bool]$IsRelease)
    $proj = (Get-Location).Path
    $task = ':app:assembleDebug'
    if ($IsRelease) { $task = ':app:assembleRelease' }
    $cmd = "./gradlew $task"

    Write-Host "🏗️  Начинается сборка APK..." -ForegroundColor Yellow

    Invoke-Docker -Args @(
        'run', '--rm',
        '-v', "${proj}:/workspace",
        '-w', '/workspace',
        $Image,
        'bash', '-lc', $cmd
    )

    if ($IsRelease) {
        Write-Host "✅ Готово: app\build\outputs\apk\release\app-release-unsigned.apk" -ForegroundColor Green
    } else {
        Write-Host "✅ Готово: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Green
    }
}

# --- MAIN ---
Ensure-DockerInstalled
$image = Build-Image
Ensure-Wrapper -Image $image
Build-APK -Image $image -IsRelease:$Release.IsPresent