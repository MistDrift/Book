let currentUser = null;

document.addEventListener('DOMContentLoaded', function() {
    if (!checkLogin()) {
        return;
    }
    initPage();
    loadList();
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

function formatTime(v) {
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
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
}

function post(url, params) {
    return apiFetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(params).toString()
    }).then(response => response.json());
}

function initPage() {
    if (isAdmin(currentUser)) {
        document.getElementById('userSection').style.display = 'none';
        document.getElementById('adminSection').style.display = 'block';
        loadUsers();
        document.getElementById('adjustForm').addEventListener('submit', submitAdjust);
    } else {
        document.getElementById('userSection').style.display = 'block';
        document.getElementById('adminSection').style.display = 'none';
        document.getElementById('myCredit').textContent = currentUser.credit != null ? currentUser.credit : '-';
    }
}

function loadList() {
    apiFetch('/api/credit/')
    .then(response => response.json())
    .then(list => {
        const records = Array.isArray(list) ? list : [];
        if (isAdmin(currentUser)) {
            renderAll(records);
        } else {
            renderMine(records.filter(r => r.username === currentUser.username));
        }
    })
    .catch(() => {
        if (isAdmin(currentUser)) {
            renderAll([]);
        } else {
            renderMine([]);
        }
    });
}

function deltaText(delta) {
    const d = parseInt(delta);
    if (isNaN(d)) return '-';
    return d > 0 ? '+' + d : String(d);
}

function deltaClass(delta) {
    const d = parseInt(delta);
    if (isNaN(d)) return '';
    return d >= 0 ? 'badge badge-green' : 'badge badge-red';
}

function renderMine(records) {
    const tbody = document.getElementById('myCreditTable');
    tbody.innerHTML = '';

    if (records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">暂无数据</td></tr>';
        return;
    }

    records.forEach(function(r) {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><span class="${deltaClass(r.delta)}">${deltaText(r.delta)}</span></td>
            <td>${r.beforeCredit != null ? r.beforeCredit : '-'}</td>
            <td>${r.afterCredit != null ? r.afterCredit : '-'}</td>
            <td>${r.reason || '-'}</td>
            <td>${formatTime(r.createTime)}</td>
        `;
        tbody.appendChild(row);
    });
}

function renderAll(records) {
    const tbody = document.getElementById('allCreditTable');
    tbody.innerHTML = '';

    if (records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">暂无数据</td></tr>';
        return;
    }

    records.forEach(function(r) {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${r.username || '-'}</td>
            <td><span class="${deltaClass(r.delta)}">${deltaText(r.delta)}</span></td>
            <td>${r.beforeCredit != null ? r.beforeCredit : '-'}</td>
            <td>${r.afterCredit != null ? r.afterCredit : '-'}</td>
            <td>${r.reason || '-'}</td>
            <td>${formatTime(r.createTime)}</td>
        `;
        tbody.appendChild(row);
    });
}

function loadUsers() {
    apiFetch('/api/user/')
    .then(response => response.json())
    .then(users => {
        const select = document.getElementById('userSelect');
        select.innerHTML = '';
        const list = (Array.isArray(users) ? users : []);
        if (list.length === 0) {
            select.innerHTML = '<option value="" disabled>暂无用户</option>';
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
        document.getElementById('userSelect').innerHTML = '<option value="" disabled>用户加载失败</option>';
    });
}

function submitAdjust(e) {
    e.preventDefault();
    const userId = document.getElementById('userSelect').value;
    const delta = document.getElementById('deltaInput').value;
    const reason = document.getElementById('reasonInput').value.trim();

    if (!userId || delta === '' || !reason) {
        alert('请完整填写调整信息');
        return;
    }

    post('/api/credit/adjust', { userId: userId, delta: delta, reason: reason })
    .then(data => {
        if (data.success) {
            alert((data.message || '调整成功') + (data.credit != null ? '，新信用分：' + data.credit : ''));
            document.getElementById('deltaInput').value = '';
            document.getElementById('reasonInput').value = '';
            loadList();
        } else {
            alert(data.message || '调整失败');
        }
    })
    .catch(() => {
        alert('调整失败，请稍后重试');
    });
}
