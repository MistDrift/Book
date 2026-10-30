let currentUser = null;
let fines = [];

const payStatusText = { 0: '未缴纳', 1: '已缴纳', 2: '已减免' };
const payStatusBadge = { 0: 'badge-red', 1: 'badge-green', 2: 'badge-blue' };

document.addEventListener('DOMContentLoaded', function() {
    if (!checkLogin()) {
        return;
    }
    // 读者隐藏状态筛选器，避免按状态查到他人罚金
    if (!isAdmin(currentUser)) {
        const fs = document.querySelector('.filter-section');
        if (fs) fs.style.display = 'none';
    }
    loadList();
    document.getElementById('waiveForm').addEventListener('submit', submitWaive);
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
    // 读者不带 status 参数：后端 /api/fine/ 对非管理员只返回本人罚金
    let url = '/api/fine/';
    if (isAdmin(currentUser)) {
        const status = document.getElementById('statusFilter').value;
        if (status !== '') url += '?status=' + status;
    }

    apiFetch(url)
    .then(response => response.json())
    .then(list => {
        fines = Array.isArray(list) ? list : [];
        renderList();
        renderUnpaidTotal();
    })
    .catch(() => {
        fines = [];
        renderList();
        renderUnpaidTotal();
    });
}

function renderUnpaidTotal() {
    const total = fines
        .filter(f => f.username === currentUser.username && f.payStatus === 0)
        .reduce((sum, f) => sum + (parseFloat(f.amount) || 0), 0);
    document.getElementById('unpaidTotal').textContent = total.toFixed(2);
}

function renderList() {
    const tbody = document.getElementById('fineTable');
    tbody.innerHTML = '';

    if (fines.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align:center;">暂无数据</td></tr>';
        return;
    }

    fines.forEach(function(f) {
        const row = document.createElement('tr');
        const statusText = payStatusText[f.payStatus] || '未知';
        const statusBadge = payStatusBadge[f.payStatus] || 'badge-blue';

        let actions = [];
        if (f.payStatus === 0 && f.username === currentUser.username) {
            actions.push(`<button class="btn btn-success" onclick="payFine(${f.id})">缴纳</button>`);
        }
        if (f.payStatus === 0 && isAdmin(currentUser)) {
            actions.push(`<button class="btn btn-warning" onclick="showWaiveModal(${f.id})">减免</button>`);
        }

        row.innerHTML = `
            <td>${f.username || '-'}</td>
            <td>${f.bookTitle || '-'}</td>
            <td>${f.amount != null ? parseFloat(f.amount).toFixed(2) : '-'}</td>
            <td>${f.overdueDays != null ? f.overdueDays : '-'}</td>
            <td>${f.rate != null ? f.rate : '-'}</td>
            <td>${f.reason || '-'}</td>
            <td><span class="badge ${statusBadge}">${statusText}</span></td>
            <td>${formatTime(f.createTime)}</td>
            <td>${formatTime(f.payTime)}</td>
            <td>${actions.length ? actions.join('') : '-'}</td>
        `;
        tbody.appendChild(row);
    });
}

function payFine(id) {
    if (!confirm('确定要缴纳该罚金吗？')) return;

    post(`/api/fine/${id}/pay`, {})
    .then(data => {
        alert(data.message || (data.success ? '缴纳成功' : '缴纳失败'));
        if (data.success) {
            loadList();
        }
    })
    .catch(() => {
        alert('缴纳失败，请稍后重试');
    });
}

function showWaiveModal(id) {
    document.getElementById('waiveId').value = id;
    document.getElementById('waiveReason').value = '';
    document.getElementById('waiveModal').classList.add('show');
}

function closeModal() {
    document.getElementById('waiveModal').classList.remove('show');
}

function submitWaive(e) {
    e.preventDefault();
    const id = document.getElementById('waiveId').value;
    const reason = document.getElementById('waiveReason').value.trim();
    if (!reason) {
        alert('请填写减免原因');
        return;
    }

    post(`/api/fine/${id}/waive`, { reason: reason })
    .then(data => {
        alert(data.message || (data.success ? '已减免' : '操作失败'));
        if (data.success) {
            closeModal();
            loadList();
        }
    })
    .catch(() => {
        alert('操作失败，请稍后重试');
    });
}
