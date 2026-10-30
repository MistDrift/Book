let currentUser = null;

document.addEventListener('DOMContentLoaded', function() {
    console.log('home.js 加载完成');
    
    // 确保localStorage有用户信息
    if (!initLogin()) {
        console.log('initLogin 返回 false，停留在当前页');
        return;
    }
    
    console.log('登录验证通过，显示页面内容');
    loadStats();
    loadRecentBorrows();
});

function initLogin() {
    console.log('开始登录验证');
    const storedUser = localStorage.getItem('user');
    console.log('localStorage user:', storedUser);
    
    if (!storedUser || !getToken()) {
        console.log('没有找到用户信息，跳转到登录页');
        window.location.href = 'index.html';
        return false;
    }
    
    try {
        currentUser = JSON.parse(storedUser);
        console.log('解析后的用户:', currentUser);
        
        // 验证用户数据完整性
        if (!currentUser || !currentUser.username) {
            console.log('用户数据不完整，清除并跳转');
            localStorage.removeItem('user');
            window.location.href = 'index.html';
            return false;
        }
        
        // 确保role字段存在（默认为普通用户）
        if (currentUser.role === undefined || currentUser.role === null) {
            console.log('role字段缺失，设置为普通用户');
            currentUser.role = 0;
        }
        
        console.log('用户验证通过:', currentUser.username, 'role:', currentUser.role);
        
        // 更新UI
        document.getElementById('userName').textContent = currentUser.name || currentUser.username;
        document.getElementById('userRole').textContent = roleTag(currentUser.role);
        
        const userListLink = document.getElementById('userListLink');
        if (userListLink) {
            userListLink.style.display = isAdmin(currentUser) ? 'block' : 'none';
        }
        
        return true;
    } catch (e) {
        console.error('登录验证出错:', e);
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

const mockBooks = [
    { id: 1, title: 'Java编程思想', author: 'Bruce Eckel', isbn: '9787111213826', category: '编程', price: 108.00, stock: 5, image: 'https://picsum.photos/400/400?random=1' },
    { id: 2, title: 'Python编程从入门到实践', author: 'Eric Matthes', isbn: '9787115428577', category: '编程', price: 79.00, stock: 3, image: 'https://picsum.photos/400/400?random=2' },
    { id: 3, title: '深入理解计算机系统', author: 'Randal E.Bryant', isbn: '9787111544937', category: '计算机', price: 129.00, stock: 2, image: 'https://picsum.photos/400/400?random=3' },
    { id: 4, title: '深度学习入门', author: '斋藤康毅', isbn: '9787115461476', category: 'AI', price: 69.00, stock: 4, image: 'https://picsum.photos/400/400?random=4' },
    { id: 5, title: '设计模式', author: 'Erich Gamma', isbn: '9787111075547', category: '编程', price: 59.00, stock: 3, image: 'https://picsum.photos/400/400?random=5' },
    { id: 6, title: '算法导论', author: 'Thomas H.Cormen', isbn: '9787111407010', category: '算法', price: 128.00, stock: 2, image: 'https://picsum.photos/400/400?random=6' },
    { id: 7, title: 'JavaScript高级程序设计', author: 'Nicholas C.Zakas', isbn: '9787115275790', category: '编程', price: 99.00, stock: 4, image: 'https://picsum.photos/400/400?random=7' },
    { id: 8, title: '数据结构与算法分析', author: 'Mark Allen Weiss', isbn: '9787111234909', category: '算法', price: 69.00, stock: 3, image: 'https://picsum.photos/400/400?random=8' },
    { id: 9, title: '计算机网络', author: 'James F.Kurose', isbn: '9787111453781', category: '网络', price: 89.00, stock: 2, image: 'https://picsum.photos/400/400?random=9' },
    { id: 10, title: '操作系统概念', author: 'Abraham Silberschatz', isbn: '9787111321330', category: '操作系统', price: 79.00, stock: 3, image: 'https://picsum.photos/400/400?random=10' },
    { id: 11, title: '编译原理', author: 'Alfred V.Aho', isbn: '9787111251217', category: '编译', price: 68.00, stock: 2, image: 'https://picsum.photos/400/400?random=11' },
    { id: 12, title: '人工智能', author: 'Stuart Russell', isbn: '9787111320900', category: 'AI', price: 128.00, stock: 2, image: 'https://picsum.photos/400/400?random=12' },
    { id: 13, title: 'C++ Primer', author: 'Stanley B.Lippman', isbn: '9787111213826', category: '编程', price: 128.00, stock: 3, image: 'https://picsum.photos/400/400?random=13' },
    { id: 14, title: 'Effective Java', author: 'Joshua Bloch', isbn: '9787111275554', category: '编程', price: 69.00, stock: 4, image: 'https://picsum.photos/400/400?random=14' },
    { id: 15, title: 'Head First设计模式', author: 'Eric Freeman', isbn: '9787508353937', category: '编程', price: 99.00, stock: 2, image: 'https://picsum.photos/400/400?random=15' },
    { id: 16, title: 'Go语言实战', author: 'William Kennedy', isbn: '9787115423474', category: '编程', price: 89.00, stock: 3, image: 'https://picsum.photos/400/400?random=16' },
    { id: 17, title: 'Redis设计与实现', author: '黄健宏', isbn: '9787111464747', category: '数据库', price: 79.00, stock: 3, image: 'https://picsum.photos/400/400?random=17' },
    { id: 18, title: 'MySQL技术内幕', author: '姜承尧', isbn: '9787111340752', category: '数据库', price: 69.00, stock: 2, image: 'https://picsum.photos/400/400?random=18' },
    { id: 19, title: 'Spring实战', author: 'Craig Walls', isbn: '9787111498847', category: '编程', price: 89.00, stock: 3, image: 'https://picsum.photos/400/400?random=19' },
    { id: 20, title: 'React学习手册', author: 'Alex Banks', isbn: '9787115485706', category: '编程', price: 79.00, stock: 4, image: 'https://picsum.photos/400/400?random=20' }
];

const mockBorrows = [
    { id: 1, bookTitle: 'Java编程思想', username: 'user1', borrowDate: '2024-01-10', status: 1 },
    { id: 2, bookTitle: 'Python编程从入门到实践', username: 'user1', borrowDate: '2024-02-01', status: 1 },
    { id: 3, bookTitle: '深入理解计算机系统', username: 'user2', borrowDate: '2024-02-10', status: 0 },
    { id: 4, bookTitle: 'Java编程思想', username: 'user1', borrowDate: '2026-05-20', status: 1 },
    { id: 5, bookTitle: '深度学习入门', username: 'user1', borrowDate: '2026-05-20', status: 1 }
];

const mockUsers = [
    { id: 1, username: 'admin', name: '管理员', role: 1 },
    { id: 2, username: 'user1', name: '张三', role: 0 },
    { id: 3, username: 'user2', name: '李四', role: 0 },
    { id: 4, username: 'user3', name: '王五', role: 0 }
];

function loadStats() {
    document.getElementById('bookCount').textContent = mockBooks.length;
    const borrowingCount = mockBorrows.filter(b => b.status === 0).length;
    document.getElementById('borrowCount').textContent = borrowingCount;
    document.getElementById('userCount').textContent = mockUsers.length;
}

function loadRecentBorrows() {
    renderBorrows(mockBorrows);
}

function renderBorrows(borrows) {
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
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}
