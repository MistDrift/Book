let currentUser = null;

// 日期格式化：毫秒时间戳或日期字符串 -> yyyy-MM-dd
function formatDate(v) {
    if (v === null || v === undefined || v === '') return '-';
    let d;
    if (typeof v === 'number') {
        d = new Date(v);
    } else if (/^\d+$/.test(String(v))) {
        d = new Date(parseInt(v));
    } else {
        d = new Date(v);
    }
    if (isNaN(d.getTime())) return '-';
    const p = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

document.addEventListener('DOMContentLoaded', function() {
    console.log('book-search.js 加载完成');
    
    if (!checkLogin()) {
        return;
    }
    loadBooks();
    
    document.getElementById('searchKeyword').addEventListener('input', searchBooks);
});

function checkLogin() {
    console.log('book-search 开始登录验证');
    const storedUser = localStorage.getItem('user');
    console.log('localStorage user:', storedUser);
    
    if (!storedUser || !getToken()) {
        console.log('没有用户信息，跳转登录');
        window.location.href = 'index.html';
        return false;
    }
    
    try {
        currentUser = JSON.parse(storedUser);
        console.log('解析用户:', currentUser);
        
        if (!currentUser || !currentUser.username) {
            console.log('用户数据无效');
            localStorage.removeItem('user');
            window.location.href = 'index.html';
            return false;
        }
        
        if (currentUser.role === undefined) {
            currentUser.role = 0;
        }
        
        document.getElementById('userName').textContent = currentUser.name || currentUser.username;
        document.getElementById('userRole').textContent = roleTag(currentUser.role);
        
        document.querySelectorAll('.admin-only').forEach(function(link) {
            link.style.display = isAdmin(currentUser) ? 'block' : 'none';
        });
        
        console.log('验证通过');
        return true;
    } catch (e) {
        console.error('验证出错:', e);
        localStorage.removeItem('user');
        window.location.href = 'index.html';
        return false;
    }
}

function logout() {
    console.log('退出登录');
    apiFetch('/api/logout', { method: 'POST' }).catch(function() {});
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    window.location.href = 'index.html';
}

function loadBooks() {
    console.log('加载图书列表');
    apiFetch('/api/book')
    .then(response => response.json())
    .then(books => {
        renderBooks(books);
    })
    .catch(error => {
        console.error('加载图书列表失败:', error);
        renderBooks([]);
    });
}

function renderBooks(books) {
    const container = document.getElementById('bookGrid');
    container.innerHTML = '';
    
    if (!Array.isArray(books) || books.length === 0) {
        container.innerHTML = '<div class="no-results">没有找到图书</div>';
        return;
    }
    
    books.forEach(book => {
        const card = document.createElement('div');
        card.className = 'book-card';
        card.onclick = () => showBookDetail(book);
        card.innerHTML = `
            <div class="book-image">
                <img src="${resolveImage(book.image) || 'https://picsum.photos/200/300?random=book'}" alt="${book.title}">
            </div>
            <div class="book-info">
                <h3 class="book-title">${book.title}</h3>
                <p class="book-author">作者: ${book.author}</p>
                <p class="book-isbn">ISBN: ${book.isbn}</p>
                <p class="book-price">¥${(book.price || 0).toFixed(2)}</p>
                <p class="book-stock">库存: ${book.stock || 0} 本</p>
            </div>
        `;
        container.appendChild(card);
    });
}

// 当前选中的图书
let currentSelectedBook = null;

// 显示图书详情模态框
function showBookDetail(book) {
    currentSelectedBook = book;
    const modal = document.getElementById('bookDetailModal');
    
    document.getElementById('detailImage').src = resolveImage(book.image) || 'https://picsum.photos/300/400?random=book';
    document.getElementById('detailTitle').textContent = book.title;
    document.getElementById('detailAuthor').textContent = book.author || '未知';
    document.getElementById('detailISBN').textContent = book.isbn || '-';
    document.getElementById('detailPublisher').textContent = book.publisher || '未知';
    document.getElementById('detailPublishDate').textContent = formatDate(book.publishDate);
    document.getElementById('detailCategory').textContent = book.category || '未分类';
    document.getElementById('detailPrice').textContent = `¥${(book.price || 0).toFixed(2)}`;
    document.getElementById('detailStock').textContent = book.stock || 0;
    document.getElementById('detailLocation').textContent = book.location || '未知';
    document.getElementById('detailDescription').textContent = book.description || '暂无简介';
    
    const borrowBtn = document.getElementById('borrowBtn');
    if (book.stock > 0 && currentUser && !isAdmin(currentUser)) {
        borrowBtn.style.display = 'inline-block';
        borrowBtn.disabled = false;
        borrowBtn.onclick = () => applyBorrow(book);
    } else {
        borrowBtn.style.display = 'none';
        borrowBtn.disabled = true;
    }
    
    modal.style.display = 'flex';
    modal.style.justifyContent = 'center';
    modal.style.alignItems = 'center';
}

// 关闭图书详情模态框
function closeBookDetail() {
    document.getElementById('bookDetailModal').style.display = 'none';
    currentSelectedBook = null;
}

// 申请借阅
function applyBorrow(book) {
    // 拉取当前用户等级（借阅上限/期限）与当前借阅记录
    Promise.all([
        apiFetch('/api/tier').then(r => r.json()).catch(() => []),
        apiFetch('/api/borrow').then(r => r.json()).catch(() => [])
    ]).then(([tiers, borrows]) => {
        const tier = Array.isArray(tiers) ? tiers.find(t => t.id === currentUser.tierId) : null;
        const maxBooks = tier && tier.maxBorrow != null ? tier.maxBorrow : 3;
        const days = tier && tier.days != null ? tier.days : 30;
        const currentBorrowCount = Array.isArray(borrows) ? borrows.filter(b => b.status === 0).length : 0;

        if (currentBorrowCount >= maxBooks) {
            alert(`借阅失败：您当前已借阅${currentBorrowCount}本，当前等级最多可借阅${maxBooks}本（期限${days}天）`);
            return;
        }

        if (!confirm(`确定借阅《${book.title}》吗？（借阅期限${days}天）`)) return;

        apiFetch('/api/borrow', {
            method: 'POST',
            body: `bookId=${book.id}`
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert(data.message || '借阅成功！');
                closeBookDetail();
                window.location.href = 'borrow-apply.html';
            } else {
                alert(data.message || '借阅失败');
            }
        })
        .catch(error => {
            console.error('借阅请求失败:', error);
            alert('借阅失败');
        });
    });
}

// 点击模态框外部关闭
document.addEventListener('click', function(e) {
    const modal = document.getElementById('bookDetailModal');
    if (e.target === modal) {
        closeBookDetail();
    }
});

function searchBooks() {
    const kw = document.getElementById('searchKeyword').value.trim();
    console.log('搜索关键词:', kw);

    if (!kw) {
        loadBooks();
        return;
    }

    apiFetch('/api/book?keyword=' + encodeURIComponent(kw))
    .then(response => response.json())
    .then(books => {
        renderBooks(books);
    })
    .catch(error => {
        console.error('搜索图书失败:', error);
        renderBooks([]);
    });
}
