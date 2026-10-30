let currentUser = null;
let claims = [];

const claimTypeText = { 1: '原价赔偿', 2: '折旧赔偿', 3: '原书购买' };
const claimStatusText = { 0: '待处理', 1: '处理中', 2: '已结案', 3: '已驳回' };
const claimStatusBadge = { 0: 'badge-orange', 1: 'badge-blue', 2: 'badge-green', 3: 'badge-red' };

document.addEventListener('DOMContentLoaded', function() {
    if (!checkLogin()) {
        return;
    }
    loadList();
    document.getElementById('reportForm').addEventListener('submit', submitForm);
    document.getElementById('handleForm').addEventListener('submit', submitHandle);
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
    apiFetch('/api/claim/')
    .then(response => response.json())
    .then(list => {
        claims = Array.isArray(list) ? list : [];
        renderList();
    })
    .catch(() => {
        claims = [];
        renderList();
    });
}

function renderList() {
    const tbody = document.getElementById('claimTable');
    tbody.innerHTML = '';

    if (claims.length === 0) {
        tbody.innerHTML = '<tr><td colspan="12" style="text-align:center;">暂无数据</td></tr>';
        return;
    }

    claims.forEach(function(c) {
        const row = document.createElement('tr');
        const statusText = claimStatusText[c.status] || '未知';
        const statusBadge = claimStatusBadge[c.status] || 'badge-blue';

        let actions = [];
        if (isAdmin(currentUser) && c.status === 0) {
            actions.push(`<button class="btn btn-success" onclick="showHandleModal(${c.id}, 'approve')">结案</button>`);
            actions.push(`<button class="btn btn-danger" onclick="showHandleModal(${c.id}, 'reject')">驳回</button>`);
        }

        row.innerHTML = `
            <td>${c.username || '-'}</td>
            <td>${c.bookTitle || '-'}</td>
            <td>${formatDate(c.reportDate)}</td>
            <td>${claimTypeText[c.type] || '-'}</td>
            <td>${c.bookPrice != null ? parseFloat(c.bookPrice).toFixed(2) : '-'}</td>
            <td>${c.depRate != null ? c.depRate : '-'}</td>
            <td>${c.amount != null ? parseFloat(c.amount).toFixed(2) : '-'}</td>
            <td>${c.paid != null ? parseFloat(c.paid).toFixed(2) : '-'}</td>
            <td><span class="badge ${statusBadge}">${statusText}</span></td>
            <td>${c.handleRemark || '-'}</td>
            <td>${formatTime(c.payTime)}</td>
            <td>${actions.length ? actions.join('') : '-'}</td>
        `;
        tbody.appendChild(row);
    });
}

function showModal() {
    const select = document.getElementById('borrowSelect');
    select.innerHTML = '<option value="">加载中...</option>';
    document.getElementById('reportModal').classList.add('show');

    apiFetch('/api/borrow/')
    .then(response => response.json())
    .then(borrows => {
        const list = (Array.isArray(borrows) ? borrows : []).filter(b => b.status === 0 && b.username === currentUser.username);
        select.innerHTML = '';
        if (list.length === 0) {
            select.innerHTML = '<option value="" disabled>暂无借阅中的图书</option>';
            return;
        }
        list.forEach(function(b) {
            const opt = document.createElement('option');
            opt.value = b.id;
            opt.textContent = b.bookTitle || ('借阅记录#' + b.id);
            select.appendChild(opt);
        });
    })
    .catch(() => {
        select.innerHTML = '<option value="" disabled>借阅记录加载失败</option>';
    });
}

function closeModal() {
    document.getElementById('reportModal').classList.remove('show');
}

function submitForm(e) {
    e.preventDefault();
    const borrowId = document.getElementById('borrowSelect').value;
    const type = document.getElementById('typeSelect').value;
    if (!borrowId) {
        alert('请选择要报失的图书');
        return;
    }

    post('/api/claim/report', { borrowId: borrowId, type: type })
    .then(data => {
        alert(data.message || (data.success ? '报失成功' : '报失失败'));
        if (data.success) {
            closeModal();
            loadList();
        }
    })
    .catch(() => {
        alert('报失失败，请稍后重试');
    });
}

function showHandleModal(id, action) {
    document.getElementById('handleId').value = id;
    document.getElementById('handleAction').value = action;
    document.getElementById('handleRemark').value = '';
    document.getElementById('paidInput').value = '';

    if (action === 'approve') {
        document.getElementById('handleTitle').textContent = '结案处理';
        document.getElementById('paidGroup').style.display = 'block';
        document.getElementById('paidInput').required = true;
    } else {
        document.getElementById('handleTitle').textContent = '驳回理赔';
        document.getElementById('paidGroup').style.display = 'none';
        document.getElementById('paidInput').required = false;
    }

    document.getElementById('handleModal').classList.add('show');
}

function closeHandleModal() {
    document.getElementById('handleModal').classList.remove('show');
}

function submitHandle(e) {
    e.preventDefault();
    const id = document.getElementById('handleId').value;
    const action = document.getElementById('handleAction').value;
    const remark = document.getElementById('handleRemark').value.trim();

    if (!remark) {
        alert('处理备注必填');
        return;
    }

    const params = { action: action, remark: remark };
    if (action === 'approve') {
        const paid = document.getElementById('paidInput').value;
        if (paid === '') {
            alert('请填写实赔金额');
            return;
        }
        params.paid = paid;
    }

    post(`/api/claim/${id}/handle`, params)
    .then(data => {
        alert(data.message || (data.success ? '处理成功' : '处理失败'));
        if (data.success) {
            closeHandleModal();
            loadList();
        }
    })
    .catch(() => {
        alert('处理失败，请稍后重试');
    });
}
