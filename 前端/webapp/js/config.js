// 后端 API 地址：前后端分离部署时改成后端实际地址
const API_BASE = 'http://localhost:8080';

// 四种角色常量，与后端 Roles.java / tb_user.role 保持一致
const Roles = { ADMIN: 1, STUDENT: 2, TEACHER: 3, SOCIAL: 0 };

// 统一把后端返回的 role（可能是 Number 或 String）转成 Number
function parseRole(val) {
    var n = parseInt(val, 10);
    return isNaN(n) ? -1 : n;
}

function roleName(val) {
    var r = parseRole(val);
    switch (r) {
        case Roles.ADMIN:   return '管理员';
        case Roles.TEACHER: return '教师';
        case Roles.STUDENT: return '线上学员';
        case Roles.SOCIAL:  return '社会人员';
        default:            return '未知';
    }
}

function roleTag(val) {
    var r = parseRole(val);
    switch (r) {
        case Roles.ADMIN:   return '(管理员)';
        case Roles.TEACHER: return '(教师)';
        case Roles.STUDENT: return '(学员)';
        case Roles.SOCIAL:  return '(社会)';
        default:            return '';
    }
}

// 角色判断：传入 user 对象或 role 数值均可，兼容 Number/String
function isAdmin(user) {   return user && parseRole(user.role) === Roles.ADMIN; }
function isTeacher(user) { return user && parseRole(user.role) === Roles.TEACHER; }
function isStudent(user) { return user && parseRole(user.role) === Roles.STUDENT; }
function isSocial(user) {  return user && parseRole(user.role) === Roles.SOCIAL; }

// 解析图书封面地址：相对路径（/upload/xxx）自动拼接后端地址，完整 URL 原样返回
function resolveImage(url) {
    if (!url) return '';
    if (/^https?:\/\//i.test(url)) return url;
    return API_BASE + url;
}

function getToken() {
    var t = localStorage.getItem('token') || '';
    // 旧版 mock 登录遗留的假 token 一律视为未登录
    if (t.indexOf('mock-') === 0) return '';
    return t;
}

// 统一 API 请求：自动拼 API_BASE、带 Authorization 头、401 跳登录
function apiFetch(path, options) {
    options = options || {};
    options.headers = options.headers || {};
    if (getToken()) options.headers['Authorization'] = 'Bearer ' + getToken();
    // 表单默认编码；FormData 不要手动设 Content-Type
    if (options.body && !(options.body instanceof FormData) && !options.headers['Content-Type']) {
        options.headers['Content-Type'] = 'application/x-www-form-urlencoded';
    }
    return fetch(API_BASE + path, options).then(function(resp) {
        if (resp.status === 401) {
            localStorage.removeItem('user');
            localStorage.removeItem('token');
            if (!/index\.html$/.test(window.location.pathname)) {
                window.location.href = 'index.html';
            }
            throw new Error('未登录');
        }
        return resp;
    });
}

// ============ 页面级权限守卫 ============

// 每个页面允许访问的角色 role 值数组（与 Roles 常量保持一致）
const PAGE_ROLES = {
    // 公开页
    'index.html':      'public',
    'register.html':   'public',
    'debug-login.html':'public',
    // 所有登录用户可访问
    'home.html':       [0,1,2,3],
    'book-search.html':[0,1,2,3],
    'borrow-list.html':[0,1,2,3],
    'borrow-apply.html':[0,1,2,3],
    'reserve.html':    [0,1,2,3],
    'fine.html':       [0,1,2,3],
    'notice.html':     [0,1,2,3],
    'profile.html':    [0,1,2,3],
    // 教师 + 学员
    'credit.html':     [1,2],        // 管理员 + 学员（教师/社会人员不能看信用分）
    'suggestion.html': [1,2],        // 管理员 + 学员（教师/社会人员不能提建议）
    // 教师 + 学员 + 社会人员
    'claim.html':      [1,2,3],      // 管理员 + 教师 + 学员（社会人员不能认领遗失）
    // 仅管理员
    'book-list.html':  [1],           // 图书 CRUD
    'dashboard.html':  [1],
    'user-list.html':  [1],
    'perm-list.html':  [1],
    'tier-list.html':  [1],
    'inventory.html':  [1],
    'log-list.html':   [1]
};

/**
 * 页面访问守卫：返回 true 表示允许继续，false 表示已拦截并跳走/弹窗。
 * 所有受限页面在 DOMContentLoaded 入口处调用：
 *   if (!guardAccess()) return;
 */
function guardAccess() {
    var page = (window.location.pathname.split('/').pop() || 'home.html').toLowerCase();
    var rule = PAGE_ROLES[page];

    // 未配置的页面 = 默认全部登录用户可访问 + 未登录跳登录
    if (!rule) rule = [0,1,2,3];

    // 公开页直接放行
    if (rule === 'public') return true;

    var user = localStorage.getItem('user');
    var token = getToken();
    if (!user || !token) {
        // 未登录 → 跳登录页（index.html 本身不走这里，因为 rule=public）
        if (page !== 'index.html') {
            window.location.href = 'index.html';
        }
        return false;
    }

    try { user = JSON.parse(user); } catch(e) { user = null; }

    var role = user ? parseRole(user.role) : -1;
    if (role === -1 || rule.indexOf(role) === -1) {
        // 跨身份拦截：弹出权限不足提示 + 记录审计日志
        showDeniedModal(user, page);
        reportAccess('DENIED', user, page);
        return false;
    }

    // 允许访问 + 记录登录后首次进入的审计
    reportAccess('ALLOWED', user, page);
    return true;
}

// 权限不足模态框（比 alert 更友好，不阻塞后续 UI）
function showDeniedModal(user, page) {
    var back = document.createElement('div');
    back.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,.5);' +
                          'display:flex;align-items:center;justify-content:center;z-index:99999;';
    var modal = document.createElement('div');
    modal.style.cssText = 'background:#fff;border-radius:12px;padding:28px 32px;' +
                          'min-width:320px;max-width:420px;text-align:center;' +
                          'box-shadow:0 20px 60px rgba(0,0,0,.3);';
    var roleName_ = user ? roleName(user.role) : '未登录身份';
    modal.innerHTML =
        '<div style="font-size:36px;margin-bottom:12px;">🚫</div>' +
        '<div style="font-size:18px;font-weight:600;margin-bottom:8px;color:#c0392b;">权限不足</div>' +
        '<div style="font-size:14px;color:#555;line-height:1.6;margin-bottom:20px;">' +
        '当前身份 <b style="color:#2c3e50;">' + roleName_ + '</b> 无权访问该功能窗口<br>' +
        '页面已被安全拦截，操作已记录到审计日志</div>' +
        '<button id="deniedBack" style="padding:8px 24px;border:none;border-radius:6px;' +
        'background:#3498db;color:#fff;font-size:14px;cursor:pointer;">返回首页</button>' +
        '<button id="deniedLogout" style="padding:8px 24px;border:1px solid #ccc;border-radius:6px;' +
        'background:#fff;color:#555;font-size:14px;cursor:pointer;margin-left:8px;">切换身份</button>';
    back.appendChild(modal);
    document.body.appendChild(back);
    document.getElementById('deniedBack').onclick = function() {
        window.location.href = 'home.html';
    };
    document.getElementById('deniedLogout').onclick = function() {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        window.location.href = 'index.html';
    };
    // 遮罩点击 = 返回首页
    back.addEventListener('click', function(e) {
        if (e.target === back) {
            document.body.removeChild(back);
            window.location.href = 'home.html';
        }
    });
}

/**
 * 访问审计上报：写入后端 tb_log（LogServlet POST 允许非管理员自己写）。
 * 页面级守卫每进入一次调一次；被拦截时也调一次（action=ACCESS_DENIED）。
 */
function reportAccess(action, user, page) {
    if (!user || !user.id) return;
    // 避免同一页面短时间内重复 ALLOWED 日志（30 秒去重）
    if (action === 'ALLOWED') {
        var key = '_rp_' + page;
        var last = parseInt(localStorage.getItem(key) || '0', 10);
        var now = Date.now();
        if (now - last < 30000) return;
        localStorage.setItem(key, String(now));
    }
    try {
        apiFetch('/api/log/', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                action: action,
                target: page,
                detail: (action === 'DENIED' ? '跨身份窗口拦截' : '窗口访问成功'),
                level: action === 'DENIED' ? 'WARN' : 'INFO'
            })
        }).catch(function() {}); // 审计失败不阻塞主流程
    } catch(e) {}
}
