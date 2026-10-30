let currentUser = null;
let onlyUnread = false;

const noticeTypeText = { 1: '到期提醒', 2: '逾期提醒', 3: '预约取书', 4: '系统通知', 5: '荐购处理' };
const noticeTypeBadge = { 1: 'badge-blue', 2: 'badge-red', 3: 'badge-green', 4: 'badge-purple', 5: 'badge-orange' };

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

function loadList() {
    apiFetch('/api/notice/' + (onlyUnread ? '?unread=1' : ''))
    .then(response => response.json())
    .then(list => {
        renderList(Array.isArray(list) ? list : []);
    })
    .catch(() => {
        renderList([]);
    });
}

function renderList(notices) {
    const box = document.getElementById('noticeList');
    box.innerHTML = '';

    if (notices.length === 0) {
        box.innerHTML = '<div class="empty-state">暂无数据</div>';
        return;
    }

    notices.forEach(function(n) {
        const item = document.createElement('div');
        const unread = n.readFlag === 0 || n.readFlag === '0';
        item.className = 'notice-item' + (unread ? ' notice-unread' : '');

        const typeText = noticeTypeText[n.type] || '通知';
        const typeBadge = noticeTypeBadge[n.type] || 'badge-blue';
        const readBtn = unread ? `<button class="btn btn-primary" onclick="readOne(${n.id})">标记已读</button>` : '';

        item.innerHTML = `
            <div class="notice-head">
                <span class="badge ${typeBadge}">${typeText}</span>
                <span class="notice-title">${n.title || ''}</span>
                <span class="notice-time">${formatTime(n.createTime)}</span>
                ${readBtn}
            </div>
            <div class="notice-content">${n.content || ''}</div>
        `;
        box.appendChild(item);
    });
}

function toggleUnread() {
    onlyUnread = document.getElementById('unreadOnly').checked;
    loadList();
}

function readOne(id) {
    post(`/api/notice/${id}/read`, {})
    .then(data => {
        if (data.success) {
            loadList();
        } else {
            alert(data.message || '操作失败');
        }
    })
    .catch(() => {
        alert('操作失败，请稍后重试');
    });
}

function readAll() {
    post('/api/notice/read-all', {})
    .then(data => {
        alert(data.message || (data.success ? '已全部标记为已读' : '操作失败'));
        if (data.success) {
            loadList();
        }
    })
    .catch(() => {
        alert('操作失败，请稍后重试');
    });
}
