let currentUser = null;
let allBorrows = [];
let filteredBorrows = [];
let currentPage = 1;
const PAGE_SIZE = 10;

document.addEventListener('DOMContentLoaded', function() {
    if (!guardAccess()) return;
    if (!checkLogin()) return;
    loadHistory();
});

function checkLogin() {
    const storedUser = localStorage.getItem('user');
    if (!storedUser || !getToken()) {
        window.location.href = 'index.html';
        return false;
    }
    try {
        currentUser = JSON.parse(storedUser);
    } catch (e) {
        localStorage.removeItem('user');
        window.location.href = 'index.html';
        return false;
    }
    if (!currentUser || !currentUser.username) {
        localStorage.removeItem('user');
        window.location.href = 'index.html';
        return false;
    }
    document.getElementById('userName').textContent = currentUser.name || currentUser.username;
    document.getElementById('userRole').textContent = roleTag(currentUser.role);
    document.querySelectorAll('.admin-only').forEach(function(link) {
        link.style.display = isAdmin(currentUser) ? 'block' : 'none';
    });
    return true;
}

function logout() {
    apiFetch('/api/logout', { method: 'POST' }).catch(function() {});
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    window.location.href = 'index.html';
}

function loadHistory() {
    apiFetch('/api/borrow')
    .then(function(r) { return r.json(); })
    .then(function(list) {
        let borrows = Array.isArray(list) ? list : [];
        // 管理员接口会返回全部记录，这里只保留当前账号本人的历史
        if (currentUser && currentUser.username) {
            borrows = borrows.filter(function(b) { return b.username === currentUser.username; });
        }
        allBorrows = borrows;
        renderStats();
        applyFilter();
    })
    .catch(function() {
        allBorrows = [];
        renderStats();
        applyFilter();
    });
}

function renderStats() {
    const borrowing = allBorrows.filter(function(b) { return b.status === 0 && !isOverdue(b); }).length;
    const returned = allBorrows.filter(function(b) { return b.status === 1; }).length;
    const overdue = allBorrows.filter(isOverdue).length;
    document.getElementById('stTotal').textContent = allBorrows.length;
    document.getElementById('stBorrowing').textContent = borrowing;
    document.getElementById('stReturned').textContent = returned;
    document.getElementById('stOverdue').textContent = overdue;
}

function isOverdue(b) {
    const due = toDate(b.dueDate);
    return b.status === 0 && due && due < new Date();
}

function toDate(v) {
    if (v === null || v === undefined || v === '') return null;
    let d;
    if (typeof v === 'number') {
        d = new Date(v);
    } else if (/^\d+$/.test(String(v))) {
        d = new Date(parseInt(v, 10));
    } else {
        d = new Date(v);
    }
    return isNaN(d.getTime()) ? null : d;
}

function formatDate(v) {
    const d = toDate(v);
    if (!d) return '-';
    const p = function(n) { return String(n).padStart(2, '0'); };
    return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate());
}

function overdueDays(b) {
    const due = toDate(b.dueDate);
    if (!due || b.status === 1) return 0;
    const diff = Math.floor((new Date() - due) / 86400000);
    return diff > 0 ? diff : 0;
}

function applyFilter() {
    const status = document.getElementById('statusFilter').value;
    const keyword = document.getElementById('keyword').value.trim().toLowerCase();
    const from = document.getElementById('dateFrom').value;
    const to = document.getElementById('dateTo').value;

    filteredBorrows = allBorrows.filter(function(b) {
        if (status === '0' && b.status !== 0) return false;
        if (status === '1' && b.status !== 1) return false;
        if (status === 'overdue' && !isOverdue(b)) return false;

        if (keyword) {
            const text = ((b.bookTitle || '') + ' ' + (b.bookAuthor || '') + ' ' + (b.bookCategory || '')).toLowerCase();
            if (text.indexOf(keyword) === -1) return false;
        }

        const bd = formatDate(b.borrowDate);
        if (from && bd !== '-' && bd < from) return false;
        if (to && bd !== '-' && bd > to) return false;
        return true;
    });

    currentPage = 1;
    renderPage();
}

function resetFilter() {
    document.getElementById('statusFilter').value = '';
    document.getElementById('keyword').value = '';
    document.getElementById('dateFrom').value = '';
    document.getElementById('dateTo').value = '';
    applyFilter();
}

function gotoPage(page) {
    const totalPages = Math.max(1, Math.ceil(filteredBorrows.length / PAGE_SIZE));
    if (page < 1 || page > totalPages) return;
    currentPage = page;
    renderPage();
}

function renderPage() {
    const tbody = document.getElementById('historyTable');
    tbody.innerHTML = '';

    const totalPages = Math.max(1, Math.ceil(filteredBorrows.length / PAGE_SIZE));
    const start = (currentPage - 1) * PAGE_SIZE;
    const pageRows = filteredBorrows.slice(start, start + PAGE_SIZE);

    if (pageRows.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align:center;">暂无借阅记录</td></tr>';
    }

    pageRows.forEach(function(b) {
        const overdue = isOverdue(b);
        const statusText = b.status === 1 ? '已归还' : (overdue ? '已逾期' : '借阅中');
        const statusClass = b.status === 1 ? 'status-returned' : (overdue ? 'status-overdue' : 'status-borrowing');
        const days = overdueDays(b);
        const fineText = (b.fine && Number(b.fine) > 0) ? ('¥' + Number(b.fine).toFixed(2)) : '-';

        const canOperate = b.status === 0;
        const actions = canOperate
            ? '<button class="btn btn-edit" onclick="renewBook(' + b.id + ')">续借</button>' +
              '<button class="btn btn-return" onclick="returnBook(' + b.id + ')">归还</button>'
            : '-';

        const row = document.createElement('tr');
        row.innerHTML =
            '<td><img src="' + coverUrl(b.bookImage) + '" alt="封面" onerror="onCoverError(this)" ' +
            'style="width:44px;height:60px;object-fit:cover;border-radius:4px;background:#eef2f7;"></td>' +
            '<td>' + escapeHtml(b.bookTitle || '未知') +
            (days > 0 ? '<div style="color:#e74c3c;font-size:12px;">逾期 ' + days + ' 天</div>' : '') + '</td>' +
            '<td>' + escapeHtml(b.bookAuthor || '-') + '</td>' +
            '<td>' + escapeHtml(b.bookCategory || '-') + '</td>' +
            '<td>' + formatDate(b.borrowDate) + '</td>' +
            '<td>' + formatDate(b.dueDate) + '</td>' +
            '<td>' + formatDate(b.returnDate) + '</td>' +
            '<td><span class="' + statusClass + '">' + statusText + '</span></td>' +
            '<td>' + fineText + '</td>' +
            '<td>' + actions + '</td>';
        tbody.appendChild(row);
    });

    document.getElementById('pageInfo').textContent = '第 ' + currentPage + ' / ' + totalPages + ' 页（共 ' + filteredBorrows.length + ' 条）';
    document.getElementById('prevBtn').disabled = currentPage <= 1;
    document.getElementById('nextBtn').disabled = currentPage >= totalPages;
}

function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, function(c) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
    });
}

function renewBook(id) {
    if (!confirm('确定要续借这本书吗？')) return;
    apiFetch('/api/borrow/' + id + '/renew', { method: 'PUT' })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        alert(data.message || (data.success ? '续借成功' : '续借失败'));
        if (data.success) loadHistory();
    })
    .catch(function() { alert('续借失败'); });
}

function returnBook(id) {
    if (!confirm('确定要归还这本书吗？')) return;
    apiFetch('/api/borrow/' + id, { method: 'PUT' })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        alert(data.message || (data.success ? '归还成功' : '归还失败'));
        if (data.success) loadHistory();
    })
    .catch(function() { alert('归还失败'); });
}

function exportHistory() {
    if (filteredBorrows.length === 0) {
        alert('当前没有可导出的记录');
        return;
    }
    const headers = ['书名', '作者', '分类', '借阅日期', '应还日期', '归还日期', '状态', '逾期天数', '罚金'];
    const rows = filteredBorrows.map(function(b) {
        const overdue = isOverdue(b);
        const statusText = b.status === 1 ? '已归还' : (overdue ? '已逾期' : '借阅中');
        return [
            b.bookTitle || '',
            b.bookAuthor || '',
            b.bookCategory || '',
            formatDate(b.borrowDate),
            formatDate(b.dueDate),
            formatDate(b.returnDate),
            statusText,
            overdueDays(b),
            (b.fine && Number(b.fine) > 0) ? Number(b.fine).toFixed(2) : '0.00'
        ];
    });
    const stamp = new Date().toISOString().slice(0, 10);
    exportCsv('我的借阅记录_' + (currentUser ? currentUser.username : '') + '_' + stamp + '.csv', headers, rows);
}
