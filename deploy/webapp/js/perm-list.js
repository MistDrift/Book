let currentUser = null;
let permDefs = [];
let admins = [];
let selectedAdminId = null;

document.addEventListener('DOMContentLoaded', function() {
    if (!checkLogin()) {
        return;
    }
    loadList();
    loadNormalUsers();
    document.getElementById('grantForm').addEventListener('submit', submitGrant);
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

        if (!isAdmin(currentUser)) {
            window.location.href = 'home.html';
            return false;
        }

        document.getElementById('userName').textContent = currentUser.name || currentUser.username;
        document.getElementById('userRole').textContent = '(管理员)';

        document.querySelectorAll('.admin-only').forEach(function(link) {
            link.style.display = 'block';
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

function post(url, params) {
    return apiFetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(params).toString()
    }).then(response => response.json());
}

function loadList() {
    Promise.all([
        apiFetch('/api/admin/perms').then(r => r.json()),
        apiFetch('/api/admin/admins').then(r => r.json())
    ])
    .then(([perms, adminList]) => {
        permDefs = Array.isArray(perms) ? perms : [];
        admins = Array.isArray(adminList) ? adminList : [];
        renderAdmins();
        if (selectedAdminId) {
            renderPanel();
        } else if (admins.length > 0) {
            selectAdmin(admins[0].id);
        } else {
            document.getElementById('permPanel').innerHTML = '<div class="empty-state">暂无管理员</div>';
        }
    })
    .catch(() => {
        document.getElementById('permPanel').innerHTML = '<div class="empty-state">数据加载失败</div>';
    });
}

function renderAdmins() {
    const box = document.getElementById('adminList');
    box.innerHTML = '';

    if (admins.length === 0) {
        box.innerHTML = '<div class="empty-state">暂无管理员</div>';
        return;
    }

    admins.forEach(function(a) {
        const item = document.createElement('div');
        item.className = 'perm-admin-item' + (a.id === selectedAdminId ? ' active' : '');
        item.textContent = `${a.name || a.username}（${a.username}）`;
        item.onclick = function() { selectAdmin(a.id); };
        box.appendChild(item);
    });
}

function selectAdmin(id) {
    selectedAdminId = id;
    renderAdmins();
    renderPanel();
}

function hasPerm(adminPerms, pos) {
    try {
        return ((BigInt(adminPerms || 0) >> BigInt(pos)) & 1n) === 1n;
    } catch (e) {
        return false;
    }
}

function renderPanel() {
    const panel = document.getElementById('permPanel');
    const admin = admins.find(a => a.id === selectedAdminId);
    if (!admin) {
        panel.innerHTML = '<div class="empty-state">请选择左侧管理员查看权限</div>';
        return;
    }

    const groups = {};
    permDefs.forEach(function(p) {
        const g = p.grp || '其他';
        if (!groups[g]) groups[g] = [];
        groups[g].push(p);
    });

    let html = `<h3 style="margin-bottom:15px;">${admin.name || admin.username} 的权限</h3>`;
    if (permDefs.length === 0) {
        html += '<div class="empty-state">暂无权限定义</div>';
    } else {
        Object.keys(groups).forEach(function(g) {
            html += `<div class="perm-group"><h4>${g}</h4><div>`;
            groups[g].forEach(function(p) {
                const checked = hasPerm(admin.perms, p.pos) ? 'checked' : '';
                const title = p.note ? `title="${p.note}"` : '';
                html += `<label class="perm-item" ${title}><input type="checkbox" class="perm-check" data-pos="${p.pos}" ${checked}> ${p.name || p.code}</label>`;
            });
            html += '</div></div>';
        });
        html += `
            <div style="margin-top:20px;display:flex;gap:10px;">
                <button class="btn btn-primary" onclick="savePerms()">保存权限</button>
                <button class="btn btn-danger" onclick="revokeAdmin(${admin.id})">收回权限</button>
            </div>`;
    }
    panel.innerHTML = html;
}

function savePerms() {
    const admin = admins.find(a => a.id === selectedAdminId);
    if (!admin) return;

    let perms = 0n;
    document.querySelectorAll('#permPanel .perm-check:checked').forEach(function(cb) {
        perms |= (1n << BigInt(cb.dataset.pos));
    });

    post('/api/admin/grant', { userId: admin.id, perms: perms.toString() })
    .then(data => {
        alert(data.message || (data.success ? '保存成功' : '保存失败'));
        if (data.success) {
            loadList();
        }
    })
    .catch(() => {
        alert('保存失败，请稍后重试');
    });
}

function revokeAdmin(userId) {
    if (!confirm('确定要收回该用户的管理员权限吗？')) return;

    post('/api/admin/revoke', { userId: userId })
    .then(data => {
        alert(data.message || (data.success ? '已收回权限' : '操作失败'));
        if (data.success) {
            selectedAdminId = null;
            loadList();
            loadNormalUsers();
        }
    })
    .catch(() => {
        alert('操作失败，请稍后重试');
    });
}

function loadNormalUsers() {
    apiFetch('/api/user/')
    .then(response => response.json())
    .then(users => {
        const select = document.getElementById('grantUserSelect');
        const list = (Array.isArray(users) ? users : []).filter(u => !isAdmin(u));
        select.innerHTML = '';
        if (list.length === 0) {
            select.innerHTML = '<option value="" disabled>暂无可授权的普通用户</option>';
            return;
        }
        list.forEach(function(u) {
            const opt = document.createElement('option');
            opt.value = u.id;
            opt.textContent = `${u.name || u.username}（${u.username}）`;
            select.appendChild(opt);
        });
    })
    .catch(() => {
        document.getElementById('grantUserSelect').innerHTML = '<option value="" disabled>用户加载失败</option>';
    });
}

function submitGrant(e) {
    e.preventDefault();
    const userId = document.getElementById('grantUserSelect').value;
    if (!userId) {
        alert('请选择要授权的用户');
        return;
    }

    if (!confirm('确定要将该用户授权为管理员吗？授权后可在上方列表中配置具体权限。')) return;

    post('/api/admin/grant', { userId: userId, perms: '0' })
    .then(data => {
        alert(data.message || (data.success ? '授权成功' : '授权失败'));
        if (data.success) {
            loadList();
            loadNormalUsers();
        }
    })
    .catch(() => {
        alert('授权失败，请稍后重试');
    });
}
