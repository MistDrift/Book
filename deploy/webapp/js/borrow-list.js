let currentUser = null;

document.addEventListener('DOMContentLoaded', function() {
    console.log('borrow-list.js 加载完成');
    
    if (!checkLogin()) {
        return;
    }
    loadBorrows();
});

function checkLogin() {
    console.log('borrow-list 开始登录验证');
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

const mockBorrows = [
    { id: 1, bookId: 1, bookTitle: 'Java编程思想', username: 'user1', borrowDate: '2024-01-10', dueDate: '2024-02-10', returnDate: '2024-02-08', status: 1 },
    { id: 2, bookId: 2, bookTitle: 'Python编程从入门到实践', username: 'user1', borrowDate: '2024-02-01', dueDate: '2024-03-01', returnDate: null, status: 0 },
    { id: 3, bookId: 3, bookTitle: '深入理解计算机系统', username: 'user2', borrowDate: '2024-02-10', dueDate: '2024-03-10', returnDate: null, status: 0 },
    { id: 4, bookId: 1, bookTitle: 'Java编程思想', username: 'user1', borrowDate: '2026-05-20', dueDate: '2026-06-20', returnDate: null, status: 0 },
    { id: 5, bookId: 4, bookTitle: '深度学习入门', username: 'user1', borrowDate: '2026-05-20', dueDate: '2026-06-20', returnDate: null, status: 0 }
];

function loadBorrows() {
    console.log('加载借阅记录');

    const filter = document.getElementById('statusFilter').value;

    apiFetch('/api/borrow')
    .then(response => response.json())
    .then(borrows => {
        renderBorrows(filterBorrows(Array.isArray(borrows) ? borrows : [], filter));
    })
    .catch(() => {
        console.log('使用mock数据');
        let filteredBorrows = mockBorrows;
        if (currentUser && !isAdmin(currentUser)) {
            filteredBorrows = mockBorrows.filter(b => b.username === currentUser.username);
        }
        renderBorrows(filterBorrows(filteredBorrows, filter));
    });
}

// 客户端状态筛选：''/all全部 0借阅中 1已归还 overdue已逾期(借阅中且应还日期已过)
function filterBorrows(borrows, filter) {
    if (filter === '0') return borrows.filter(b => b.status === 0);
    if (filter === '1') return borrows.filter(b => b.status === 1);
    if (filter === 'overdue') return borrows.filter(isOverdue);
    return borrows;
}

function isOverdue(b) {
    return b.status === 0 && b.dueDate && new Date(b.dueDate) < new Date();
}

function renderBorrows(borrows) {
    const tbody = document.getElementById('borrowTable');
    tbody.innerHTML = '';
    
    if (!Array.isArray(borrows) || borrows.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">暂无借阅记录</td></tr>';
        return;
    }

    borrows.forEach(borrow => {
        const row = document.createElement('tr');
        const overdue = isOverdue(borrow);
        const statusText = borrow.status === 1 ? '已归还' : (overdue ? '已逾期' : '借阅中');
        const statusClass = borrow.status === 1 ? 'status-returned' : (overdue ? 'status-overdue' : 'status-borrowing');

        // 管理员可归还所有图书，普通用户只能操作自己借阅中的图书（续借/归还）
        const canOperate = (isAdmin(currentUser) && borrow.status === 0) ||
                         (!isAdmin(currentUser) && borrow.username === currentUser.username && borrow.status === 0);
        const actionButtons = canOperate ?
            `<button class="btn btn-edit" onclick="renewBook(${borrow.id})">续借</button>
             <button class="btn btn-edit" onclick="returnBook(${borrow.id})">归还</button>` : '-';

        row.innerHTML = `
            <td>${borrow.bookTitle || '未知'}</td>
            <td>${borrow.username || '未知'}</td>
            <td>${formatDate(borrow.borrowDate)}</td>
            <td>${formatDate(borrow.dueDate)}</td>
            <td>${formatDate(borrow.returnDate)}</td>
            <td><span class="${statusClass}">${statusText}</span></td>
            <td>${actionButtons}</td>
        `;
        tbody.appendChild(row);
    });
}

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

function renewBook(id) {
    if (!confirm('确定要续借这本书吗？')) return;

    apiFetch('/api/borrow/' + id + '/renew', {
        method: 'PUT'
    })
    .then(response => response.json())
    .then(data => {
        alert(data.message || (data.success ? '续借成功' : '续借失败'));
        if (data.success) loadBorrows();
    })
    .catch(() => {
        alert('续借失败');
    });
}

function returnBook(id) {
    if (!confirm('确定要归还这本书吗？')) return;
    
    apiFetch('/api/borrow/' + id, {
        method: 'PUT'
    })
    .then(response => response.json())
    .then(data => {
        alert(data.message || (data.success ? '归还成功' : '归还失败'));
        if (data.success) loadBorrows();
    })
    .catch(() => {
        const borrow = mockBorrows.find(b => b.id === id);
        if (borrow) {
            borrow.status = 1;
            borrow.returnDate = new Date().toISOString().split('T')[0];
            alert('归还成功');
            renderBorrows(mockBorrows);
        }
    });
}

// ========== 管理员代借用的模态框 ==========

function showBorrowModal() {
    const modal = document.getElementById('borrowModal');
    if (!modal) return;
    document.getElementById('borrowForm').reset();
    modal.style.display = 'flex';
    const firstInput = document.getElementById('borrowBookId');
    if (firstInput) firstInput.focus();
}

function closeBorrowModal() {
    const modal = document.getElementById('borrowModal');
    if (!modal) return;
    modal.style.display = 'none';
}

// 点击模态框遮罩关闭
document.addEventListener('click', function(e) {
    if (e.target && e.target.id === 'borrowModal') closeBorrowModal();
});

function submitBorrowForm() {
    const bookId = document.getElementById('borrowBookId').value;
    const userId = document.getElementById('borrowUserId').value;
    if (!bookId || !userId) { alert('请填写完整信息'); return; }
    apiFetch('/api/borrow/', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ bookId: parseInt(bookId, 10), userId: parseInt(userId, 10) })
    })
    .then(r => r.json())
    .then(d => {
        alert(d.message || (d.success ? '借阅登记成功' : '借阅登记失败'));
        if (d.success) { closeBorrowModal(); loadBorrows(); }
    })
    .catch(() => alert('借阅登记失败，请稍后重试'));
}
