# 下载10本艺术类封面并更新进度
$base = "F:\信息系统开发\王子怡 姚逸雪 缪丽娟 韩鼎明 李安娜  郝全书\王子怡 姚逸雪 缪丽娟 韩鼎明 李安娜  郝全书"
$uploadDir = Join-Path $base '其他\src\main\webapp\upload'
$ua = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36'

# subjectId -> coverCode
$artCovers = @{
    '24686283'='3Q9syn5pWy'; '10423700'='ZcKiyumc4n'; '26360519'='FVwSsW3UTf'
    '36330431'='bVjZObLbHI'; '4713415'='zMEVeNn0P5'; '25806782'='zx5LWr4EpH'
    '3086379'='db66E0yglj'; '2057624'='fKXwtDU9bn'; '36602024'='ssmWU9yLUA'
    '30412794'='n1Izee86Fj'
}

$pf = Join-Path $base 'fetch_progress.json'
$saved = Get-Content $pf -Raw -Encoding UTF8 | ConvertFrom-Json
$books = @($saved.books)

$ok = 0
foreach ($b in $books) {
    $code = $artCovers[$b.subjectId]
    if (-not $code) { continue }
    $coverName = "book$($b.id).jpg"
    $coverPath = Join-Path $uploadDir $coverName
    try {
        Invoke-WebRequest -Uri "https://aka.doubaocdn.com/s/$code" -OutFile $coverPath -UseBasicParsing -TimeoutSec 20 -Headers @{'User-Agent'=$ua; 'Referer'='https://book.douban.com/'}
        if ((Get-Item $coverPath).Length -gt 2000) {
            $b.image = "/upload/$coverName"
            $ok++
            Write-Host "OK: id=$($b.id) $($b.title)"
        }
    } catch { Write-Host "FAIL: id=$($b.id)" }
}
Write-Host "艺术类封面下载: $ok / 10"

# 保存进度并重新生成SQL
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
$noCover = ($books | Where-Object { -not $_.image }).Count
Write-Host "SQL已更新。总$($books.Count)本，无封面$noCover本"