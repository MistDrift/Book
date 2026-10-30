let currentUser = null;
let days = 30;
let charts = {};

document.addEventListener('DOMContentLoaded', function() {
    if (!checkLogin()) {
        return;
    }
    loadDashboard();
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

function setDays(d) {
    days = d;
    document.querySelectorAll('.tab').forEach(function(t) {
        t.classList.toggle('active', parseInt(t.dataset.days) === d);
    });
    loadDashboard();
}

function loadDashboard() {
    apiFetch('/api/dashboard/?days=' + days)
    .then(response => response.json())
    .then(data => {
        renderDashboard(data);
    })
    .catch(() => {
        alert('数据加载失败');
    });
}

function renderDashboard(data) {
    const summary = data.summary || {};
    document.getElementById('stBook').textContent = summary.bookCount != null ? summary.bookCount : '-';
    document.getElementById('stUser').textContent = summary.userCount != null ? summary.userCount : '-';
    document.getElementById('stBorrowing').textContent = summary.borrowing != null ? summary.borrowing : '-';
    document.getElementById('stOverdue').textContent = summary.overdue != null ? summary.overdue : '-';
    document.getElementById('stReserve').textContent = summary.reserveActive != null ? summary.reserveActive : '-';
    document.getElementById('stFine').textContent = summary.unpaidFine != null ? summary.unpaidFine : '-';

    const fines = data.fines || {};
    document.getElementById('fineUnpaidAmount').textContent = fines.unpaidAmount != null ? fines.unpaidAmount : '0';
    document.getElementById('fineUnpaidCount').textContent = fines.unpaidCount != null ? fines.unpaidCount : '0';
    document.getElementById('fineCollected').textContent = fines.collected != null ? fines.collected : '0';
    document.getElementById('borrowTotal').textContent = data.borrowTotal != null ? data.borrowTotal : '0';

    renderRecent(data.recent || []);
    renderCharts(data);
}

function renderRecent(recent) {
    const tbody = document.getElementById('recentTable');
    tbody.innerHTML = '';

    if (!Array.isArray(recent) || recent.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;">暂无数据</td></tr>';
        return;
    }

    recent.forEach(function(r) {
        const row = document.createElement('tr');
        const statusText = r.status === 1 ? '已归还' : '借阅中';
        const statusClass = r.status === 1 ? 'badge badge-green' : 'badge badge-blue';
        row.innerHTML = `
            <td>${r.username || '-'}</td>
            <td>${r.bookTitle || '-'}</td>
            <td>${formatDate(r.borrowDate)}</td>
            <td><span class="${statusClass}">${statusText}</span></td>
        `;
        tbody.appendChild(row);
    });
}

const palette = ['#3498db', '#e74c3c', '#27ae60', '#f39c12', '#9b59b6', '#16a085', '#667eea', '#e67e22', '#2ecc71', '#95a5a6'];

function drawChart(id, config) {
    if (charts[id]) {
        charts[id].destroy();
    }
    charts[id] = new Chart(document.getElementById(id), config);
}

function renderFallback(fbId, rows, labelName, showPct) {
    const fb = document.getElementById(fbId);
    if (!Array.isArray(rows) || rows.length === 0) {
        fb.innerHTML = '<div class="empty-state">暂无数据</div>';
        return;
    }
    let html = '<table class="table"><thead><tr><th>' + labelName + '</th><th>数量</th>' +
               (showPct ? '<th>占比</th>' : '') + '</tr></thead><tbody>';
    rows.forEach(function(r) {
        html += '<tr><td>' + r.label + '</td><td>' + r.value + '</td>' +
                (showPct ? '<td>' + (r.pct != null ? r.pct : 0) + '%</td>' : '') + '</tr>';
    });
    html += '</tbody></table>';
    fb.innerHTML = html;
}

function renderCharts(data) {
    const trend = (data.trend || []).map(function(t) { return { label: t.d, value: t.c }; });
    const categories = (data.categories || []).map(function(t) { return { label: t.category, value: t.c }; });
    const topBooks = (data.topBooks || []).map(function(t) { return { label: t.title, value: t.c }; });
    const roles = (data.roles || []).map(function(t) { return { label: t.name, value: t.c, pct: t.pct }; });

    if (typeof Chart === 'undefined') {
        document.querySelectorAll('.chart-canvas').forEach(function(el) {
            el.style.display = 'none';
        });
        renderFallback('fbTrend', trend, '日期');
        renderFallback('fbCategory', categories, '分类');
        renderFallback('fbTop', topBooks, '图书');
        renderFallback('fbTier', roles, '身份', true);
        return;
    }

    document.querySelectorAll('.chart-canvas').forEach(function(el) {
        el.style.display = '';
    });
    document.getElementById('fbTrend').innerHTML = '';
    document.getElementById('fbCategory').innerHTML = '';
    document.getElementById('fbTop').innerHTML = '';
    document.getElementById('fbTier').innerHTML = '';

    drawChart('chartTrend', {
        type: 'line',
        data: {
            labels: trend.map(t => t.label),
            datasets: [{
                label: '借阅量',
                data: trend.map(t => t.value),
                borderColor: '#3498db',
                backgroundColor: 'rgba(52, 152, 219, 0.1)',
                fill: true,
                tension: 0.3
            }]
        },
        options: { responsive: true, maintainAspectRatio: false }
    });

    drawChart('chartCategory', {
        type: 'pie',
        data: {
            labels: categories.map(t => t.label),
            datasets: [{
                data: categories.map(t => t.value),
                backgroundColor: palette
            }]
        },
        options: { responsive: true, maintainAspectRatio: false }
    });

    drawChart('chartTop', {
        type: 'bar',
        data: {
            labels: topBooks.map(t => t.label),
            datasets: [{
                label: '借阅次数',
                data: topBooks.map(t => t.value),
                backgroundColor: '#667eea'
            }]
        },
        options: { responsive: true, maintainAspectRatio: false, indexAxis: 'y' }
    });

    drawChart('chartTier', {
        type: 'pie',
        data: {
            labels: roles.map(t => t.label + '（' + (t.pct != null ? t.pct : 0) + '%）'),
            datasets: [{
                label: '用户数',
                data: roles.map(t => t.value),
                backgroundColor: ['#3498db', '#27ae60']
            }]
        },
        options: { responsive: true, maintainAspectRatio: false }
    });
}
