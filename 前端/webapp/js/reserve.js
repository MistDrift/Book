let currentUser = null;
let reserves = [];

const reserveStatusText = { 0: '等待中', 1: '已通知', 2: '已取消', 3: '已过期', 4: '已取书' };
const reserveStatusBadge = { 0: 'badge-orange', 1: 'badge-blue', 2: 'badge-purple', 3: 'badge-red', 4: 'badge-green' };

document.addEventListener('DOMContentLoaded', function() {
    if (!checkLogin()) {
        return;
    }
    loadList();
    document.getElementById('reserveForm').addEventListener('submit', submitForm);
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

function loadList() {
    apiFetch('/api/reserve/')
    .then(response => response.json())
    .then(list => {
        reserves = Array.isArray(list) ? list : [];
        renderList();
    })
    .catch(() => {
        reserves = [];
        renderList();
    });
}

function renderList() {
    const tbody = document.getElementById('reserveTable');
    tbody.innerHTML = '';

    if (reserves.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">暂无数据</td></tr>';
        return;
    }

    reserves.forEach(function(r) {
        const row = document.createElement('tr');
        const statusText = reserveStatusText[r.status] || '未知';
        const statusBadge = reserveStatusBadge[r.status] || 'badge-blue';

        let actions = [];
        if ((r.status === 0 || r.status === 1) && (isAdmin(currentUser) || r.username === currentUser.username)) {
            actions.push(`<button class="btn btn-warning" onclick="cancelReserve(${r.id})">取消</button>`);
        }
        if (isAdmin(currentUser) && r.status === 1) {
            actions.push(`<button class="btn btn-success" onclick="pickReserve(${r.id})">确认取书</button>`);
        }

        row.innerHTML = `
            <td>${r.username || '-'}</td>
            <td>${r.bookTitle || '-'}</td>
            <td>${r.tierName || '-'}</td>
            <td>${formatTime(r.reserveTime)}</td>
            <td>${formatDate(r.deadline)}</td>
            <td><span class="badge ${statusBadge}">${statusText}</span></td>
            <td>${actions.length ? actions.join('') : '-'}</td>
        `;
        tbody.appendChild(row);
    });
}

function showModal() {
    const select = document.getElementById('bookSelect');
    select.innerHTML = '<option value="">加载中...</option>';
    document.getElementById('reserveModal').classList.add('show');

    apiFetch('/api/book/')
    .then(response => response.json())
    .then(books => {
        const list = (Array.isArray(books) ? books : []).filter(b => b.stock <= 0);
        select.innerHTML = '';
        if (list.length === 0) {
            select.innerHTML = '<option value="" disabled>暂无可预约图书（均有库存）</option>';
            return;
        }
        list.forEach(function(b) {
            const opt = document.createElement('option');
            opt.value = b.id;
            opt.textContent = `${b.title}（${b.author || '未知作者'}，库存${b.stock}）`;
            select.appendChild(opt);
        });
    })
    .catch(() => {
        select.innerHTML = '<option value="" disabled>图书加载失败</option>';
    });
}

function closeModal() {
    document.getElementById('reserveModal').classList.remove('show');
}

function submitForm(e) {
    e.preventDefault();
    const bookId = document.getElementById('bookSelect').value;
    if (!bookId) {
        alert('请选择要预约的图书');
        return;
    }

    post('/api/reserve/', { bookId: bookId })
    .then(data => {
        alert(data.message || (data.success ? '预约成功' : '预约失败'));
        if (data.success) {
            closeModal();
            loadList();
        }
    })
    .catch(() => {
        alert('预约失败，请稍后重试');
    });
}

function cancelReserve(id) {
    if (!confirm('确定要取消该预约吗？')) return;

    post(`/api/reserve/${id}/cancel`, {})
    .then(data => {
        alert(data.message || (data.success ? '已取消' : '操作失败'));
        if (data.success) {
            loadList();
        }
    })
    .catch(() => {
        alert('操作失败，请稍后重试');
    });
}

function pickReserve(id) {
    if (!confirm('确认读者已取书？')) return;

    post(`/api/reserve/${id}/pick`, {})
    .then(data => {
        alert(data.message || (data.success ? '已确认取书' : '操作失败'));
        if (data.success) {
            loadList();
        }
    })
    .catch(() => {
        alert('操作失败，请稍后重试');
    });
}
