let currentUser = null;
let tiers = [];

document.addEventListener('DOMContentLoaded', function() {
    if (!checkLogin()) {
        return;
    }
    loadList();
    document.getElementById('tierForm').addEventListener('submit', submitForm);
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
    apiFetch('/api/tier/')
    .then(response => response.json())
    .then(list => {
        tiers = Array.isArray(list) ? list : [];
        renderList();
    })
    .catch(() => {
        tiers = [];
        renderList();
    });
}

function renderList() {
    const tbody = document.getElementById('tierTable');
    tbody.innerHTML = '';

    if (tiers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align:center;">暂无数据</td></tr>';
        return;
    }

    tiers.forEach(function(t) {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${t.name || '-'}</td>
            <td>${t.minScore != null ? t.minScore : '-'}</td>
            <td>${t.maxBorrow != null ? t.maxBorrow : '-'}</td>
            <td>${t.days != null ? t.days : '-'}</td>
            <td>${t.renewMax != null ? t.renewMax : '-'}</td>
            <td>${t.renewDays != null ? t.renewDays : '-'}</td>
            <td>${t.priority != null ? t.priority : '-'}</td>
            <td>${t.rate != null ? Number(t.rate).toFixed(2) : '-'}</td>
            <td>${t.note || '-'}</td>
            <td>
                <button class="btn btn-edit" onclick="showEditModal(${t.id})">编辑</button>
                <button class="btn btn-delete" onclick="deleteTier(${t.id})">删除</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function showModal() {
    document.getElementById('modalTitle').textContent = '新增等级';
    document.getElementById('tierId').value = '';
    document.getElementById('tierForm').reset();
    document.getElementById('tierModal').classList.add('show');
}

function showEditModal(id) {
    const t = tiers.find(x => x.id === id);
    if (!t) return;

    document.getElementById('modalTitle').textContent = '编辑等级';
    document.getElementById('tierId').value = t.id;
    document.getElementById('tName').value = t.name || '';
    document.getElementById('tMinScore').value = t.minScore != null ? t.minScore : '';
    document.getElementById('tMaxBorrow').value = t.maxBorrow != null ? t.maxBorrow : '';
    document.getElementById('tDays').value = t.days != null ? t.days : '';
    document.getElementById('tRenewMax').value = t.renewMax != null ? t.renewMax : '';
    document.getElementById('tRenewDays').value = t.renewDays != null ? t.renewDays : '';
    document.getElementById('tPriority').value = t.priority != null ? t.priority : '';
    document.getElementById('tRate').value = t.rate != null ? t.rate : '';
    document.getElementById('tNote').value = t.note || '';
    document.getElementById('tierModal').classList.add('show');
}

function closeModal() {
    document.getElementById('tierModal').classList.remove('show');
}

function collectParams() {
    return {
        name: document.getElementById('tName').value.trim(),
        minScore: document.getElementById('tMinScore').value,
        maxBorrow: document.getElementById('tMaxBorrow').value,
        days: document.getElementById('tDays').value,
        renewMax: document.getElementById('tRenewMax').value,
        renewDays: document.getElementById('tRenewDays').value,
        priority: document.getElementById('tPriority').value,
        rate: document.getElementById('tRate').value,
        note: document.getElementById('tNote').value.trim()
    };
}

function submitForm(e) {
    e.preventDefault();
    const id = document.getElementById('tierId').value;
    const params = collectParams();

    let request;
    if (id) {
        params.id = id;
        const qs = new URLSearchParams(params).toString();
        request = apiFetch(`/api/tier/${id}?${qs}`, { method: 'PUT' }).then(response => response.json());
    } else {
        request = post('/api/tier/', params);
    }

    request.then(data => {
        alert(data.message || (data.success ? '保存成功' : '保存失败'));
        if (data.success) {
            closeModal();
            loadList();
        }
    })
    .catch(() => {
        alert('保存失败，请稍后重试');
    });
}

function deleteTier(id) {
    if (!confirm('确定要删除该等级吗？')) return;

    apiFetch(`/api/tier/${id}`, { method: 'DELETE' })
    .then(response => response.json())
    .then(data => {
        alert(data.message || (data.success ? '删除成功' : '删除失败'));
        if (data.success) {
            loadList();
        }
    })
    .catch(() => {
        alert('删除失败，请稍后重试');
    });
}
