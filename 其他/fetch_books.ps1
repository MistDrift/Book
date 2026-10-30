# 批量抓取豆瓣书籍（22个中图法类别）
$ErrorActionPreference = 'Continue'
$base = Split-Path -Parent $PSScriptRoot
$uploadDir = Join-Path $base '其他\src\main\webapp\upload'
if (-not (Test-Path $uploadDir)) { New-Item -ItemType Directory -Path $uploadDir -Force | Out-Null }
$ua = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36'

# 22个中图法类别 -> 豆瓣标签
$categories = @(
    @{ name='A 马列'; tag='马克思主义' },
    @{ name='B 哲学宗教'; tag='哲学' },
    @{ name='C 社会科学总论'; tag='社会学' },
    @{ name='D 政治法律'; tag='政治' },
    @{ name='E 军事'; tag='军事' },
    @{ name='F 经济'; tag='经济' },
    @{ name='G 文化科学教育体育'; tag='教育' },
    @{ name='H 语言文字'; tag='语言' },
    @{ name='I 文学'; tag='文学' },
    @{ name='J 艺术'; tag='艺术' },
    @{ name='K 历史地理'; tag='历史' },
    @{ name='N 自然科学总论'; tag='科学' },
    @{ name='O 数理科学'; tag='数学' },
    @{ name='P 天文地球'; tag='地理' },
    @{ name='Q 生物'; tag='生物' },
    @{ name='R 医药卫生'; tag='医学' },
    @{ name='S 农业'; tag='农业' },
    @{ name='T 工业技术'; tag='技术' },
    @{ name='U 交通运输'; tag='交通' },
    @{ name='V 航空航天'; tag='航空' },
    @{ name='X 环境科学'; tag='环境' },
    @{ name='Z 综合性图书'; tag='百科' }
)

$allBooks = @()
$bookId = 28
$progressFile = Join-Path $base 'fetch_progress.json'
if (Test-Path $progressFile) {
    try {
        $saved = Get-Content $progressFile -Raw -Encoding UTF8 | ConvertFrom-Json
        $bookId = $saved.nextId
        $allBooks = @($saved.books)
        Write-Host "恢复进度: 已有 $($allBooks.Count) 本，下一个ID=$bookId"
    } catch { }
}

function Save-Progress {
    $obj = @{ nextId = $bookId; books = $allBooks }
    $obj | ConvertTo-Json -Depth 5 | Out-File -FilePath $progressFile -Encoding UTF8
}

function Get-DoubanPage($url) {
    try {
        $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 25 -Headers @{'User-Agent'=$ua; 'Accept-Language'='zh-CN,zh;q=0.9'}
        return $r.Content
    } catch {
        Write-Host "    请求失败: $($_.Exception.Message)"
        return $null
    }
}

function Parse-TagBooks($html) {
    $books = @()
    if (-not $html) { return $books }
    $pattern = 'subject/(\d+)/.*?public/(s\d+\.jpg).*?title="([^"]+)".*?class="pub">\s*(.+?)\s*</div>'
    $ms = [regex]::Matches($html, $pattern, [System.Text.RegularExpressions.RegexOptions]::Singleline)
    foreach ($m in $ms) {
        $books += @{
            subjectId = $m.Groups[1].Value
            coverFile = $m.Groups[2].Value
            title = $m.Groups[3].Value.Trim()
            pub = $m.Groups[4].Value.Trim()
        }
    }
    return $books
}

function Parse-Pub($pub) {
    $r = @{}
    # 格式: 作者 / 出版社 / 日期 / 价格
    $parts = $pub -split ' / '
    if ($parts.Count -ge 1) { $r.author = $parts[0].Trim() }
    if ($parts.Count -ge 2) { $r.publisher = $parts[1].Trim() }
    if ($parts.Count -ge 3 -and $parts[2] -match '(\d{4})') { $r.year = $Matches[1] }
    if ($parts.Count -ge 4 -and $parts[3] -match '([0-9.]+)') { $r.price = [decimal]$Matches[1] }
    return $r
}

function Get-BookDetail($subjectId) {
    $c = Get-DoubanPage "https://book.douban.com/subject/$subjectId/"
    if (-not $c) { return $null }
    $d = @{}
    if ($c -match 'ISBN:</span>\s*([0-9][0-9-]+)') { $d.isbn = $Matches[1].Replace('-','') }
    elseif ($c -match 'isbn[^0-9]{0,15}([0-9][0-9-]{9,})') { $d.isbn = $Matches[1].Replace('-','') }
    else { return $null }
    if ($c -match '作者</span>:\s*(.+?)</') { $d.author = $Matches[1].Trim() }
    if ($c -match '出版社</span>:\s*(.+?)</') { $d.publisher = $Matches[1].Trim() }
    if ($c -match '出版年</span>:\s*(\d{4})') { $d.year = $Matches[1] }
    if ($c -match '定价</span>:\s*([0-9.]+)') { $d.price = [decimal]$Matches[1] }
    if ($c -match 'img\d+\.doubanio\.com/view/subject/l/public/(\w+\.jpg)') { $d.largeCover = $Matches[1] }
    return $d
}

function Download-Cover($coverFile, $fileName) {
    if (-not $coverFile) { return $false }
    $servers = @('img9','img1','img2','img3','img4','img5')
    foreach ($s in $servers) {
        $u = "https://$s.doubanio.com/view/subject/l/public/$coverFile"
        try {
            Invoke-WebRequest -Uri $u -OutFile $fileName -UseBasicParsing -TimeoutSec 20 -Headers @{'User-Agent'=$ua}
            $f = Get-Item $fileName
            if ($f.Length -gt 2000) { return $true }
        } catch { }
    }
    return $false
}

foreach ($cat in $categories) {
    Write-Host "`n=== $($cat.name) [标签:$($cat.tag)] ==="
    # 跳过已完成的类别
    $existingCount = ($allBooks | Where-Object { $_.category -eq $cat.name }).Count
    if ($existingCount -ge 10) { Write-Host "  已完成($existingCount本)，跳过"; continue }
    $html = Get-DoubanPage "https://book.douban.com/tag/$($cat.tag)?type=S"
    $tagBooks = Parse-TagBooks $html
    Write-Host "  找到 $($tagBooks.Count) 本"
    $count = 0
    foreach ($tb in $tagBooks) {
        if ($count -ge 10) { break }
        # 跳过已存在的subject
        if ($allBooks | Where-Object { $_.subjectId -eq $tb.subjectId }) { continue }
        Write-Host "  [$count] $($tb.title)"
        # 从标签页pub字段提取作者/出版社/年份/价格
        $pub = Parse-Pub $tb.pub
        Start-Sleep -Milliseconds 600
        # 详情页只取ISBN
        $d = Get-BookDetail $tb.subjectId
        if ($d -and $d.isbn) {
            $coverName = "book$bookId.jpg"
            $coverPath = Join-Path $uploadDir $coverName
            $ok = Download-Cover $tb.coverFile $coverPath
            $imageUrl = if ($ok) { "/upload/$coverName" } else { $null }
            $pubDate = if ($pub.year) { "$($pub.year)-01-01" } else { '2000-01-01' }
            $allBooks += @{
                id=$bookId; subjectId=$tb.subjectId; isbn=$d.isbn; title=$tb.title
                author=$(if($pub.author){$pub.author}else{'未知'}); publisher=$(if($pub.publisher){$pub.publisher}else{'未知'})
                publishDate=$pubDate; category=$cat.name; price=$(if($pub.price){$pub.price}else{0}); stock=5; image=$imageUrl
            }
            Write-Host "    ISBN=$($d.isbn) 封面=$ok"
            $bookId++; $count++
            if ($count % 3 -eq 0) { Save-Progress }
        } else {
            Write-Host "    跳过：无ISBN"
        }
    }
    Save-Progress
}

# 生成SQL
$sqlFile = Join-Path $base 'insert_books.sql'
$sql = New-Object System.Collections.Generic.List[string]
$sql.Add("USE db_book;")
foreach ($b in $allBooks) {
    $t = $b.title -replace "'", "''"
    $a = $b.author -replace "'", "''"
    $p = $b.publisher -replace "'", "''"
    $img = if ($b.image) { "'$($b.image)'" } else { 'NULL' }
    $sql.Add("INSERT INTO tb_book (isbn,title,author,publisher,publish_date,category,price,stock,image,coll_status) VALUES ('$($b.isbn)','$t','$a','$p','$($b.publishDate)','$($b.category)',$($b.price),$($b.stock),$img,1);")
}
[IO.File]::WriteAllLines($sqlFile, $sql, (New-Object System.Text.UTF8Encoding($false)))
Write-Host "`n完成！共 $($allBooks.Count) 本书，SQL: $sqlFile"
$allBooks | Group-Object category | Select-Object Name, Count | Format-Table -AutoSize
