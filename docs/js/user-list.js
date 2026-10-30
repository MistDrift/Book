let currentUser = null;

document.addEventListener('DOMContentLoaded', function() {
    checkLogin();
    
    document.getElementById('userForm').addEventListener('submit', saveUser);
});

function checkLogin() {
    const storedUser = localStorage.getItem('user');
    if (!storedUser || !getToken()) {
        window.location.href = 'index.html';
        return;
    }
    
    try {
        const user = JSON.parse(storedUser);
        currentUser = user;
        document.getElementById('userName').textContent = user.name;
        document.getElementById('userRole').textContent = roleTag(user.role);
        
        if (!isAdmin(user)) {
            window.location.href = 'home.html';
            return;
        }
        
        loadUsers();
    } catch (e) {
        window.location.href = 'index.html';
    }
}

function logout() {
    apiFetch('/api/logout', { method: 'POST' }).catch(function() {});
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    window.location.href = 'index.html';
}

const mockUsers = [
    { id: 1, username: 'admin', name: '管理员', role: 1, email: 'admin@example.com', phone: '12345678901' },
    { id: 2, username: 'user', name: '普通用户', role: 0, email: 'user@example.com', phone: '12345678902' },
    { id: 3, username: 'test', name: '测试用户', role: 0, email: 'test@example.com', phone: '12345678903' }
];

function loadUsers() {
    renderUsers(mockUsers);
}

function renderUsers(users) {
    const tbody = document.getElementById('userTable');
    tbody.innerHTML = '';
    
    users.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.username}</td>
            <td>${user.name}</td>
            <td>${roleName(user.role)}</td>
            <td>${user.email || '-'}</td>
            <td>${user.phone || '-'}</td>
            <td>
                <button class="btn btn-edit" onclick="showEditModal(${user.id})">编辑</button>
                <button class="btn btn-delete" onclick="deleteUser(${user.id})">删除</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function showAddModal() {
    document.getElementById('modalTitle').textContent = '添加用户';
    document.getElementById('userId').value = '';
    document.getElementById('username').value = '';
    document.getElementById('password').value = '';
    document.getElementById('name').value = '';
    document.getElementById('role').value = '0';
    document.getElementById('email').value = '';
    document.getElementById('phone').value = '';
    document.getElementById('userModal').style.display = 'flex';
}

function showEditModal(id) {
    const user = mockUsers.find(u => u.id === id);
    if (user) {
        document.getElementById('modalTitle').textContent = '编辑用户';
        document.getElementById('userId').value = user.id;
        document.getElementById('username').value = user.username;
        document.getElementById('password').value = '';
        document.getElementById('name').value = user.name;
        document.getElementById('role').value = user.role;
        document.getElementById('email').value = user.email || '';
        document.getElementById('phone').value = user.phone || '';
        document.getElementById('userModal').style.display = 'flex';
    }
}

function closeModal() {
    document.getElementById('userModal').style.display = 'none';
}

function saveUser(e) {
    e.preventDefault();
    
    const id = document.getElementById('userId').value;
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const name = document.getElementById('name').value;
    const role = document.getElementById('role').value;
    const email = document.getElementById('email').value;
    const phone = document.getElementById('phone').value;
    
    if (!id && (!password || password.length < 3)) {
        alert('密码至少需要3个字符');
        return;
    }
    
    if (id) {
        const index = mockUsers.findIndex(u => u.id === parseInt(id));
        if (index !== -1) {
            mockUsers[index] = {
                ...mockUsers[index],
                username,
                name,
                role: parseInt(role),
                email,
                phone
            };
        }
    } else {
        mockUsers.push({
            id: Date.now(),
            username,
            name,
            role: parseInt(role),
            email,
            phone
        });
    }
    
    closeModal();
    loadUsers();
}

function deleteUser(id) {
    if (!confirm('确定要删除这个用户吗？')) return;
    
    const index = mockUsers.findIndex(u => u.id === id);
    if (index !== -1) {
        mockUsers.splice(index, 1);
    }
    loadUsers();
}