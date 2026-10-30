# 批量下载豆瓣封面（带Referer防盗链）
$ErrorActionPreference = 'Continue'
$base = Split-Path -Parent $PSScriptRoot
$uploadDir = Join-Path $base '其他\src\main\webapp\upload'
$ua = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36'
$headers = @{'User-Agent'=$ua; 'Referer'='https://book.douban.com/'}

# 类别 -> 标签
$catTag = @{
    'A 马列'='马克思主义'; 'B 哲学宗教'='哲学'; 'C 社会科学总论'='社会学'; 'D 政治法律'='政治'
    'E 军事'='军事'; 'F 经济'='经济'; 'G 文化科学教育体育'='教育'; 'H 语言文字'='语言'
    'I 文学'='文学'; 'J 艺术'='艺术'; 'K 历史地理'='历史'; 'N 自然科学总论'='科学'
    'O 数理科学'='数学'; 'P 天文地球'='地理'; 'Q 生物'='生物'; 'R 医药卫生'='医学'
    'S 农业'='农业'; 'T 工业技术'='技术'; 'U 交通运输'='交通'; 'V 航空航天'='航空'
    'X 环境科学'='环境'; 'Z 综合性图书'='百科'
}

# 读取进度
$progressFile = Join-Path $base 'fetch_progress.json'
$saved = Get-Content $progressFile -Raw -Encoding UTF8 | ConvertFrom-Json
$books = @($saved.books)
Write-Host "进度文件中 $($books.Count) 本书"

# 构建 subjectId -> coverFile 映射（从标签页）
$coverMap = @{}
foreach ($cat in $catTag.Keys) {
    $tag = $catTag[$cat]
    Write-Host "抓取标签页: $cat ($tag)"
    try {
        $r = Invoke-WebRequest -Uri "https://book.douban.com/tag/$tag?type=S" -UseBasicParsing -TimeoutSec 25 -Headers @{'User-Agent'=$ua}
        $c = $r.Content
        $ms = [regex]::Matches($c, 'subject/(\d+)/.*?public/(s\d+\.jpg)', [System.Text.RegularExpressions.RegexOptions]::Singleline)
        foreach ($m in $ms) {
            $sid = $m.Groups[1].Value
            $cf = $m.Groups[2].Value
            if (-not $coverMap.ContainsKey($sid)) { $coverMap[$sid] = $cf }
        }
        Write-Host "  已映射 $($coverMap.Count) 个subject"
    } catch {
        Write-Host "  失败: $($_.Exception.Message)"
    }
    Start-Sleep -Milliseconds 500
}

# 下载封面
$okCount = 0
$failCount = 0
foreach ($b in $books) {
    $sid = $b.subjectId
    $coverFile = $coverMap[$sid]
    if (-not $coverFile) { $failCount++; continue }
    $coverName = "book$($b.id).jpg"
    $coverPath = Join-Path $uploadDir $coverName
    $success = $false
    foreach ($s in @('img9','img1','img2','img3')) {
        $u = "https://$s.doubanio.com/view/subject/l/public/$coverFile"
        try {
            Invoke-WebRequest -Uri $u -OutFile $coverPath -UseBasicParsing -TimeoutSec 20 -Headers $headers
            if ((Get-Item $coverPath).Length -gt 2000) { $success = $true; break }
        } catch { }
    }
    if ($success) {
        $b.image = "/upload/$coverName"
        $okCount++
    } else {
        $failCount++
    }
    if (($okCount + $failCount) % 20 -eq 0) {
        Write-Host "进度: 成功$okCount 失败$failCount / $($books.Count)"
    }
}

# 保存进度并重新生成SQL
$saved.books = $books
$saved | ConvertTo-Json -Depth 5 | Out-File -FilePath $progressFile -Encoding UTF8

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
Write-Host "`n封面下载完成: 成功$okCount 失败$failCount"
Write-Host "SQL: $sqlFile"