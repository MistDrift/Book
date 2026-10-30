let currentUser = null;
let inventories = [];
let currentInvId = null;
let currentInvRunning = false;

const invTypeText = { 1: '全盘', 2: '区域盘点', 3: '抽样盘点' };
const invStatusText = { 0: '待开始', 1: '进行中', 2: '已完成', 3: '已取消' };
const invStatusBadge = { 0: 'badge-orange', 1: 'badge-blue', 2: 'badge-green', 3: 'badge-purple' };
const sysStatusText = { 1: '在馆', 2: '借出', 3: '遗失', 4: '维修' };
const realStatusText = { 1: '在架', 2: '遗失', 3: '破损', 4: '错架' };

document.addEventListener('DOMContentLoaded', function() {
    if (!checkLogin()) {
        return;
    }
    loadList();
    document.getElementById('inventoryForm').addEventListener('submit', submitForm);
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

function post(url, params) {
    return apiFetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(params).toString()
    }).then(response => response.json());
}

function loadList() {
    apiFetch('/api/inventory/')
    .then(response => response.json())
    .then(list => {
        inventories = Array.isArray(list) ? list : [];
        renderList();
    })
    .catch(() => {
        inventories = [];
        renderList();
    });
}

function renderList() {
    const tbody = document.getElementById('inventoryTable');
    tbody.innerHTML = '';

    if (inventories.length === 0) {
        tbody.innerHTML = '<tr><td colspan="12" style="text-align:center;">暂无数据</td></tr>';
        return;
    }

    inventories.forEach(function(inv) {
        const row = document.createElement('tr');
        const statusText = invStatusText[inv.status] || '未知';
        const statusBadge = invStatusBadge[inv.status] || 'badge-blue';

        let actions = [];
        if (inv.status === 0) {
            actions.push(`<button class="btn btn-success" onclick="start(${inv.id})">开始</button>`);
            actions.push(`<button class="btn btn-danger" onclick="cancel(${inv.id})">取消</button>`);
        } else if (inv.status === 1) {
            actions.push(`<button class="btn btn-info" onclick="showItems(${inv.id}, '${(inv.name || '').replace(/'/g, '')}')">盘点录入</button>`);
            actions.push(`<button class="btn btn-warning" onclick="finish(${inv.id})">完成结算</button>`);
            actions.push(`<button class="btn btn-danger" onclick="cancel(${inv.id})">取消</button>`);
        }

        row.innerHTML = `
            <td>${inv.name || '-'}</td>
            <td>${invTypeText[inv.type] || '-'}</td>
            <td>${inv.area || '-'}</td>
            <td>${inv.total != null ? inv.total : '-'}</td>
            <td>${inv.actual != null ? inv.actual : '-'}</td>
            <td>${inv.missing != null ? inv.missing : '-'}</td>
            <td>${inv.extra != null ? inv.extra : '-'}</td>
            <td><span class="badge ${statusBadge}">${statusText}</span></td>
            <td>${formatTime(inv.startTime)}</td>
            <td>${formatTime(inv.endTime)}</td>
            <td>${inv.creatorName || '-'}</td>
            <td>${actions.length ? actions.join('') : '-'}</td>
        `;
        tbody.appendChild(row);
    });
}

function toggleArea() {
    const type = document.getElementById('invType').value;
    document.getElementById('invArea').required = (type === '2');
}

function showModal() {
    document.getElementById('invName').value = '';
    document.getElementById('invType').value = '1';
    document.getElementById('invArea').value = '';
    toggleArea();
    document.getElementById('inventoryModal').classList.add('show');
}

function closeModal() {
    document.getElementById('inventoryModal').classList.remove('show');
}

function submitForm(e) {
    e.preventDefault();
    const name = document.getElementById('invName').value.trim();
    const type = document.getElementById('invType').value;
    const area = document.getElementById('invArea').value.trim();

    if (type === '2' && !area) {
        alert('区域盘点必须填写盘点区域');
        return;
    }

    post('/api/inventory/create', { name: name, type: type, area: area })
    .then(data => {
        alert(data.message || (data.success ? '创建成功' : '创建失败'));
        if (data.success) {
            closeModal();
            loadList();
        }
    })
    .catch(() => {
        alert('创建失败，请稍后重试');
    });
}

function start(id) {
    if (!confirm('确定要开始该盘点任务吗？')) return;

    post(`/api/inventory/${id}/start`, {})
    .then(data => {
        alert(data.message || (data.success ? '已开始' : '操作失败'));
        if (data.success) {
            loadList();
        }
    })
    .catch(() => {
        alert('操作失败，请稍后重试');
    });
}

function finish(id) {
    if (!confirm('完成结算后将生成盘点结果，确定完成该盘点吗？')) return;

    post(`/api/inventory/${id}/finish`, {})
    .then(data => {
        alert(data.message || (data.success ? '已完成结算' : '操作失败'));
        if (data.success) {
            document.getElementById('itemsPanel').style.display = 'none';
            currentInvId = null;
            loadList();
        }
    })
    .catch(() => {
        alert('操作失败，请稍后重试');
    });
}

function cancel(id) {
    if (!confirm('确定要取消该盘点任务吗？')) return;

    post(`/api/inventory/${id}/cancel`, {})
    .then(data => {
        alert(data.message || (data.success ? '已取消' : '操作失败'));
        if (data.success) {
            document.getElementById('itemsPanel').style.display = 'none';
            currentInvId = null;
            loadList();
        }
    })
    .catch(() => {
        alert('操作失败，请稍后重试');
    });
}

function showItems(id, name) {
    currentInvId = id;
    const inv = inventories.find(i => i.id === id);
    currentInvRunning = inv && inv.status === 1;

    document.getElementById('itemsTitle').textContent = '盘点明细 - ' + (name || '');
    document.getElementById('itemsPanel').style.display = 'block';

    apiFetch(`/api/inventory/${id}/items`)
    .then(response => response.json())
    .then(items => {
        renderItems(Array.isArray(items) ? items : []);
    })
    .catch(() => {
        renderItems([]);
    });
}

function renderItems(items) {
    const tbody = document.getElementById('itemsTable');
    tbody.innerHTML = '';

    if (items.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">暂无数据</td></tr>';
        return;
    }

    items.forEach(function(item) {
        const row = document.createElement('tr');

        let options = '<option value="">未盘</option>';
        Object.keys(realStatusText).forEach(function(k) {
            const selected = item.realStatus === parseInt(k) ? 'selected' : '';
            options += `<option value="${k}" ${selected}>${realStatusText[k]}</option>`;
        });

        const disabled = currentInvRunning ? '' : 'disabled';
        const saveBtn = currentInvRunning
            ? `<button class="btn btn-success" onclick="saveItem(${item.bookId})">保存</button>`
            : '-';

        row.innerHTML = `
            <td>${item.isbn || '-'}</td>
            <td>${item.title || '-'}</td>
            <td>${sysStatusText[item.sysStatus] || '-'}</td>
            <td><select id="rs_${item.bookId}" ${disabled}>${options}</select></td>
            <td><input type="text" id="rm_${item.bookId}" value="${item.remark || ''}" ${disabled}></td>
            <td>${formatTime(item.checkedTime)}</td>
            <td>${saveBtn}</td>
        `;
        tbody.appendChild(row);
    });
}

function saveItem(bookId) {
    const realStatus = document.getElementById('rs_' + bookId).value;
    const remark = document.getElementById('rm_' + bookId).value;

    if (!realStatus) {
        alert('请选择实盘状态');
        return;
    }

    post(`/api/inventory/${currentInvId}/check`, { bookId: bookId, realStatus: realStatus, remark: remark })
    .then(data => {
        alert(data.message || (data.success ? '已保存' : '保存失败'));
        if (data.success) {
            showItems(currentInvId, document.getElementById('itemsTitle').textContent.replace('盘点明细 - ', ''));
            loadList();
        }
    })
    .catch(() => {
        alert('保存失败，请稍后重试');
    });
}
