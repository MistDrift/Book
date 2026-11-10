# BookStore backend starter: compile all sources, deploy to webapp, start Jetty
$ErrorActionPreference = 'Stop'
$base = Split-Path -Parent $PSScriptRoot
$other = Join-Path $base '其他'
$src = Join-Path $base '后端\java'
$javahome = 'C:\Users\Sariel\.jdks\openjdk-ea-25'
$javac = Join-Path $javahome 'bin\javac.exe'
$java = Join-Path $javahome 'bin\java.exe'
$M2 = "$env:USERPROFILE\.m2\repository"

# javac / java 的日志（尤其 Jetty）全部输出到 stderr。直接用 & 调用时 PowerShell 会把每一行
# stderr 变成 ErrorRecord，在 $ErrorActionPreference='Stop' 下就升级成终止性错误，脚本会在
# 启动途中直接退出（输出被重定向时必现）。这里把 stderr 并进管道再按普通文本打印，
# 既保留实时日志，也能正常拿到退出码。
function Invoke-Native {
    param([string]$FilePath, [string[]]$Arguments)
    $prev = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        & $FilePath @Arguments 2>&1 | ForEach-Object { Write-Host $_ }
        return $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $prev
    }
}

# ---- collect dependency jars ----
$dirs = @('javax\servlet\javax.servlet-api','mysql\mysql-connector-java','com\fasterxml\jackson\core\jackson-databind','com\fasterxml\jackson\core\jackson-core','com\fasterxml\jackson\core\jackson-annotations','org\eclipse\jetty','javax\annotation\javax.annotation-api','com\google\protobuf')
$jars = foreach ($d in $dirs) { Get-ChildItem (Join-Path $M2 $d) -Recurse -Filter *.jar -ErrorAction SilentlyContinue | Where-Object { $_.Name -notmatch 'sources|javadoc' } | ForEach-Object FullName }
$jars += @("$M2\org\ow2\asm\asm\9.9.1\asm-9.9.1.jar","$M2\org\ow2\asm\asm-commons\9.9.1\asm-commons-9.9.1.jar","$M2\org\ow2\asm\asm-tree\9.9.1\asm-tree-9.9.1.jar")

# ---- compile all sources (argfile with forward slashes, UTF-8) ----
Write-Host 'Compiling backend sources...'
$list = Get-ChildItem $src -Recurse -Filter *.java | ForEach-Object { '"' + ($_.FullName -replace '\\','/') + '"' }
$lf = Join-Path $env:TEMP 'bstore_src.txt'
[IO.File]::WriteAllLines($lf, $list, (New-Object System.Text.UTF8Encoding($false)))
$outDir = Join-Path $other 'target\classes'
Remove-Item (Join-Path $outDir 'com') -Recurse -Force -ErrorAction SilentlyContinue
$compileExit = Invoke-Native $javac @('-encoding','UTF-8','--release','8','-nowarn','-cp',($jars -join ';'),'-d',$outDir,"@$lf")
if ($compileExit -ne 0) { Write-Host 'COMPILE FAILED'; Read-Host 'Press Enter'; exit 1 }

# ---- deploy classes into webapp WEB-INF/classes ----
# 先尽量清掉上一轮的 class；再拷 outDir 的“内容”。拷内容而不是 com 目录本身，
# 是为了避免旧 com 没删干净时被 Copy-Item 又套一层，反复运行会攒出 com\com\com。
$dest = Join-Path $other 'src\main\webapp\WEB-INF\classes'
New-Item -ItemType Directory -Path $dest -Force | Out-Null
Get-ChildItem $dest -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
Copy-Item (Join-Path $outDir '*') $dest -Recurse -Force
Write-Host "Deployed $((Get-ChildItem $dest -Recurse -Filter *.class | Measure-Object).Count) classes."

# ---- run ----
Write-Host 'Starting backend at http://localhost:8080 ...'
$CP = (@($outDir) + $jars) -join ';'
Set-Location $other
$runExit = Invoke-Native $java @('-cp',$CP,'com.example.bookstore.JettyServer')
Write-Host "Backend stopped (exit code $runExit)."
