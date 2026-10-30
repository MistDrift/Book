let currentUser = null;

document.addEventListener('DOMContentLoaded', function() {
    checkLogin();
    loadStats();
    loadRecentBorrows();
});

function checkLogin() {
    fetch('/api/login', { credentials: 'include' })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            currentUser = data.user;
            document.getElementById('userName').textContent = data.user.name;
            document.getElementById('userRole').textContent = data.user.role === 1 ? '(管理员)' : '(普通用户)';
            
            if (data.user.role === 1) {
                document.getElementById('userListLink').style.display = 'block';
            }
        } else {
            checkLocalStorageUser();
        }
    })
    .catch(() => {
        checkLocalStorageUser();
    });
}

function checkLocalStorageUser() {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
        try {
            currentUser = JSON.parse(storedUser);
            document.getElementById('userName').textContent = currentUser.name;
            document.getElementById('userRole').textContent = currentUser.role === 1 ? '(管理员)' : '(普通用户)';
            
            if (currentUser.role === 1) {
                document.getElementById('userListLink').style.display = 'block';
            }
        } catch (e) {
            window.location.href = 'index.html';
        }
    } else {
        window.location.href = 'index.html';
    }
}

function logout() {
    fetch('/api/logout', {
        method: 'POST',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(() => {
        localStorage.removeItem('user');
        window.location.href = 'index.html';
    });
}

function loadStats() {
    fetch('/api/book')
    .then(response => response.json())
    .then(books => {
        document.getElementById('bookCount').textContent = books.length;
    });
    
    fetch('/api/borrow?status=0')
    .then(response => response.json())
    .then(borrows => {
        document.getElementById('borrowCount').textContent = Array.isArray(borrows) ? borrows.length : 0;
    });
    
    fetch('/api/user')
    .then(response => response.json())
    .then(users => {
        if (Array.isArray(users)) {
            document.getElementById('userCount').textContent = users.length;
        }
    })
    .catch(() => {
        document.getElementById('userCount').textContent = '无权限';
    });
}

function loadRecentBorrows() {
    fetch('/api/borrow')
    .then(response => response.json())
    .then(borrows => {
        const tbody = document.getElementById('recentBorrows');
        tbody.innerHTML = '';
        
        const recentBorrows = Array.isArray(borrows) ? borrows.slice(0, 5) : [];
        
        if (recentBorrows.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;">暂无借阅记录</td></tr>';
            return;
        }
        
        recentBorrows.forEach(borrow => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${borrow.bookTitle || '未知'}</td>
                <td>${borrow.username || '未知'}</td>
                <td>${formatDate(borrow.borrowDate)}</td>
                <td>${borrow.status === 0 ? '<span class="status-borrowing">借阅中</span>' : '<span class="status-returned">已归还</span>'}</td>
            `;
            tbody.appendChild(row);
        });
    });
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}