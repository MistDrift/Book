# BookStore backend starter: compile all sources, deploy to webapp, start Jetty
$ErrorActionPreference = 'Stop'
$base = Split-Path -Parent $PSScriptRoot
$other = Join-Path $base '其他'
$src = Join-Path $base '后端\java'
$javahome = 'C:\Users\Sariel\.jdks\openjdk-ea-25'
$javac = Join-Path $javahome 'bin\javac.exe'
$java = Join-Path $javahome 'bin\java.exe'
$M2 = "$env:USERPROFILE\.m2\repository"

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
& $javac -encoding UTF-8 --release 8 -nowarn -cp ($jars -join ';') -d $outDir "@$lf"
if ($LASTEXITCODE -ne 0) { Write-Host 'COMPILE FAILED'; Read-Host 'Press Enter'; exit 1 }

# ---- deploy classes into webapp WEB-INF/classes ----
$dest = Join-Path $other 'src\main\webapp\WEB-INF\classes'
New-Item -ItemType Directory -Path $dest -Force | Out-Null
Remove-Item (Join-Path $dest 'com') -Recurse -Force -ErrorAction SilentlyContinue
Copy-Item (Join-Path $outDir 'com') (Join-Path $dest 'com') -Recurse -Force
Write-Host "Deployed $((Get-ChildItem $dest -Recurse -Filter *.class | Measure-Object).Count) classes."

# ---- run ----
Write-Host 'Starting backend at http://localhost:8080 ...'
$CP = @($outDir) + $jars -join ';'
Set-Location $other
& $java -cp $CP com.example.bookstore.JettyServer
