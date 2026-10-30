let currentUser = null;
let page = 1;
let size = 20;
let pages = 1;
let total = 0;
let actionCollected = false;

document.addEventListener('DOMContentLoaded', function() {
    if (!checkLogin()) {
        return;
    }
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

function loadList() {
    const action = document.getElementById('actionFilter').value;
    const url = `/api/log/?action=${encodeURIComponent(action)}&page=${page}&size=${size}`;

    apiFetch(url)
    .then(response => response.json())
    .then(data => {
        const list = Array.isArray(data.list) ? data.list : [];
        total = data.total != null ? data.total : list.length;
        pages = data.pages != null ? data.pages : 1;
        page = data.page != null ? data.page : page;

        collectActions(list);
        renderList(list);
        renderPagination();
    })
    .catch(() => {
        renderList([]);
        renderPagination();
    });
}

function collectActions(list) {
    if (actionCollected) return;
    const select = document.getElementById('actionFilter');
    const seen = new Set();
    list.forEach(function(l) {
        if (l.action && !seen.has(l.action)) {
            seen.add(l.action);
            const opt = document.createElement('option');
            opt.value = l.action;
            opt.textContent = l.action;
            select.appendChild(opt);
        }
    });
    actionCollected = true;
}

function renderList(list) {
    const tbody = document.getElementById('logTable');
    tbody.innerHTML = '';

    if (list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align:center;">暂无数据</td></tr>';
        return;
    }

    list.forEach(function(l) {
        const row = document.createElement('tr');
        const ok = l.ok === 1 || l.ok === true || l.ok === '1';
        row.innerHTML = `
            <td>${l.id != null ? l.id : '-'}</td>
            <td>${l.username || '-'}</td>
            <td>${l.action || '-'}</td>
            <td>${l.target || '-'}</td>
            <td>${l.targetId != null ? l.targetId : '-'}</td>
            <td>${l.detail || '-'}</td>
            <td>${l.ip || '-'}</td>
            <td><span class="badge ${ok ? 'badge-green' : 'badge-red'}">${ok ? '成功' : '失败'}</span></td>
            <td>${formatTime(l.createTime)}</td>
        `;
        tbody.appendChild(row);
    });
}

function renderPagination() {
    document.getElementById('pageInfo').textContent = `第 ${page} / ${pages} 页，共 ${total} 条`;
    document.getElementById('prevBtn').disabled = page <= 1;
    document.getElementById('nextBtn').disabled = page >= pages;
}

function filterAction() {
    page = 1;
    loadList();
}

function prevPage() {
    if (page <= 1) return;
    page--;
    loadList();
}

function nextPage() {
    if (page >= pages) return;
    page++;
    loadList();
}
