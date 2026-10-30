let currentUser = null;
let suggestions = [];

const sgTypeText = { 1: '补库存', 2: '新增图书' };
const sgStatusText = { 0: '待审核', 1: '已采纳', 2: '已驳回', 3: '采购中', 4: '已到馆' };
const sgStatusBadge = { 0: 'badge-orange', 1: 'badge-blue', 2: 'badge-red', 3: 'badge-purple', 4: 'badge-green' };

document.addEventListener('DOMContentLoaded', function() {
    if (!checkLogin()) {
        return;
    }
    loadList();
    document.getElementById('suggestionForm').addEventListener('submit', submitForm);
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

function post(url, params) {
    return apiFetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(params).toString()
    }).then(response => response.json());
}

function loadList() {
    apiFetch('/api/suggestion/')
    .then(response => response.json())
    .then(list => {
        suggestions = Array.isArray(list) ? list : [];
        renderList();
    })
    .catch(() => {
        suggestions = [];
        renderList();
    });
}

function renderList() {
    const tbody = document.getElementById('suggestionTable');
    tbody.innerHTML = '';

    if (suggestions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="11" style="text-align:center;">暂无数据</td></tr>';
        return;
    }

    suggestions.forEach(function(s) {
        const row = document.createElement('tr');
        const statusText = sgStatusText[s.status] || '未知';
        const statusBadge = sgStatusBadge[s.status] || 'badge-blue';

        let actions = [`<button class="btn btn-info" onclick="vote(${s.id})">点赞(${s.votes || 0})</button>`];
        if (isAdmin(currentUser)) {
            if (s.status === 0) {
                actions.push(`<button class="btn btn-success" onclick="handle(${s.id}, 'adopt')">采纳</button>`);
                actions.push(`<button class="btn btn-danger" onclick="handle(${s.id}, 'reject')">驳回</button>`);
            } else if (s.status === 1) {
                actions.push(`<button class="btn btn-warning" onclick="handle(${s.id}, 'purchase')">采购</button>`);
            } else if (s.status === 3) {
                actions.push(`<button class="btn btn-success" onclick="handle(${s.id}, 'arrive')">到馆</button>`);
            }
        }

        row.innerHTML = `
            <td>${s.username || '-'}</td>
            <td>${sgTypeText[s.type] || '-'}</td>
            <td>${s.title || '-'}</td>
            <td>${s.author || '-'}</td>
            <td>${s.publisher || '-'}</td>
            <td>${s.price != null && s.price !== '' ? parseFloat(s.price).toFixed(2) : '-'}</td>
            <td>${s.reason || '-'}</td>
            <td><span class="badge ${statusBadge}">${statusText}</span></td>
            <td>${s.votes || 0}</td>
            <td>${s.handleRemark || '-'}</td>
            <td>${actions.join('')}</td>
        `;
        tbody.appendChild(row);
    });
}

function showModal() {
    document.getElementById('suggestionForm').reset();
    document.getElementById('suggestionModal').classList.add('show');
}

function closeModal() {
    document.getElementById('suggestionModal').classList.remove('show');
}

function submitForm(e) {
    e.preventDefault();

    const params = {
        type: document.getElementById('sgType').value,
        title: document.getElementById('sgTitle').value.trim(),
        author: document.getElementById('sgAuthor').value.trim(),
        publisher: document.getElementById('sgPublisher').value.trim(),
        isbn: document.getElementById('sgIsbn').value.trim(),
        category: document.getElementById('sgCategory').value.trim(),
        price: document.getElementById('sgPrice').value,
        reason: document.getElementById('sgReason').value.trim()
    };

    if (!params.title) {
        alert('书名必填');
        return;
    }

    post('/api/suggestion/submit', params)
    .then(data => {
        alert(data.message || (data.success ? '提交成功' : '提交失败'));
        if (data.success) {
            closeModal();
            loadList();
        }
    })
    .catch(() => {
        alert('提交失败，请稍后重试');
    });
}

function vote(id) {
    post(`/api/suggestion/${id}/vote`, {})
    .then(data => {
        alert(data.message || (data.success ? '点赞成功' : '点赞失败'));
        if (data.success) {
            loadList();
        }
    })
    .catch(() => {
        alert('点赞失败，请稍后重试');
    });
}

function handle(id, action) {
    let remark = '';
    if (action === 'reject') {
        remark = prompt('请输入驳回原因：');
        if (remark === null) return;
        remark = remark.trim();
        if (!remark) {
            alert('驳回原因必填');
            return;
        }
    } else {
        const actionText = { adopt: '采纳', purchase: '采购', arrive: '确认到馆' }[action] || action;
        if (!confirm(`确定要${actionText}该荐购吗？`)) return;
    }

    post(`/api/suggestion/${id}/handle`, { action: action, remark: remark })
    .then(data => {
        alert(data.message || (data.success ? '处理成功' : '处理失败'));
        if (data.success) {
            loadList();
        }
    })
    .catch(() => {
        alert('处理失败，请稍后重试');
    });
}
