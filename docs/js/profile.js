let currentUser = null;

document.addEventListener('DOMContentLoaded', function() {
    if (!checkLogin()) {
        return;
    }
    loadProfile();

    document.getElementById('profileForm').addEventListener('submit', saveProfile);
    document.getElementById('passwordForm').addEventListener('submit', changePassword);
});

function checkLogin() {
    const storedUser = localStorage.getItem('user');
    if (!storedUser || !getToken()) {
        window.location.href = 'index.html';
        return false;
    }

    try {
        currentUser = JSON.parse(storedUser);
        if (!currentUser || !currentUser.username) {
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
        return true;
    } catch (e) {
        localStorage.removeItem('user');
        window.location.href = 'index.html';
        return false;
    }
}

function logout() {
    apiFetch('/api/logout', { method: 'POST' }).catch(function() {});
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    window.location.href = 'index.html';
}

function fillForm(user) {
    document.getElementById('userId').value = user.id || '';
    document.getElementById('username').value = user.username || '';
    document.getElementById('name').value = user.name || '';
    document.getElementById('email').value = user.email || '';
    document.getElementById('phone').value = user.phone || '';
}

function loadProfile() {
    // 先用 localStorage 中的用户信息填充，再从后端拉取最新资料
    fillForm(currentUser);

    apiFetch('/api/user/profile')
    .then(function(response) { return response.json(); })
    .then(function(user) {
        if (!user || !user.username) return;
        currentUser = Object.assign({}, currentUser, user);
        localStorage.setItem('user', JSON.stringify(currentUser));
        fillForm(currentUser);
        document.getElementById('userName').textContent = currentUser.name || currentUser.username;
    })
    .catch(function() {});
}

function saveProfile(e) {
    e.preventDefault();

    const id = document.getElementById('userId').value || currentUser.id;
    const params = new URLSearchParams({
        id: id,
        name: document.getElementById('name').value,
        email: document.getElementById('email').value,
        phone: document.getElementById('phone').value
    });

    apiFetch('/api/user/' + id + '?' + params.toString(), { method: 'PUT' })
    .then(function(response) { return response.json(); })
    .then(function(data) {
        alert(data.message || (data.success ? '资料更新成功' : '资料更新失败'));
        if (data.success) {
            currentUser.name = params.get('name');
            currentUser.email = params.get('email');
            currentUser.phone = params.get('phone');
            localStorage.setItem('user', JSON.stringify(currentUser));
            document.getElementById('userName').textContent = currentUser.name || currentUser.username;
        }
    })
    .catch(function() {
        alert('网络错误，资料更新失败');
    });
}

function changePassword(e) {
    e.preventDefault();

    const oldPassword = document.getElementById('oldPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        alert('两次输入的新密码不一致');
        return;
    }

    apiFetch('/api/user/password', {
        method: 'PUT',
        body: new URLSearchParams({ oldPassword: oldPassword, newPassword: newPassword })
    })
    .then(function(response) { return response.json(); })
    .then(function(data) {
        if (data.success) {
            alert('密码已修改');
            document.getElementById('passwordForm').reset();
        } else {
            alert(data.message || '密码修改失败');
        }
    })
    .catch(function() {
        alert('网络错误，密码修改失败');
    });
}
