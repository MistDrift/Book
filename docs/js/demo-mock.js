/* =====================================================================
 * 纯前端演示数据层（GitHub Pages 演示版）
 * 拦截所有 /api/* 请求，返回内存假数据；不依赖后端、不请求外部网络。
 * 加载顺序：config.js 之后、各页面自己的 js 之前。
 * 数据仅保存在内存中，刷新页面后恢复初始值。
 * ===================================================================== */
(function () {
    'use strict';

    if (window.__DEMO_MOCK_READY__) return;
    window.__DEMO_MOCK_READY__ = true;

    var DEMO_ORIGIN = 'http://localhost:8080';
    var DAY = 24 * 60 * 60 * 1000;
    var originalFetch = window.fetch ? window.fetch.bind(window) : null;

    function ago(n) { return Date.now() - n * DAY; }

    function json(data, status) {
        return new Response(JSON.stringify(data), {
            status: status || 200,
            headers: { 'Content-Type': 'application/json;charset=utf-8' }
        });
    }
    function ok(msg) {
        return json({ success: true, message: msg || '操作成功（演示数据）' });
    }
    function bad(msg) {
        return json({ success: false, message: msg || '操作失败（演示数据）' });
    }
    function denied() {
        return json({ success: false, message: '权限不足（演示环境）' }, 403);
    }

    // ---------------- 图书（20 本，封面取 docs/upload 实存文件 book19~book38）----------------
    var BOOKS = [
        { id: 1, isbn: '9787111213826', title: 'Java编程思想', author: 'Bruce Eckel', publisher: '机械工业出版社', publishDate: '2007-06-01', category: '编程', price: 108.00, stock: 5, total: 8, location: 'A区-01架', image: '/upload/book19.jpg', description: 'Java 学习必读经典，覆盖语法、并发与泛型。', intro: 'Java 学习必读经典，覆盖语法、并发与泛型。' },
        { id: 2, isbn: '9787115428577', title: 'Python编程：从入门到实践', author: 'Eric Matthes', publisher: '人民邮电出版社', publishDate: '2016-07-01', category: '编程', price: 79.00, stock: 10, total: 12, location: 'A区-02架', image: '/upload/book20.jpg', description: '零基础入门 Python 的实战教程。', intro: '零基础入门 Python 的实战教程。' },
        { id: 3, isbn: '9787111544937', title: '深入理解计算机系统', author: 'Randal E.Bryant', publisher: '机械工业出版社', publishDate: '2015-11-01', category: '计算机', price: 129.00, stock: 3, total: 6, location: 'A区-03架', image: '/upload/book21.jpg', description: '从程序员视角剖析计算机系统。', intro: '从程序员视角剖析计算机系统。' },
        { id: 4, isbn: '9787115461476', title: '深度学习入门', author: '斋藤康毅', publisher: '人民邮电出版社', publishDate: '2017-05-01', category: 'AI', price: 69.00, stock: 8, total: 10, location: 'B区-01架', image: '/upload/book22.jpg', description: '基于 Python 的深度学习入门读物。', intro: '基于 Python 的深度学习入门读物。' },
        { id: 5, isbn: '9787111075547', title: '设计模式', author: 'Erich Gamma', publisher: '机械工业出版社', publishDate: '2002-09-01', category: '编程', price: 59.00, stock: 4, total: 6, location: 'A区-04架', image: '/upload/book23.jpg', description: 'GoF 二十三种设计模式经典。', intro: 'GoF 二十三种设计模式经典。' },
        { id: 6, isbn: '9787111407010', title: '算法导论', author: 'Thomas H.Cormen', publisher: '机械工业出版社', publishDate: '2012-12-01', category: '算法', price: 128.00, stock: 2, total: 5, location: 'A区-05架', image: '/upload/book24.jpg', description: '算法领域权威教材。', intro: '算法领域权威教材。' },
        { id: 7, isbn: '9787115275790', title: 'JavaScript高级程序设计', author: 'Nicholas C.Zakas', publisher: '人民邮电出版社', publishDate: '2012-03-01', category: '编程', price: 99.00, stock: 6, total: 8, location: 'A区-06架', image: '/upload/book25.jpg', description: '前端开发红宝书。', intro: '前端开发红宝书。' },
        { id: 8, isbn: '9787111234909', title: '数据结构与算法分析', author: 'Mark Allen Weiss', publisher: '机械工业出版社', publishDate: '2014-01-01', category: '算法', price: 69.00, stock: 5, total: 7, location: 'A区-07架', image: '/upload/book26.jpg', description: '数据结构与算法分析教材。', intro: '数据结构与算法分析教材。' },
        { id: 9, isbn: '9787111453781', title: '计算机网络：自顶向下方法', author: 'James F.Kurose', publisher: '机械工业出版社', publishDate: '2013-06-01', category: '网络', price: 89.00, stock: 3, total: 5, location: 'C区-01架', image: '/upload/book27.jpg', description: '自顶向下的计算机网络教材。', intro: '自顶向下的计算机网络教材。' },
        { id: 10, isbn: '9787111321330', title: '操作系统概念', author: 'Abraham Silberschatz', publisher: '高等教育出版社', publishDate: '2010-06-01', category: '操作系统', price: 79.00, stock: 4, total: 6, location: 'C区-02架', image: '/upload/book28.jpg', description: '操作系统原理经典教材。', intro: '操作系统原理经典教材。' },
        { id: 11, isbn: '9787111251217', title: '编译原理', author: 'Alfred V.Aho', publisher: '机械工业出版社', publishDate: '2008-12-01', category: '编译', price: 68.00, stock: 0, total: 4, location: 'C区-03架', image: '/upload/book29.jpg', description: '龙书，编译原理权威著作。', intro: '龙书，编译原理权威著作。' },
        { id: 12, isbn: '9787111320900', title: '人工智能：一种现代的方法', author: 'Stuart Russell', publisher: '人民邮电出版社', publishDate: '2010-05-01', category: 'AI', price: 128.00, stock: 0, total: 4, location: 'B区-02架', image: '/upload/book30.jpg', description: '人工智能领域经典教材。', intro: '人工智能领域经典教材。' },
        { id: 13, isbn: '9787115480114', title: 'C++ Primer', author: 'Stanley B.Lippman', publisher: '电子工业出版社', publishDate: '2017-09-01', category: '编程', price: 128.00, stock: 4, total: 6, location: 'A区-08架', image: '/upload/book31.jpg', description: 'C++ 经典教程。', intro: 'C++ 经典教程。' },
        { id: 14, isbn: '9787111275554', title: 'Effective Java', author: 'Joshua Bloch', publisher: '机械工业出版社', publishDate: '2018-01-01', category: '编程', price: 69.00, stock: 5, total: 7, location: 'A区-09架', image: '/upload/book32.jpg', description: 'Java 最佳实践指南。', intro: 'Java 最佳实践指南。' },
        { id: 15, isbn: '9787508353937', title: 'Head First设计模式', author: 'Eric Freeman', publisher: '中国电力出版社', publishDate: '2007-09-01', category: '编程', price: 99.00, stock: 3, total: 5, location: 'A区-10架', image: '/upload/book33.jpg', description: '图文并茂的设计模式入门。', intro: '图文并茂的设计模式入门。' },
        { id: 16, isbn: '9787115423474', title: 'Go语言实战', author: 'William Kennedy', publisher: '人民邮电出版社', publishDate: '2017-03-01', category: '编程', price: 89.00, stock: 4, total: 6, location: 'A区-11架', image: '/upload/book34.jpg', description: 'Go 语言工程实践。', intro: 'Go 语言工程实践。' },
        { id: 17, isbn: '9787115447587', title: 'Redis设计与实现', author: '黄健宏', publisher: '人民邮电出版社', publishDate: '2014-06-01', category: '数据库', price: 79.00, stock: 3, total: 5, location: 'D区-01架', image: '/upload/book35.jpg', description: 'Redis 内部设计与实现原理。', intro: 'Redis 内部设计与实现原理。' },
        { id: 18, isbn: '9787111340752', title: 'MySQL技术内幕', author: '姜承尧', publisher: '机械工业出版社', publishDate: '2013-05-01', category: '数据库', price: 69.00, stock: 4, total: 6, location: 'D区-02架', image: '/upload/book36.jpg', description: 'InnoDB 存储引擎剖析。', intro: 'InnoDB 存储引擎剖析。' },
        { id: 19, isbn: '9787111498847', title: 'Spring实战', author: 'Craig Walls', publisher: '人民邮电出版社', publishDate: '2016-08-01', category: '编程', price: 89.00, stock: 5, total: 7, location: 'A区-12架', image: '/upload/book37.jpg', description: 'Spring 框架实战指南。', intro: 'Spring 框架实战指南。' },
        { id: 20, isbn: '9787115485706', title: 'Vue.js实战', author: '梁灏', publisher: '人民邮电出版社', publishDate: '2017-11-01', category: '编程', price: 79.00, stock: 6, total: 8, location: 'A区-13架', image: '/upload/book38.jpg', description: 'Vue.js 入门与工程实践。', intro: 'Vue.js 入门与工程实践。' }
    ];

    // ---------------- 借阅等级（5 条）----------------
    var TIERS = [
        { id: 1, name: '社会人员', minScore: 0, maxBorrow: 3, days: 30, renewMax: 1, renewDays: 15, priority: 1, rate: 0.50, note: '默认等级，面向社会读者' },
        { id: 2, name: '普通学员', minScore: 60, maxBorrow: 5, days: 30, renewMax: 2, renewDays: 15, priority: 2, rate: 0.30, note: '线上学员默认等级' },
        { id: 3, name: '教师', minScore: 70, maxBorrow: 8, days: 60, renewMax: 2, renewDays: 30, priority: 3, rate: 0.20, note: '教师专属等级' },
        { id: 4, name: '高级读者', minScore: 85, maxBorrow: 10, days: 60, renewMax: 3, renewDays: 30, priority: 4, rate: 0.10, note: '信用优秀读者' },
        { id: 5, name: '馆员', minScore: 95, maxBorrow: 15, days: 90, renewMax: 3, renewDays: 45, priority: 5, rate: 0.00, note: '工作人员借阅等级' }
    ];

    // ---------------- 用户（7 个演示账号）----------------
    var USERS = [
        { id: 1, username: 'admin', name: '系统管理员', role: 1, email: 'admin@lib.demo', phone: '13800000001', status: 1, tierId: 5, tierName: '馆员', credit: 100, creditScore: 100, createTime: ago(400) },
        { id: 2, username: 'teacher1', name: '王老师', role: 3, email: 'teacher1@lib.demo', phone: '13800000002', status: 1, tierId: 3, tierName: '教师', credit: 92, creditScore: 92, createTime: ago(380) },
        { id: 3, username: 'student1', name: '张小明', role: 2, email: 'student1@lib.demo', phone: '13800000003', status: 1, tierId: 2, tierName: '普通学员', credit: 88, creditScore: 88, createTime: ago(360) },
        { id: 4, username: 'student2', name: '李小红', role: 2, email: 'student2@lib.demo', phone: '13800000004', status: 1, tierId: 2, tierName: '普通学员', credit: 76, creditScore: 76, createTime: ago(340) },
        { id: 5, username: 'student3', name: '王小刚', role: 2, email: 'student3@lib.demo', phone: '13800000005', status: 1, tierId: 1, tierName: '社会人员', credit: 55, creditScore: 55, createTime: ago(320) },
        { id: 6, username: 'user1', name: '赵一', role: 0, email: 'user1@lib.demo', phone: '13800000006', status: 1, tierId: 1, tierName: '社会人员', credit: 45, creditScore: 45, createTime: ago(300) },
        { id: 7, username: 'user2', name: '钱二', role: 0, email: 'user2@lib.demo', phone: '13800000007', status: 1, tierId: 1, tierName: '社会人员', credit: 70, creditScore: 70, createTime: ago(280) }
    ];

    var PASSWORDS = {
        admin: 'admin123',
        teacher1: 'teacher123',
        student1: 'student123',
        student2: 'student123',
        student3: 'student123',
        user1: 'user123',
        user2: 'user123'
    };

    function findBook(id) {
        for (var i = 0; i < BOOKS.length; i++) { if (BOOKS[i].id === id) return BOOKS[i]; }
        return null;
    }
    function findUser(id) {
        for (var i = 0; i < USERS.length; i++) { if (USERS[i].id === id) return USERS[i]; }
        return null;
    }
    function findUserByName(name) {
        for (var i = 0; i < USERS.length; i++) { if (USERS[i].username === name) return USERS[i]; }
        return null;
    }
    function findTier(id) {
        for (var i = 0; i < TIERS.length; i++) { if (TIERS[i].id === id) return TIERS[i]; }
        return null;
    }
    function nextId(arr) {
        var max = 0;
        arr.forEach(function (x) { if (x.id > max) max = x.id; });
        return max + 1;
    }
    function fmtDate(ms) {
        var d = new Date(ms);
        var p = function (n) { return String(n).padStart(2, '0'); };
        return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate());
    }
    function publicUser(u) {
        return {
            id: u.id, username: u.username, name: u.name, role: u.role,
            email: u.email, phone: u.phone, status: u.status,
            tierId: u.tierId, tierName: u.tierName,
            credit: u.credit, creditScore: u.creditScore
        };
    }

    // ---------------- 借阅记录（25 条，student1 占 14 条）----------------
    var BORROWS = [];
    function mkBorrow(id, userId, bookId, borrowAgo, termDays, returnAgo, fine, paid) {
        var u = findUser(userId);
        var b = findBook(bookId);
        var borrowDate = ago(borrowAgo);
        var dueDate = borrowDate + termDays * DAY;
        var returnDate = (returnAgo === null || returnAgo === undefined) ? null : ago(returnAgo);
        BORROWS.push({
            id: id, userId: userId, username: u ? u.username : '',
            bookId: bookId, bookTitle: b ? b.title : '', bookImage: b ? b.image : '',
            bookAuthor: b ? b.author : '', bookCategory: b ? b.category : '',
            borrowDate: borrowDate, dueDate: dueDate, returnDate: returnDate,
            status: returnDate === null ? 0 : 1,
            fine: fine || 0, paid: paid || 0
        });
    }
    // student1：4 借阅中 + 2 逾期 + 8 已归还
    mkBorrow(1, 3, 1, 12, 30, null);
    mkBorrow(2, 3, 2, 8, 30, null);
    mkBorrow(3, 3, 4, 5, 30, null);
    mkBorrow(4, 3, 17, 3, 30, null);
    mkBorrow(5, 3, 3, 45, 30, null);
    mkBorrow(6, 3, 16, 52, 30, null);
    mkBorrow(7, 3, 5, 120, 30, 95, 0, 0);
    mkBorrow(8, 3, 6, 100, 30, 80, 0, 0);
    mkBorrow(9, 3, 7, 90, 30, 66, 0, 0);
    mkBorrow(10, 3, 8, 80, 30, 55, 2.5, 1);
    mkBorrow(11, 3, 14, 70, 30, 42, 0, 0);
    mkBorrow(12, 3, 15, 60, 30, 35, 0, 0);
    mkBorrow(13, 3, 18, 150, 30, 120, 0, 0);
    mkBorrow(14, 3, 19, 140, 30, 110, 4.0, 0);
    // 其他读者
    mkBorrow(15, 2, 9, 20, 60, null);
    mkBorrow(16, 2, 10, 25, 60, null);
    mkBorrow(17, 2, 11, 60, 60, 40, 0, 0);
    mkBorrow(18, 2, 12, 90, 60, 65, 3.0, 1);
    mkBorrow(19, 4, 13, 7, 30, null);
    mkBorrow(20, 4, 1, 70, 30, 45, 0, 0);
    mkBorrow(21, 4, 3, 100, 30, 78, 0, 0);
    mkBorrow(22, 5, 5, 18, 30, null);
    mkBorrow(23, 5, 20, 55, 30, 30, 1.5, 0);
    mkBorrow(24, 6, 6, 10, 30, null);
    mkBorrow(25, 7, 8, 63, 30, 40, 2.0, 1);

    // ---------------- 预约（8 条）----------------
    var RESERVES = [
        { id: 1, userId: 3, username: 'student1', bookId: 11, bookTitle: '编译原理', tierName: '普通学员', reserveTime: ago(3), deadline: Date.now() + 5 * DAY, status: 0 },
        { id: 2, userId: 3, username: 'student1', bookId: 12, bookTitle: '人工智能：一种现代的方法', tierName: '普通学员', reserveTime: ago(5), deadline: Date.now() + 2 * DAY, status: 1 },
        { id: 3, userId: 6, username: 'user1', bookId: 11, bookTitle: '编译原理', tierName: '社会人员', reserveTime: ago(9), deadline: ago(2), status: 2 },
        { id: 4, userId: 2, username: 'teacher1', bookId: 12, bookTitle: '人工智能：一种现代的方法', tierName: '教师', reserveTime: ago(4), deadline: Date.now() + 4 * DAY, status: 0 },
        { id: 5, userId: 4, username: 'student2', bookId: 11, bookTitle: '编译原理', tierName: '普通学员', reserveTime: ago(20), deadline: ago(10), status: 4 },
        { id: 6, userId: 7, username: 'user2', bookId: 12, bookTitle: '人工智能：一种现代的方法', tierName: '社会人员', reserveTime: ago(30), deadline: ago(18), status: 3 },
        { id: 7, userId: 3, username: 'student1', bookId: 9, bookTitle: '计算机网络：自顶向下方法', tierName: '普通学员', reserveTime: ago(7), deadline: Date.now() + 3 * DAY, status: 1 },
        { id: 8, userId: 5, username: 'student3', bookId: 12, bookTitle: '人工智能：一种现代的方法', tierName: '社会人员', reserveTime: ago(1), deadline: Date.now() + 6 * DAY, status: 0 }
    ];

    // ---------------- 罚金（8 条）----------------
    var FINES = [
        { id: 1, userId: 3, username: 'student1', bookTitle: '深入理解计算机系统', amount: 4.50, overdueDays: 15, rate: 0.30, reason: '逾期归还', payStatus: 0, createTime: ago(2), payTime: null },
        { id: 2, userId: 3, username: 'student1', bookTitle: 'Go语言实战', amount: 6.60, overdueDays: 22, rate: 0.30, reason: '逾期归还', payStatus: 0, createTime: ago(1), payTime: null },
        { id: 3, userId: 3, username: 'student1', bookTitle: 'Spring实战', amount: 4.00, overdueDays: 10, rate: 0.40, reason: '逾期归还', payStatus: 0, createTime: ago(15), payTime: null },
        { id: 4, userId: 2, username: 'teacher1', bookTitle: '人工智能：一种现代的方法', amount: 3.00, overdueDays: 10, rate: 0.30, reason: '逾期归还', payStatus: 1, createTime: ago(25), payTime: ago(20) },
        { id: 5, userId: 4, username: 'student2', bookTitle: '深入理解计算机系统', amount: 2.00, overdueDays: 5, rate: 0.40, reason: '逾期归还', payStatus: 1, createTime: ago(30), payTime: ago(24) },
        { id: 6, userId: 6, username: 'user1', bookTitle: '数据结构与算法分析', amount: 2.50, overdueDays: 5, rate: 0.50, reason: '逾期归还', payStatus: 0, createTime: ago(6), payTime: null },
        { id: 7, userId: 7, username: 'user2', bookTitle: '数据结构与算法分析', amount: 2.00, overdueDays: 4, rate: 0.50, reason: '逾期归还', payStatus: 1, createTime: ago(14), payTime: ago(9) },
        { id: 8, userId: 5, username: 'student3', bookTitle: 'Vue.js实战', amount: 1.50, overdueDays: 3, rate: 0.50, reason: '逾期归还', payStatus: 0, createTime: ago(4), payTime: null }
    ];

    // ---------------- 遗失理赔（6 条）----------------
    var CLAIMS = [
        { id: 1, userId: 3, username: 'student1', bookTitle: 'Java编程思想', reportDate: ago(6), type: 2, bookPrice: 108.00, depRate: 0.80, amount: 86.40, paid: 0, status: 0, handleRemark: '', payTime: null },
        { id: 2, userId: 2, username: 'teacher1', bookTitle: '人工智能：一种现代的方法', reportDate: ago(12), type: 1, bookPrice: 128.00, depRate: 1.00, amount: 128.00, paid: 128.00, status: 2, handleRemark: '已按原价赔偿结案', payTime: ago(8) },
        { id: 3, userId: 4, username: 'student2', bookTitle: '计算机网络：自顶向下方法', reportDate: ago(20), type: 3, bookPrice: 89.00, depRate: 0.60, amount: 53.40, paid: 53.40, status: 2, handleRemark: '读者已自行购买同版本归还', payTime: ago(15) },
        { id: 4, userId: 6, username: 'user1', bookTitle: '设计模式', reportDate: ago(9), type: 1, bookPrice: 59.00, depRate: 1.00, amount: 59.00, paid: 0, status: 1, handleRemark: '处理中，等待读者确认', payTime: null },
        { id: 5, userId: 5, username: 'student3', bookTitle: 'Go语言实战', reportDate: ago(3), type: 2, bookPrice: 89.00, depRate: 0.70, amount: 62.30, paid: 0, status: 0, handleRemark: '', payTime: null },
        { id: 6, userId: 2, username: 'teacher1', bookTitle: 'Redis设计与实现', reportDate: ago(28), type: 2, bookPrice: 79.00, depRate: 0.50, amount: 39.50, paid: 0, status: 3, handleRemark: '证明材料不足，驳回', payTime: null }
    ];

    // ---------------- 荐购（8 条）----------------
    var SUGGESTIONS = [
        { id: 1, userId: 3, username: 'student1', type: 2, title: 'Rust程序设计语言', author: 'Steve Klabnik', publisher: '机械工业出版社', isbn: '9787111554659', category: '编程', price: 99.00, reason: '系统编程课程需要', status: 0, votes: 6, handleRemark: '', createTime: ago(5) },
        { id: 2, userId: 3, username: 'student1', type: 1, title: '算法导论', author: 'Thomas H.Cormen', publisher: '机械工业出版社', isbn: '9787111407010', category: '算法', price: 128.00, reason: '库存太少，经常借不到', status: 1, votes: 12, handleRemark: '已采纳，等待采购', createTime: ago(18) },
        { id: 3, userId: 2, username: 'teacher1', type: 2, title: '数据密集型应用系统设计', author: 'Martin Kleppmann', publisher: '中国电力出版社', isbn: '9787508389295', category: '计算机', price: 128.00, reason: '分布式课程参考书', status: 3, votes: 9, handleRemark: '采购中', createTime: ago(25) },
        { id: 4, userId: 4, username: 'student2', type: 1, title: '深度学习入门', author: '斋藤康毅', publisher: '人民邮电出版社', isbn: '9787115461476', category: 'AI', price: 69.00, reason: '借阅需求量大', status: 4, votes: 15, handleRemark: '已到馆上架', createTime: ago(40) },
        { id: 5, userId: 6, username: 'user1', type: 2, title: '人类简史', author: '尤瓦尔·赫拉利', publisher: '中信出版社', isbn: '9787508647357', category: '历史', price: 68.00, reason: '馆藏缺少人文类图书', status: 0, votes: 4, handleRemark: '', createTime: ago(9) },
        { id: 6, userId: 5, username: 'student3', type: 2, title: '三体', author: '刘慈欣', publisher: '重庆出版社', isbn: '9787536692930', category: '文学', price: 23.00, reason: '希望增加科幻小说', status: 2, votes: 2, handleRemark: '与馆藏建设方向不符', createTime: ago(33) },
        { id: 7, userId: 7, username: 'user2', type: 2, title: '经济学原理', author: '曼昆', publisher: '北京大学出版社', isbn: '9787301256909', category: '经济', price: 88.00, reason: '自学使用', status: 0, votes: 3, handleRemark: '', createTime: ago(2) },
        { id: 8, userId: 2, username: 'teacher1', type: 1, title: '计算机网络：自顶向下方法', author: 'James F.Kurose', publisher: '机械工业出版社', isbn: '9787111453781', category: '网络', price: 89.00, reason: '课程指定教材，需增加复本', status: 1, votes: 8, handleRemark: '已采纳', createTime: ago(60) }
    ];

    // ---------------- 公告/消息（10 条）----------------
    var NOTICES = [
        { id: 1, userId: 1, username: 'admin', type: 4, title: '系统维护通知', content: '本周日凌晨 2:00-4:00 进行系统升级，期间暂停借阅服务。', createTime: ago(1), readFlag: 0 },
        { id: 2, userId: 1, username: 'admin', type: 4, title: '月度盘点安排', content: '本月底将进行全馆盘点，请提前整理在架图书。', createTime: ago(4), readFlag: 1 },
        { id: 3, userId: 3, username: 'student1', type: 2, title: '图书逾期提醒', content: '您借阅的《深入理解计算机系统》已逾期，请尽快归还。', createTime: ago(1), readFlag: 0 },
        { id: 4, userId: 3, username: 'student1', type: 1, title: '图书到期提醒', content: '您借阅的《Python编程：从入门到实践》将于 3 天后到期。', createTime: ago(2), readFlag: 0 },
        { id: 5, userId: 3, username: 'student1', type: 3, title: '预约取书通知', content: '您预约的《人工智能：一种现代的方法》已到馆，请 5 日内取书。', createTime: ago(5), readFlag: 1 },
        { id: 6, userId: 3, username: 'student1', type: 5, title: '荐购处理结果', content: '您荐购的《算法导论》已采纳，正在采购中。', createTime: ago(17), readFlag: 1 },
        { id: 7, userId: 2, username: 'teacher1', type: 2, title: '图书逾期提醒', content: '您借阅的图书已逾期，请及时处理。', createTime: ago(3), readFlag: 0 },
        { id: 8, userId: 4, username: 'student2', type: 1, title: '图书到期提醒', content: '您借阅的《C++ Primer》将于 3 天后到期。', createTime: ago(2), readFlag: 1 },
        { id: 9, userId: 5, username: 'student3', type: 4, title: '信用分调整通知', content: '因逾期归还，您的信用分已被调整，请关注信用记录。', createTime: ago(6), readFlag: 0 },
        { id: 10, userId: 6, username: 'user1', type: 5, title: '荐购处理结果', content: '您荐购的《人类简史》正在审核中。', createTime: ago(8), readFlag: 1 }
    ];

    // ---------------- 信用记录（8 条）----------------
    var CREDITS = [
        { id: 1, userId: 3, username: 'student1', delta: -4, beforeCredit: 92, afterCredit: 88, reason: '逾期归还《深入理解计算机系统》', createTime: ago(2) },
        { id: 2, userId: 3, username: 'student1', delta: 5, beforeCredit: 83, afterCredit: 88, reason: '按时归还累计 5 次，信用加分', createTime: ago(40) },
        { id: 3, userId: 3, username: 'student1', delta: -3, beforeCredit: 86, afterCredit: 83, reason: '预约未按时取书', createTime: ago(70) },
        { id: 4, userId: 2, username: 'teacher1', delta: -8, beforeCredit: 100, afterCredit: 92, reason: '图书逾期 10 天', createTime: ago(25) },
        { id: 5, userId: 4, username: 'student2', delta: -6, beforeCredit: 82, afterCredit: 76, reason: '图书逾期 5 天', createTime: ago(30) },
        { id: 6, userId: 5, username: 'student3', delta: -5, beforeCredit: 60, afterCredit: 55, reason: '图书逾期 3 天', createTime: ago(4) },
        { id: 7, userId: 6, username: 'user1', delta: -5, beforeCredit: 50, afterCredit: 45, reason: '图书逾期 5 天', createTime: ago(6) },
        { id: 8, userId: 7, username: 'user2', delta: -5, beforeCredit: 75, afterCredit: 70, reason: '图书逾期 4 天', createTime: ago(14) }
    ];

    // ---------------- 操作日志（25 条，分页返回）----------------
    var LOG_ACTIONS = ['LOGIN_OK', 'ACCESS_DENIED', 'BOOK_CREATE', 'BORROW', 'RETURN', 'FINE_PAY', 'RESERVE', 'USER_UPDATE', 'GRANT', 'LOGIN_DENIED'];
    var LOG_USERS = ['admin', 'student1', 'teacher1', 'student2', 'user1', 'user2'];
    var LOGS = [];
    (function () {
        for (var i = 0; i < 25; i++) {
            var action = LOG_ACTIONS[i % LOG_ACTIONS.length];
            var okFlag = (i % 9 === 4) ? 0 : 1;
            LOGS.push({
                id: 25 - i,
                username: LOG_USERS[i % LOG_USERS.length],
                action: action,
                target: action.indexOf('BOOK') === 0 ? 'book' : (action.indexOf('LOGIN') === 0 ? 'index.html' : 'system'),
                targetId: action.indexOf('BOOK') === 0 ? (i + 1) : null,
                detail: '演示环境操作记录 #' + (25 - i),
                ip: '192.168.1.' + (10 + i),
                ok: okFlag,
                level: okFlag ? 'INFO' : 'WARN',
                createTime: ago(i * 0.5)
            });
        }
    })();

    // ---------------- 盘点任务（6 条）----------------
    var INVENTORIES = [
        { id: 1, name: '2026 年秋季全馆盘点', type: 1, area: '全馆', total: 231, actual: 228, missing: 3, extra: 0, status: 2, startTime: ago(30), endTime: ago(26), creatorName: '系统管理员' },
        { id: 2, name: 'A 区编程类书架盘点', type: 2, area: 'A区', total: 86, actual: 86, missing: 0, extra: 2, status: 2, startTime: ago(15), endTime: ago(13), creatorName: '系统管理员' },
        { id: 3, name: 'B 区 AI 类抽样盘点', type: 3, area: 'B区', total: 20, actual: 19, missing: 1, extra: 0, status: 2, startTime: ago(8), endTime: ago(7), creatorName: '系统管理员' },
        { id: 4, name: 'D 区数据库书架盘点', type: 2, area: 'D区', total: 42, actual: 17, missing: 0, extra: 0, status: 1, startTime: ago(2), endTime: null, creatorName: '系统管理员' },
        { id: 5, name: '新到图书入藏盘点', type: 3, area: '新书区', total: 30, actual: 0, missing: 0, extra: 0, status: 0, startTime: null, endTime: null, creatorName: '系统管理员' },
        { id: 6, name: '临时抽查（取消）', type: 3, area: 'C区', total: 12, actual: 0, missing: 0, extra: 0, status: 3, startTime: null, endTime: null, creatorName: '系统管理员' }
    ];

    var INVENTORY_ITEMS = {};
    (function () {
        INVENTORIES.forEach(function (inv) {
            var items = [];
            for (var i = 0; i < 8; i++) {
                var b = BOOKS[i];
                var real = 0;
                if (inv.status === 2) real = (i === 2 || i === 5) ? 2 : 1;
                items.push({
                    invId: inv.id,
                    bookId: b.id,
                    isbn: b.isbn,
                    title: b.title,
                    sysStatus: b.stock > 0 ? 1 : 2,
                    realStatus: inv.status === 2 ? real : 0,
                    remark: inv.status === 2 && (i === 2 || i === 5) ? '架上未找到' : '',
                    checkedTime: inv.status === 2 ? inv.endTime : null
                });
            }
            INVENTORY_ITEMS[inv.id] = items;
        });
    })();

    // ---------------- 管理员权限（8 项定义 + 管理员账号）----------------
    var PERM_DEFS = [
        { code: 'book', name: '图书管理', pos: 0, grp: '资源管理', note: '图书增删改查' },
        { code: 'inventory', name: '库存盘点', pos: 1, grp: '资源管理', note: '创建与执行盘点任务' },
        { code: 'user', name: '用户管理', pos: 2, grp: '读者服务', note: '用户增删改查与启停' },
        { code: 'borrow', name: '借阅管理', pos: 3, grp: '读者服务', note: '借阅登记与归还' },
        { code: 'fine', name: '罚金管理', pos: 4, grp: '读者服务', note: '罚金减免' },
        { code: 'claim', name: '理赔处理', pos: 5, grp: '读者服务', note: '遗失理赔结案' },
        { code: 'perm', name: '权限管理', pos: 6, grp: '系统管理', note: '管理员授权' },
        { code: 'log', name: '日志审计', pos: 7, grp: '系统管理', note: '操作日志查看' }
    ];
    var ADMIN_ACCOUNTS = [
        { id: 1, username: 'admin', name: '系统管理员', perms: '255' }
    ];

    // ---------------- 会话与工具 ----------------
    function currentUser() {
        try {
            var raw = localStorage.getItem('user');
            if (!raw) return null;
            return JSON.parse(raw);
        } catch (e) { return null; }
    }
    function roleOf(u) { return u ? parseInt(u.role, 10) : -1; }
    function isAdminUser() { return roleOf(currentUser()) === 1; }
    function meRecord() {
        var u = currentUser();
        if (!u) return null;
        return findUser(parseInt(u.id, 10)) || findUserByName(u.username) || u;
    }

    function parseBody(body) {
        if (!body) return {};
        if (typeof body === 'string') {
            var s = body.trim();
            if (!s) return {};
            if (s.charAt(0) === '{') {
                try { return JSON.parse(s) || {}; } catch (e) { return {}; }
            }
            var obj = {};
            new URLSearchParams(s).forEach(function (v, k) { obj[k] = v; });
            return obj;
        }
        if (typeof FormData !== 'undefined' && body instanceof FormData) {
            var fd = {};
            body.forEach(function (v, k) { fd[k] = v; });
            return fd;
        }
        if (typeof URLSearchParams !== 'undefined' && body instanceof URLSearchParams) {
            var usp = {};
            body.forEach(function (v, k) { usp[k] = v; });
            return usp;
        }
        if (typeof body === 'object') return body;
        return {};
    }

    // ============================================================
    // 各端点实现
    // ============================================================

    function doLogin(m, q, body) {
        var username = body.username || q.username || '';
        var password = body.password || q.password || '';
        var user = findUserByName(username);
        if (!user || PASSWORDS[username] !== password) {
            return json({ success: false, message: '演示环境请使用演示账号：admin/admin123、teacher1/teacher123、student1/student123、user1/user123' });
        }
        if (user.status === 0) {
            return json({ success: false, message: '该账号已被禁用（演示数据）' });
        }
        var token = 'demo-' + Math.random().toString(36).slice(2, 10) + Math.random().toString(36).slice(2, 10);
        return json({ success: true, message: '登录成功（演示数据）', token: token, user: publicUser(user) });
    }

    function doLogout() { return json({ success: true, message: '已退出登录（演示数据）' }); }

    function doRegister(m, q, body) {
        var username = body.username || '';
        if (!username) return bad('用户名不能为空');
        if (findUserByName(username)) return json({ success: false, message: '用户名已存在（演示环境）' });
        var role = parseInt(body.role != null ? body.role : 0, 10);
        if (isNaN(role)) role = 0;
        var user = {
            id: nextId(USERS),
            username: username,
            name: body.name || username,
            role: role,
            email: body.email || '',
            phone: body.phone || '',
            status: 1,
            tierId: role === 3 ? 3 : (role === 2 ? 2 : 1),
            tierName: role === 3 ? '教师' : (role === 2 ? '普通学员' : '社会人员'),
            credit: 80,
            creditScore: 80,
            createTime: Date.now()
        };
        USERS.push(user);
        PASSWORDS[username] = body.password || 'demo123';
        return json({ success: true, message: '注册成功（演示数据）', user: publicUser(user) });
    }

    function doBookList(m, q) {
        var kw = (q.keyword || '').trim().toLowerCase();
        if (!kw) return json(BOOKS);
        var list = BOOKS.filter(function (b) {
            var text = ((b.title || '') + ' ' + (b.author || '') + ' ' + (b.isbn || '') + ' ' + (b.category || '')).toLowerCase();
            return text.indexOf(kw) !== -1;
        });
        return json(list);
    }

    function doBookDetail(m) {
        var b = findBook(parseInt(m[1], 10));
        return b ? json(b) : json({}, 404);
    }

    function doBookCreate(m, q, body) {
        if (!isAdminUser()) return denied();
        var book = {
            id: nextId(BOOKS),
            isbn: body.isbn || '',
            title: body.title || '未命名图书',
            author: body.author || '',
            publisher: body.publisher || '',
            publishDate: body.publishDate || '',
            category: body.category || '编程',
            price: parseFloat(body.price) || 0,
            stock: parseInt(body.stock, 10) || 0,
            total: parseInt(body.stock, 10) || 0,
            location: body.location || '待上架',
            image: body.image || '',
            description: body.description || '',
            intro: body.description || ''
        };
        BOOKS.push(book);
        return json({ success: true, message: '添加成功（演示数据）', book: book });
    }

    function doBookUpdate(m, q, body) {
        if (!isAdminUser()) return denied();
        var id = parseInt(body.id, 10);
        var b = findBook(id);
        if (!b) return bad('图书不存在（演示数据）');
        ['isbn', 'title', 'author', 'publisher', 'publishDate', 'category', 'location', 'image', 'description'].forEach(function (k) {
            if (body[k] !== undefined) b[k] = body[k];
        });
        if (body.price !== undefined) b.price = parseFloat(body.price) || 0;
        if (body.stock !== undefined) b.stock = parseInt(body.stock, 10) || 0;
        return json({ success: true, message: '更新成功（演示数据）' });
    }

    function doBookDelete(m) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        for (var i = 0; i < BOOKS.length; i++) {
            if (BOOKS[i].id === id) { BOOKS.splice(i, 1); break; }
        }
        return json({ success: true, message: '删除成功（演示数据）' });
    }

    function doBorrowList(m, q) {
        var u = currentUser();
        if (!u) return denied();
        var list = BORROWS.slice();
        if (!isAdminUser()) {
            list = list.filter(function (b) { return b.username === u.username; });
        }
        if (q.status !== undefined && q.status !== '') {
            var s = parseInt(q.status, 10);
            list = list.filter(function (b) { return b.status === s; });
        }
        list.sort(function (a, b) { return b.borrowDate - a.borrowDate; });
        return json(list);
    }

    function doBorrowCreate(m, q, body) {
        var u = meRecord();
        if (!u) return denied();
        var bookId = parseInt(body.bookId || q.bookId, 10);
        var book = findBook(bookId);
        if (!book) return bad('图书不存在（演示数据）');
        if (book.stock <= 0) return json({ success: false, message: '该图书已无可借库存' });

        var target = u;
        if (isAdminUser() && body.userId) {
            target = findUser(parseInt(body.userId, 10)) || u;
        }
        var tier = findTier(target.tierId) || TIERS[0];
        book.stock = Math.max(0, book.stock - 1);
        var row = {
            id: nextId(BORROWS),
            userId: target.id,
            username: target.username,
            bookId: book.id,
            bookTitle: book.title,
            bookImage: book.image,
            bookAuthor: book.author,
            bookCategory: book.category,
            borrowDate: Date.now(),
            dueDate: Date.now() + (tier.days || 30) * DAY,
            returnDate: null,
            status: 0,
            fine: 0,
            paid: 0
        };
        BORROWS.push(row);
        return json({ success: true, message: '借阅登记成功（演示数据）', borrow: row });
    }

    function doBorrowReturn(m) {
        var u = currentUser();
        if (!u) return denied();
        var id = parseInt(m[1], 10);
        var row = null;
        BORROWS.forEach(function (b) { if (b.id === id) row = b; });
        if (!row) return bad('借阅记录不存在（演示数据）');
        if (!isAdminUser() && row.username !== u.username) return denied();
        if (row.status === 1) return json({ success: false, message: '该图书已归还' });
        row.status = 1;
        row.returnDate = Date.now();
        var book = findBook(row.bookId);
        if (book) book.stock = (book.stock || 0) + 1;
        return json({ success: true, message: '归还成功（演示数据）' });
    }

    function doBorrowRenew(m) {
        var u = currentUser();
        if (!u) return denied();
        var id = parseInt(m[1], 10);
        var row = null;
        BORROWS.forEach(function (b) { if (b.id === id) row = b; });
        if (!row) return bad('借阅记录不存在（演示数据）');
        if (!isAdminUser() && row.username !== u.username) return denied();
        if (row.status === 1) return json({ success: false, message: '已归还的图书不能续借' });
        var user = findUser(row.userId);
        var tier = user ? findTier(user.tierId) : null;
        row.dueDate = (row.dueDate || Date.now()) + ((tier && tier.renewDays) || 15) * DAY;
        return json({ success: true, message: '续借成功（演示数据）' });
    }

    function doReserveList(m, q) {
        var u = currentUser();
        if (!u) return denied();
        var list = RESERVES.slice();
        if (!isAdminUser()) {
            list = list.filter(function (r) { return r.username === u.username; });
        }
        list.sort(function (a, b) { return b.reserveTime - a.reserveTime; });
        return json(list);
    }

    function doReserveCreate(m, q, body) {
        var u = meRecord();
        if (!u) return denied();
        var bookId = parseInt(body.bookId || q.bookId, 10);
        var book = findBook(bookId);
        if (!book) return bad('图书不存在（演示数据）');
        if (book.stock > 0) return json({ success: false, message: '该图书有库存，无需预约' });
        var tier = findTier(u.tierId);
        var row = {
            id: nextId(RESERVES),
            userId: u.id,
            username: u.username,
            bookId: book.id,
            bookTitle: book.title,
            tierName: tier ? tier.name : '普通学员',
            reserveTime: Date.now(),
            deadline: Date.now() + 7 * DAY,
            status: 0
        };
        RESERVES.push(row);
        return json({ success: true, message: '预约成功（演示数据）' });
    }

    function doReserveCancel(m) {
        var u = currentUser();
        if (!u) return denied();
        var id = parseInt(m[1], 10);
        var row = null;
        RESERVES.forEach(function (r) { if (r.id === id) row = r; });
        if (!row) return bad('预约记录不存在（演示数据）');
        if (!isAdminUser() && row.username !== u.username) return denied();
        row.status = 2;
        return json({ success: true, message: '预约已取消（演示数据）' });
    }

    function doReservePick(m) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        var row = null;
        RESERVES.forEach(function (r) { if (r.id === id) row = r; });
        if (!row) return bad('预约记录不存在（演示数据）');
        row.status = 4;
        return json({ success: true, message: '已确认取书（演示数据）' });
    }

    function doFineList(m, q) {
        var u = currentUser();
        if (!u) return denied();
        var list = FINES.slice();
        if (!isAdminUser()) {
            list = list.filter(function (f) { return f.username === u.username; });
        } else if (q.status !== undefined && q.status !== '') {
            var s = parseInt(q.status, 10);
            list = list.filter(function (f) { return f.payStatus === s; });
        }
        list.sort(function (a, b) { return b.createTime - a.createTime; });
        return json(list);
    }

    function doFinePay(m) {
        var u = currentUser();
        if (!u) return denied();
        var id = parseInt(m[1], 10);
        var f = null;
        FINES.forEach(function (x) { if (x.id === id) f = x; });
        if (!f) return bad('罚金记录不存在（演示数据）');
        if (!isAdminUser() && f.username !== u.username) return denied();
        if (f.payStatus !== 0) return json({ success: false, message: '该罚金已处理' });
        f.payStatus = 1;
        f.payTime = Date.now();
        return json({ success: true, message: '缴纳成功（演示数据）' });
    }

    function doFineWaive(m, q, body) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        var f = null;
        FINES.forEach(function (x) { if (x.id === id) f = x; });
        if (!f) return bad('罚金记录不存在（演示数据）');
        if (!body.reason) return bad('减免原因不能为空');
        f.payStatus = 2;
        f.payTime = Date.now();
        f.reason = f.reason + '（减免：' + body.reason + '）';
        return json({ success: true, message: '已减免（演示数据）' });
    }

    function doClaimList(m, q) {
        var u = currentUser();
        if (!u) return denied();
        var list = CLAIMS.slice();
        if (!isAdminUser()) {
            list = list.filter(function (c) { return c.username === u.username; });
        }
        return json(list);
    }

    function doClaimReport(m, q, body) {
        var u = meRecord();
        if (!u) return denied();
        var borrowId = parseInt(body.borrowId, 10);
        var row = null;
        BORROWS.forEach(function (b) { if (b.id === borrowId) row = b; });
        if (!row) return bad('借阅记录不存在（演示数据）');
        if (row.username !== u.username || row.status !== 0) return bad('只能报失本人借阅中的图书');
        var type = parseInt(body.type, 10) || 1;
        var book = findBook(row.bookId);
        var price = book ? book.price : 0;
        var rate = type === 1 ? 1.0 : (type === 2 ? 0.8 : 0.6);
        var claim = {
            id: nextId(CLAIMS),
            userId: u.id,
            username: u.username,
            bookTitle: row.bookTitle,
            reportDate: Date.now(),
            type: type,
            bookPrice: price,
            depRate: rate,
            amount: Math.round(price * rate * 100) / 100,
            paid: 0,
            status: 0,
            handleRemark: '',
            payTime: null
        };
        CLAIMS.push(claim);
        return json({ success: true, message: '报失成功（演示数据）' });
    }

    function doClaimHandle(m, q, body) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        var c = null;
        CLAIMS.forEach(function (x) { if (x.id === id) c = x; });
        if (!c) return bad('理赔记录不存在（演示数据）');
        if (!body.remark) return bad('处理备注必填');
        if (body.action === 'approve') {
            c.status = 2;
            c.paid = parseFloat(body.paid) || c.amount;
            c.payTime = Date.now();
        } else {
            c.status = 3;
        }
        c.handleRemark = body.remark;
        return json({ success: true, message: '处理成功（演示数据）' });
    }

    function doDashboard(m, q) {
        if (!isAdminUser()) return denied();
        var borrowing = BORROWS.filter(function (b) { return b.status === 0; }).length;
        var overdue = BORROWS.filter(function (b) { return b.status === 0 && b.dueDate < Date.now(); }).length;
        var reserveActive = RESERVES.filter(function (r) { return r.status === 0 || r.status === 1; }).length;
        var unpaid = FINES.filter(function (f) { return f.payStatus === 0; });
        var unpaidAmount = unpaid.reduce(function (s, f) { return s + (parseFloat(f.amount) || 0); }, 0);
        var paidList = FINES.filter(function (f) { return f.payStatus === 1; });
        var collected = paidList.reduce(function (s, f) { return s + (parseFloat(f.amount) || 0); }, 0);

        var roleCount = { 1: 0, 3: 0, 2: 0, 0: 0 };
        USERS.forEach(function (u) { if (roleCount[u.role] !== undefined) roleCount[u.role]++; });
        var totalUsers = USERS.length || 1;
        var roles = [
            { name: '管理员', c: roleCount[1] },
            { name: '教师', c: roleCount[3] },
            { name: '线上学员', c: roleCount[2] },
            { name: '社会人员', c: roleCount[0] }
        ];
        roles.forEach(function (r) { r.pct = Math.round(r.c * 100 / totalUsers); });

        var categories = [];
        var catMap = {};
        BOOKS.forEach(function (b) {
            var c = b.category || '未分类';
            catMap[c] = (catMap[c] || 0) + 1;
        });
        Object.keys(catMap).forEach(function (k) { categories.push({ category: k, c: catMap[k] }); });

        var bookMap = {};
        BORROWS.forEach(function (b) {
            bookMap[b.bookTitle] = (bookMap[b.bookTitle] || 0) + 1;
        });
        var topBooks = Object.keys(bookMap).map(function (k) { return { title: k, c: bookMap[k] }; })
            .sort(function (a, b) { return b.c - a.c; }).slice(0, 5);

        var trend = [];
        var todayStart = new Date();
        todayStart.setHours(0, 0, 0, 0);
        for (var i = 6; i >= 0; i--) {
            var start = todayStart.getTime() - i * DAY;
            var c = BORROWS.filter(function (b) { return b.borrowDate >= start && b.borrowDate < start + DAY; }).length;
            trend.push({ d: fmtDate(start), c: c });
        }

        var recent = BORROWS.slice().sort(function (a, b) { return b.borrowDate - a.borrowDate; }).slice(0, 5)
            .map(function (b) {
                return { username: b.username, bookTitle: b.bookTitle, borrowDate: b.borrowDate, status: b.status };
            });

        return json({
            summary: {
                bookCount: BOOKS.length,
                userCount: USERS.length,
                borrowing: borrowing,
                overdue: overdue,
                reserveActive: reserveActive,
                unpaidFine: unpaid.length
            },
            fines: {
                unpaidAmount: unpaidAmount.toFixed(2),
                unpaidCount: unpaid.length,
                collected: collected.toFixed(2)
            },
            borrowTotal: BORROWS.length,
            recent: recent,
            trend: trend,
            categories: categories,
            topBooks: topBooks,
            roles: roles
        });
    }

    function doNoticeList(m, q) {
        var u = currentUser();
        if (!u) return denied();
        var list = NOTICES.filter(function (n) { return n.userId === parseInt(u.id, 10); });
        if (q.unread === '1') {
            list = list.filter(function (n) { return n.readFlag === 0; });
        }
        list.sort(function (a, b) { return b.createTime - a.createTime; });
        return json(list);
    }

    function doNoticeRead(m) {
        var u = currentUser();
        if (!u) return denied();
        var id = parseInt(m[1], 10);
        NOTICES.forEach(function (n) {
            if (n.id === id && n.userId === parseInt(u.id, 10)) n.readFlag = 1;
        });
        return json({ success: true, message: '已标记为已读（演示数据）' });
    }

    function doNoticeReadAll() {
        var u = currentUser();
        if (!u) return denied();
        NOTICES.forEach(function (n) {
            if (n.userId === parseInt(u.id, 10)) n.readFlag = 1;
        });
        return json({ success: true, message: '已全部标记为已读（演示数据）' });
    }

    function doCreditList(m, q) {
        var u = currentUser();
        if (!u) return denied();
        var list = CREDITS.slice();
        if (!isAdminUser()) {
            list = list.filter(function (c) { return c.username === u.username; });
        }
        list.sort(function (a, b) { return b.createTime - a.createTime; });
        return json(list);
    }

    function doCreditAdjust(m, q, body) {
        if (!isAdminUser()) return denied();
        var userId = parseInt(body.userId, 10);
        var delta = parseInt(body.delta, 10);
        var reason = body.reason || '管理员调整';
        var target = findUser(userId);
        if (!target) return bad('用户不存在（演示数据）');
        if (isNaN(delta) || delta === 0) return bad('调整分值无效');
        var before = target.credit != null ? target.credit : 80;
        var after = Math.max(0, Math.min(100, before + delta));
        target.credit = after;
        target.creditScore = after;
        CREDITS.unshift({
            id: nextId(CREDITS),
            userId: target.id,
            username: target.username,
            delta: after - before,
            beforeCredit: before,
            afterCredit: after,
            reason: reason,
            createTime: Date.now()
        });
        return json({ success: true, message: '调整成功（演示数据）', credit: after });
    }

    function doSuggestionList() { return json(SUGGESTIONS.slice()); }

    function doSuggestionSubmit(m, q, body) {
        var u = meRecord();
        if (!u) return denied();
        if (!body.title) return bad('书名必填');
        var row = {
            id: nextId(SUGGESTIONS),
            userId: u.id,
            username: u.username,
            type: parseInt(body.type, 10) || 1,
            title: body.title,
            author: body.author || '',
            publisher: body.publisher || '',
            isbn: body.isbn || '',
            category: body.category || '',
            price: parseFloat(body.price) || 0,
            reason: body.reason || '',
            status: 0,
            votes: 0,
            handleRemark: '',
            createTime: Date.now()
        };
        SUGGESTIONS.push(row);
        return json({ success: true, message: '提交成功（演示数据）' });
    }

    function doSuggestionVote(m) {
        var u = currentUser();
        if (!u) return denied();
        var id = parseInt(m[1], 10);
        var s = null;
        SUGGESTIONS.forEach(function (x) { if (x.id === id) s = x; });
        if (!s) return bad('荐购记录不存在（演示数据）');
        s.votes = (s.votes || 0) + 1;
        return json({ success: true, message: '点赞成功（演示数据）' });
    }

    function doSuggestionHandle(m, q, body) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        var s = null;
        SUGGESTIONS.forEach(function (x) { if (x.id === id) s = x; });
        if (!s) return bad('荐购记录不存在（演示数据）');
        var action = body.action || '';
        if (action === 'adopt') s.status = 1;
        else if (action === 'reject') s.status = 2;
        else if (action === 'purchase') s.status = 3;
        else if (action === 'arrive') s.status = 4;
        else return bad('未知的处理动作');
        if (body.remark) s.handleRemark = body.remark;
        return json({ success: true, message: '处理成功（演示数据）' });
    }

    function doInventoryList() {
        if (!isAdminUser()) return denied();
        return json(INVENTORIES.slice());
    }

    function doInventoryCreate(m, q, body) {
        if (!isAdminUser()) return denied();
        var name = body.name || '未命名盘点';
        var type = parseInt(body.type, 10) || 1;
        var inv = {
            id: nextId(INVENTORIES),
            name: name,
            type: type,
            area: body.area || (type === 1 ? '全馆' : '未指定区域'),
            total: BOOKS.length,
            actual: 0,
            missing: 0,
            extra: 0,
            status: 0,
            startTime: null,
            endTime: null,
            creatorName: currentUser() ? (currentUser().name || currentUser().username) : '管理员'
        };
        INVENTORIES.push(inv);
        var items = [];
        BOOKS.forEach(function (b) {
            items.push({
                invId: inv.id,
                bookId: b.id,
                isbn: b.isbn,
                title: b.title,
                sysStatus: b.stock > 0 ? 1 : 2,
                realStatus: 0,
                remark: '',
                checkedTime: null
            });
        });
        INVENTORY_ITEMS[inv.id] = items;
        return json({ success: true, message: '创建成功（演示数据）' });
    }

    function doInventoryItems(m) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        return json(INVENTORY_ITEMS[id] || []);
    }

    function doInventoryCheck(m, q, body) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        var items = INVENTORY_ITEMS[id] || [];
        var bookId = parseInt(body.bookId, 10);
        var item = null;
        items.forEach(function (x) { if (x.bookId === bookId) item = x; });
        if (!item) return bad('盘点明细不存在（演示数据）');
        item.realStatus = parseInt(body.realStatus, 10) || 0;
        item.remark = body.remark || '';
        item.checkedTime = Date.now();
        var inv = null;
        INVENTORIES.forEach(function (x) { if (x.id === id) inv = x; });
        if (inv) {
            inv.actual = items.filter(function (x) { return x.realStatus > 0; }).length;
            inv.missing = items.filter(function (x) { return x.realStatus === 2; }).length;
            inv.extra = 0;
        }
        return json({ success: true, message: '已保存（演示数据）' });
    }

    function doInventoryStart(m) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        var inv = null;
        INVENTORIES.forEach(function (x) { if (x.id === id) inv = x; });
        if (!inv) return bad('盘点任务不存在（演示数据）');
        inv.status = 1;
        inv.startTime = Date.now();
        return json({ success: true, message: '盘点已开始（演示数据）' });
    }

    function doInventoryFinish(m) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        var inv = null;
        INVENTORIES.forEach(function (x) { if (x.id === id) inv = x; });
        if (!inv) return bad('盘点任务不存在（演示数据）');
        var items = INVENTORY_ITEMS[id] || [];
        inv.actual = items.filter(function (x) { return x.realStatus > 0; }).length;
        inv.missing = items.filter(function (x) { return x.realStatus === 2; }).length;
        inv.extra = 0;
        inv.status = 2;
        inv.endTime = Date.now();
        return json({ success: true, message: '已完成结算（演示数据）' });
    }

    function doInventoryCancel(m) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        var inv = null;
        INVENTORIES.forEach(function (x) { if (x.id === id) inv = x; });
        if (!inv) return bad('盘点任务不存在（演示数据）');
        inv.status = 3;
        inv.endTime = Date.now();
        return json({ success: true, message: '盘点已取消（演示数据）' });
    }

    function doProfile() {
        var u = meRecord();
        if (!u) return denied();
        return json(publicUser(u));
    }

    function doPassword(m, q, body) {
        var u = meRecord();
        if (!u) return denied();
        var oldPwd = body.oldPassword || '';
        var newPwd = body.newPassword || '';
        if (!newPwd) return bad('新密码不能为空');
        if (PASSWORDS[u.username] && oldPwd !== PASSWORDS[u.username]) {
            return json({ success: false, message: '原密码不正确（演示数据）' });
        }
        PASSWORDS[u.username] = newPwd;
        return json({ success: true, message: '密码已修改（演示数据）' });
    }

    function doUserList() {
        if (!isAdminUser()) return denied();
        return json(USERS.map(function (u) {
            return {
                id: u.id, username: u.username, name: u.name, role: u.role,
                email: u.email, phone: u.phone, status: u.status,
                tierId: u.tierId, tierName: u.tierName,
                credit: u.credit, creditScore: u.creditScore
            };
        }));
    }

    function doUserCreate(m, q, body) {
        if (!isAdminUser()) return denied();
        var username = body.username || '';
        if (!username) return bad('用户名不能为空');
        if (findUserByName(username)) return json({ success: false, message: '用户名已存在（演示数据）' });
        var role = parseInt(body.role, 10);
        if (isNaN(role)) role = 0;
        var tier = role === 3 ? 3 : (role === 2 ? 2 : 1);
        var t = findTier(tier);
        var user = {
            id: nextId(USERS),
            username: username,
            name: body.name || username,
            role: role,
            email: body.email || '',
            phone: body.phone || '',
            status: 1,
            tierId: tier,
            tierName: t ? t.name : '',
            credit: 80,
            creditScore: 80,
            createTime: Date.now()
        };
        USERS.push(user);
        PASSWORDS[username] = body.password || 'demo123';
        return json({ success: true, message: '添加成功（演示数据）' });
    }

    function doUserUpdate(m, q, body) {
        var u = currentUser();
        if (!u) return denied();
        var id = parseInt(m[1], 10);
        if (!isAdminUser() && parseInt(u.id, 10) !== id) return denied();
        var target = findUser(id);
        if (!target) return bad('用户不存在（演示数据）');
        var src = {};
        ['name', 'email', 'phone', 'role', 'status', 'tierId'].forEach(function (k) {
            if (q[k] !== undefined) src[k] = q[k];
            if (body[k] !== undefined) src[k] = body[k];
        });
        if (src.name !== undefined) target.name = src.name;
        if (src.email !== undefined) target.email = src.email;
        if (src.phone !== undefined) target.phone = src.phone;
        // 角色 / 状态只有管理员能改，普通读者只能改自己的资料
        if (isAdminUser()) {
            if (src.role !== undefined) target.role = parseInt(src.role, 10) || 0;
            if (src.status !== undefined) target.status = parseInt(src.status, 10);
        }
        if (body.password) PASSWORDS[target.username] = body.password;
        if (target.id === 1 && target.role !== 1) target.role = 1;
        return json({ success: true, message: '更新成功（演示数据）' });
    }

    function doUserStatus(m, q) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        var target = findUser(id);
        if (!target) return bad('用户不存在（演示数据）');
        var status = parseInt(q.status, 10);
        target.status = isNaN(status) ? 1 : status;
        return json({ success: true, message: '操作成功（演示数据）' });
    }

    function doUserDelete(m) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        for (var i = 0; i < USERS.length; i++) {
            if (USERS[i].id === id) { USERS.splice(i, 1); break; }
        }
        return json({ success: true, message: '删除成功（演示数据）' });
    }

    function doTierList() { return json(TIERS.slice()); }

    function doTierCreate(m, q, body) {
        if (!isAdminUser()) return denied();
        if (!body.name) return bad('等级名称必填');
        var t = {
            id: nextId(TIERS),
            name: body.name,
            minScore: parseInt(body.minScore, 10) || 0,
            maxBorrow: parseInt(body.maxBorrow, 10) || 3,
            days: parseInt(body.days, 10) || 30,
            renewMax: parseInt(body.renewMax, 10) || 1,
            renewDays: parseInt(body.renewDays, 10) || 15,
            priority: parseInt(body.priority, 10) || 1,
            rate: parseFloat(body.rate) || 0,
            note: body.note || ''
        };
        TIERS.push(t);
        return json({ success: true, message: '保存成功（演示数据）' });
    }

    function doTierUpdate(m, q, body) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        var t = findTier(id);
        if (!t) return bad('等级不存在（演示数据）');
        var src = {};
        ['name', 'minScore', 'maxBorrow', 'days', 'renewMax', 'renewDays', 'priority', 'rate', 'note'].forEach(function (k) {
            if (q[k] !== undefined) src[k] = q[k];
            if (body[k] !== undefined) src[k] = body[k];
        });
        if (src.name !== undefined) t.name = src.name;
        if (src.minScore !== undefined) t.minScore = parseInt(src.minScore, 10) || 0;
        if (src.maxBorrow !== undefined) t.maxBorrow = parseInt(src.maxBorrow, 10) || 0;
        if (src.days !== undefined) t.days = parseInt(src.days, 10) || 0;
        if (src.renewMax !== undefined) t.renewMax = parseInt(src.renewMax, 10) || 0;
        if (src.renewDays !== undefined) t.renewDays = parseInt(src.renewDays, 10) || 0;
        if (src.priority !== undefined) t.priority = parseInt(src.priority, 10) || 0;
        if (src.rate !== undefined) t.rate = parseFloat(src.rate) || 0;
        if (src.note !== undefined) t.note = src.note;
        return json({ success: true, message: '保存成功（演示数据）' });
    }

    function doTierDelete(m) {
        if (!isAdminUser()) return denied();
        var id = parseInt(m[1], 10);
        for (var i = 0; i < TIERS.length; i++) {
            if (TIERS[i].id === id) { TIERS.splice(i, 1); break; }
        }
        return json({ success: true, message: '删除成功（演示数据）' });
    }

    function doAdminPerms() {
        if (!isAdminUser()) return denied();
        return json(PERM_DEFS.slice());
    }

    function doAdminAdmins() {
        if (!isAdminUser()) return denied();
        return json(ADMIN_ACCOUNTS.slice());
    }

    function doAdminGrant(m, q, body) {
        if (!isAdminUser()) return denied();
        var userId = parseInt(body.userId, 10);
        var target = findUser(userId);
        if (!target) return bad('用户不存在（演示数据）');
        target.role = 1;
        var perms = body.perms != null ? String(body.perms) : '0';
        var found = null;
        ADMIN_ACCOUNTS.forEach(function (a) { if (a.id === userId) found = a; });
        if (found) {
            found.perms = perms;
        } else {
            ADMIN_ACCOUNTS.push({ id: target.id, username: target.username, name: target.name, perms: perms });
        }
        return json({ success: true, message: '保存成功（演示数据）' });
    }

    function doAdminRevoke(m, q, body) {
        if (!isAdminUser()) return denied();
        var userId = parseInt(body.userId, 10);
        if (userId === 1) return bad('不能收回主管理员权限');
        for (var i = 0; i < ADMIN_ACCOUNTS.length; i++) {
            if (ADMIN_ACCOUNTS[i].id === userId) { ADMIN_ACCOUNTS.splice(i, 1); break; }
        }
        var target = findUser(userId);
        if (target) target.role = 0;
        return json({ success: true, message: '已收回权限（演示数据）' });
    }

    function doLogList(m, q) {
        if (!isAdminUser()) return denied();
        var action = q.action || '';
        var page = parseInt(q.page, 10) || 1;
        var size = parseInt(q.size, 10) || 20;
        var list = LOGS.slice();
        if (action) list = list.filter(function (l) { return l.action === action; });
        var total = list.length;
        var pages = Math.max(1, Math.ceil(total / size));
        var start = (page - 1) * size;
        return json({ list: list.slice(start, start + size), total: total, pages: pages, page: page });
    }

    function doLogWrite(m, q, body) {
        LOGS.unshift({
            id: nextId(LOGS),
            username: (currentUser() && currentUser().username) || (body.username || 'anonymous'),
            action: body.action || 'UNKNOWN',
            target: body.target || '-',
            targetId: body.targetId || null,
            detail: body.detail || '演示环境操作记录',
            ip: '127.0.0.1',
            ok: 1,
            level: body.level || 'INFO',
            createTime: Date.now()
        });
        return json({ success: true, message: '日志已记录（演示数据）' });
    }

    function doUpload() {
        return json({ success: true, message: '上传成功（演示数据）', url: '/upload/book19.jpg' });
    }

    // ============================================================
    // 路由表（顺序敏感：具体路径在前）
    // ============================================================
    function rule(method, re, fn) { return { method: method, re: re, fn: fn }; }

    var RULES = [
        rule('POST', /^\/api\/login\/?$/, doLogin),
        rule('POST', /^\/api\/logout\/?$/, doLogout),
        rule('POST', /^\/api\/register\/?$/, doRegister),

        rule('GET', /^\/api\/book\/?$/, doBookList),
        rule('POST', /^\/api\/book\/?$/, doBookCreate),
        rule('PUT', /^\/api\/book\/?$/, doBookUpdate),
        rule('GET', /^\/api\/book\/(\d+)\/?$/, doBookDetail),
        rule('DELETE', /^\/api\/book\/(\d+)\/?$/, doBookDelete),

        rule('GET', /^\/api\/borrow\/?$/, doBorrowList),
        rule('POST', /^\/api\/borrow\/?$/, doBorrowCreate),
        rule('PUT', /^\/api\/borrow\/(\d+)\/renew\/?$/, doBorrowRenew),
        rule('PUT', /^\/api\/borrow\/(\d+)\/?$/, doBorrowReturn),

        rule('GET', /^\/api\/reserve\/?$/, doReserveList),
        rule('POST', /^\/api\/reserve\/?$/, doReserveCreate),
        rule('POST', /^\/api\/reserve\/(\d+)\/cancel\/?$/, doReserveCancel),
        rule('POST', /^\/api\/reserve\/(\d+)\/pick\/?$/, doReservePick),

        rule('GET', /^\/api\/fine\/?$/, doFineList),
        rule('POST', /^\/api\/fine\/(\d+)\/pay\/?$/, doFinePay),
        rule('POST', /^\/api\/fine\/(\d+)\/waive\/?$/, doFineWaive),

        rule('GET', /^\/api\/claim\/?$/, doClaimList),
        rule('POST', /^\/api\/claim\/report\/?$/, doClaimReport),
        rule('POST', /^\/api\/claim\/(\d+)\/handle\/?$/, doClaimHandle),

        rule('GET', /^\/api\/dashboard\/?$/, doDashboard),

        rule('GET', /^\/api\/notice\/?$/, doNoticeList),
        rule('POST', /^\/api\/notice\/read-all\/?$/, doNoticeReadAll),
        rule('POST', /^\/api\/notice\/(\d+)\/read\/?$/, doNoticeRead),

        rule('GET', /^\/api\/credit\/?$/, doCreditList),
        rule('POST', /^\/api\/credit\/adjust\/?$/, doCreditAdjust),

        rule('GET', /^\/api\/suggestion\/?$/, doSuggestionList),
        rule('POST', /^\/api\/suggestion\/submit\/?$/, doSuggestionSubmit),
        rule('POST', /^\/api\/suggestion\/(\d+)\/vote\/?$/, doSuggestionVote),
        rule('POST', /^\/api\/suggestion\/(\d+)\/handle\/?$/, doSuggestionHandle),

        rule('GET', /^\/api\/inventory\/?$/, doInventoryList),
        rule('POST', /^\/api\/inventory\/create\/?$/, doInventoryCreate),
        rule('GET', /^\/api\/inventory\/(\d+)\/items\/?$/, doInventoryItems),
        rule('POST', /^\/api\/inventory\/(\d+)\/check\/?$/, doInventoryCheck),
        rule('POST', /^\/api\/inventory\/(\d+)\/start\/?$/, doInventoryStart),
        rule('POST', /^\/api\/inventory\/(\d+)\/finish\/?$/, doInventoryFinish),
        rule('POST', /^\/api\/inventory\/(\d+)\/cancel\/?$/, doInventoryCancel),

        rule('GET', /^\/api\/user\/profile\/?$/, doProfile),
        rule('PUT', /^\/api\/user\/password\/?$/, doPassword),
        rule('GET', /^\/api\/user\/?$/, doUserList),
        rule('POST', /^\/api\/user\/?$/, doUserCreate),
        rule('PUT', /^\/api\/user\/(\d+)\/status\/?$/, doUserStatus),
        rule('PUT', /^\/api\/user\/(\d+)\/?$/, doUserUpdate),
        rule('DELETE', /^\/api\/user\/(\d+)\/?$/, doUserDelete),

        rule('GET', /^\/api\/tier\/?$/, doTierList),
        rule('POST', /^\/api\/tier\/?$/, doTierCreate),
        rule('PUT', /^\/api\/tier\/(\d+)\/?$/, doTierUpdate),
        rule('DELETE', /^\/api\/tier\/(\d+)\/?$/, doTierDelete),

        rule('GET', /^\/api\/admin\/perms\/?$/, doAdminPerms),
        rule('GET', /^\/api\/admin\/admins\/?$/, doAdminAdmins),
        rule('POST', /^\/api\/admin\/grant\/?$/, doAdminGrant),
        rule('POST', /^\/api\/admin\/revoke\/?$/, doAdminRevoke),

        rule('GET', /^\/api\/log\/?$/, doLogList),
        rule('POST', /^\/api\/log\/?$/, doLogWrite),

        rule('POST', /^\/api\/upload\/?$/, doUpload)
    ];

    function dispatch(method, path, q, body) {
        for (var i = 0; i < RULES.length; i++) {
            var r = RULES[i];
            if (r.method !== method) continue;
            var match = path.match(r.re);
            if (match) return r.fn(match, q, body);
        }
        return json({ success: false, message: '演示环境未实现该接口：' + method + ' ' + path }, 404);
    }

    // ============================================================
    // 覆盖 fetch：拦截所有 /api/* 请求
    // ============================================================
    window.fetch = function (input, init) {
        init = init || {};
        var url = typeof input === 'string' ? input : ((input && input.url) || '');
        var idx = url.indexOf('/api/');

        if (idx === -1) {
            if (originalFetch) return originalFetch(input, init);
            return Promise.reject(new Error('演示环境不访问外部网络'));
        }

        var method = (init.method || (input && input.method) || 'GET').toUpperCase();
        var raw = url.slice(idx);
        var qIdx = raw.indexOf('?');
        var path = qIdx === -1 ? raw : raw.slice(0, qIdx);

        var q = {};
        if (qIdx !== -1) {
            new URLSearchParams(raw.slice(qIdx + 1)).forEach(function (v, k) { q[k] = v; });
        }

        var body = parseBody(init.body);
        var result;
        try {
            result = dispatch(method, path, q, body);
        } catch (e) {
            if (window.console && console.warn) console.warn('[demo-mock] 处理失败：' + path, e);
            result = json({ success: false, message: '演示数据处理失败' }, 500);
        }
        return Promise.resolve(result);
    };

    // 封面路径改为相对路径（docs/upload/*.jpg），不依赖后端
    window.resolveImage = function (url) {
        if (!url) return '';
        if (url.indexOf(DEMO_ORIGIN) === 0) {
            return url.slice(DEMO_ORIGIN.length).replace(/^\/+/, '');
        }
        if (/^https?:\/\//i.test(url)) return url;
        return url.replace(/^\/+/, '');
    };
})();
