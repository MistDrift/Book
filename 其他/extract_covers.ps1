# 从WebFetch输出文件提取封面并下载
$ErrorActionPreference = 'Continue'
$base = "F:\信息系统开发\王子怡 姚逸雪 缪丽娟 韩鼎明 李安娜  郝全书\王子怡 姚逸雪 缪丽娟 韩鼎明 李安娜  郝全书"
$uploadDir = Join-Path $base '其他\src\main\webapp\upload'
$outDir = "c:\Users\Sariel\.trae-cn\work\.trae\toolcall-output"
$ua = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36'

# 读取所有输出文件，提取 subjectId -> coverCode
$coverMap = @{}
$files = Get-ChildItem -Path $outDir -Filter "call_*.txt" | Sort-Object LastWriteTime
Write-Host "找到 $($files.Count) 个输出文件"
foreach ($f in $files) {
    $content = Get-Content $f.FullName -Raw -Encoding UTF8
    # 匹配: aka.doubaocdn.com/s/CODE ... subject/SUBJECTID
    $ms = [regex]::Matches($content, 'aka\.doubaocdn\.com/s/(\w+).*?subject/(\d+)', [System.Text.RegularExpressions.RegexOptions]::Singleline)
    foreach ($m in $ms) {
        $code = $m.Groups[1].Value
        $sid = $m.Groups[2].Value
        if (-not $coverMap.ContainsKey($sid)) { $coverMap[$sid] = $code }
    }
}
Write-Host "提取到 $($coverMap.Count) 个封面映射"

# 读取进度
$pf = Join-Path $base 'fetch_progress.json'
$saved = Get-Content $pf -Raw -Encoding UTF8 | ConvertFrom-Json
$books = @($saved.books)
Write-Host "进度文件中 $($books.Count) 本书"

# 下载封面
$ok = 0; $fail = 0; $noMap = 0
foreach ($b in $books) {
    $sid = $b.subjectId
    $code = $coverMap[$sid]
    if (-not $code) { $noMap++; continue }
    $coverName = "book$($b.id).jpg"
    $coverPath = Join-Path $uploadDir $coverName
    $url = "https://aka.doubaocdn.com/s/$code"
    $success = $false
    try {
        Invoke-WebRequest -Uri $url -OutFile $coverPath -UseBasicParsing -TimeoutSec 20 -Headers @{'User-Agent'=$ua; 'Referer'='https://book.douban.com/'}
        $len = (Get-Item $coverPath).Length
        if ($len -gt 2000) { $success = $true }
    } catch { }
    if ($success) {
        $b.image = "/upload/$coverName"
        $ok++
    } else { $fail++ }
}
Write-Host "下载完成: 成功$ok 失败$fail 无映射$noMap"

# 保存进度并生成SQL
$saved.books = $books
$saved | ConvertTo-Json -Depth 5 | Out-File -FilePath $pf -Encoding UTF8
$sqlFile = Join-Path $base 'insert_books.sql'
$sql = New-Object System.Collections.Generic.List[string]
$sql.Add("USE db_book;")
foreach ($b in $books) {
    $t = $b.title -replace "'", "''"
    $a = $b.author -replace "'", "''"
    $p = $b.publisher -replace "'", "''"
    $img = if ($b.image) { "'$($b.image)'" } else { 'NULL' }
    $sql.Add("INSERT INTO tb_book (isbn,title,author,publisher,publish_date,category,price,stock,image,coll_status) VALUES ('$($b.isbn)','$t','$a','$p','$($b.publishDate)','$($b.category)',$($b.price),$($b.stock),$img,1);")
}
[IO.File]::WriteAllLines($sqlFile, $sql, (New-Object System.Text.UTF8Encoding($false)))
Write-Host "SQL生成: $sqlFile"

# 统计无封面的书
$noCover = $books | Where-Object { -not $_.image }
Write-Host "`n无封面的书 ($($noCover.Count)):"
$noCover | Select-Object id, category, title | Format-Table -AutoSize