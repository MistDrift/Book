let currentUser = null;

// 日期格式化：毫秒时间戳或日期字符串 -> yyyy-MM-dd
function formatDate(v) {
    if (v === null || v === undefined || v === '') return '';
    let d;
    if (typeof v === 'number') {
        d = new Date(v);
    } else if (/^\d+$/.test(String(v))) {
        d = new Date(parseInt(v));
    } else {
        d = new Date(v);
    }
    if (isNaN(d.getTime())) return '';
    const p = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

document.addEventListener('DOMContentLoaded', function() {
    console.log('book-list.js 加载完成');
    
    const modal = document.getElementById('bookModal');
    if (modal) {
        modal.classList.remove('show');
    }
    
    if (!checkLogin()) {
        return;
    }
    loadBooks();
    document.getElementById('bookForm').addEventListener('submit', saveBook);
});

function checkLogin() {
    console.log('book-list 开始登录验证');
    const storedUser = localStorage.getItem('user');
    console.log('localStorage user:', storedUser);
    
    if (!storedUser || !getToken()) {
        console.log('没有用户信息，跳转登录');
        window.location.href = 'index.html';
        return false;
    }
    
    try {
        currentUser = JSON.parse(storedUser);
        console.log('解析用户:', currentUser);
        
        if (!currentUser || !currentUser.username) {
            console.log('用户数据无效');
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
        
        if (!isAdmin(currentUser)) {
            const addBtn = document.getElementById('addBtn');
            if (addBtn) {
                addBtn.style.display = 'none';
            }
        }
        
        console.log('验证通过');
        return true;
    } catch (e) {
        console.error('验证出错:', e);
        localStorage.removeItem('user');
        window.location.href = 'index.html';
        return false;
    }
}

function logout() {
    console.log('退出登录');
    apiFetch('/api/logout', { method: 'POST' }).catch(function() {});
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    window.location.href = 'index.html';
}

// 20本书的mock数据（带图片）
const mockBooks = [
    { id: 1, isbn: '9787111213826', title: 'Java编程思想', author: 'Bruce Eckel', publisher: '机械工业出版社', publishDate: '2007-06-01', category: '编程', price: 108.00, stock: 5, description: 'Java学习必读经典', image: 'https://picsum.photos/200/200?random=1' },
    { id: 2, isbn: '9787115428577', title: 'Python编程从入门到实践', author: 'Eric Matthes', publisher: '人民邮电出版社', publishDate: '2016-07-01', category: '编程', price: 79.00, stock: 10, description: 'Python入门经典', image: 'https://picsum.photos/200/200?random=2' },
    { id: 3, isbn: '9787111544937', title: '深入理解计算机系统', author: 'Randal E.Bryant', publisher: '机械工业出版社', publishDate: '2015-11-01', category: '计算机', price: 129.00, stock: 3, description: '计算机系统经典教材', image: 'https://picsum.photos/200/200?random=3' },
    { id: 4, isbn: '9787115461476', title: '深度学习入门', author: '斋藤康毅', publisher: '人民邮电出版社', publishDate: '2017-05-01', category: 'AI', price: 69.00, stock: 8, description: '深度学习入门书籍', image: 'https://picsum.photos/200/200?random=4' },
    { id: 5, isbn: '9787111075547', title: '设计模式', author: 'Erich Gamma', publisher: '机械工业出版社', publishDate: '2002-09-01', category: '编程', price: 59.00, stock: 4, description: '设计模式经典', image: 'https://picsum.photos/200/200?random=5' },
    { id: 6, isbn: '9787111407010', title: '算法导论', author: 'Thomas H.Cormen', publisher: '机械工业出版社', publishDate: '2012-12-01', category: '算法', price: 128.00, stock: 2, description: '算法领域经典教材', image: 'https://picsum.photos/200/200?random=6' },
    { id: 7, isbn: '9787111275790', title: 'JavaScript高级程序设计', author: 'Nicholas C.Zakas', publisher: '人民邮电出版社', publishDate: '2012-03-01', category: '编程', price: 99.00, stock: 6, description: 'JavaScript权威指南', image: 'https://picsum.photos/200/200?random=7' },
    { id: 8, isbn: '9787111234909', title: '数据结构与算法分析', author: 'Mark Allen Weiss', publisher: '机械工业出版社', publishDate: '2014-01-01', category: '算法', price: 69.00, stock: 5, description: '数据结构与算法', image: 'https://picsum.photos/200/200?random=8' },
    { id: 9, isbn: '9787111453781', title: '计算机网络', author: 'James F.Kurose', publisher: '机械工业出版社', publishDate: '2013-06-01', category: '网络', price: 89.00, stock: 3, description: '计算机网络经典', image: 'https://picsum.photos/200/200?random=9' },
    { id: 10, isbn: '9787111321330', title: '操作系统概念', author: 'Abraham Silberschatz', publisher: '高等教育出版社', publishDate: '2010-06-01', category: '操作系统', price: 79.00, stock: 4, description: '操作系统经典', image: 'https://picsum.photos/200/200?random=10' },
    { id: 11, isbn: '9787111251217', title: '编译原理', author: 'Alfred V.Aho', publisher: '机械工业出版社', publishDate: '2008-12-01', category: '编译', price: 68.00, stock: 2, description: '编译原理经典', image: 'https://picsum.photos/200/200?random=11' },
    { id: 12, isbn: '9787111320900', title: '人工智能', author: 'Stuart Russell', publisher: '人民邮电出版社', publishDate: '2010-05-01', category: 'AI', price: 128.00, stock: 3, description: '人工智能经典教材', image: 'https://picsum.photos/200/200?random=12' },
    { id: 13, isbn: '9787115480114', title: 'C++ Primer', author: 'Stanley B.Lippman', publisher: '电子工业出版社', publishDate: '2017-09-01', category: '编程', price: 128.00, stock: 4, description: 'C++经典教程', image: 'https://picsum.photos/200/200?random=13' },
    { id: 14, isbn: '9787111275554', title: 'Effective Java', author: 'Joshua Bloch', publisher: '机械工业出版社', publishDate: '2018-01-01', category: '编程', price: 69.00, stock: 5, description: 'Java最佳实践', image: 'https://picsum.photos/200/200?random=14' },
    { id: 15, isbn: '9787508353937', title: 'Head First设计模式', author: 'Eric Freeman', publisher: '中国电力出版社', publishDate: '2007-09-01', category: '编程', price: 99.00, stock: 3, description: '设计模式入门', image: 'https://picsum.photos/200/200?random=15' },
    { id: 16, isbn: '9787115423474', title: 'Go语言实战', author: 'William Kennedy', publisher: '人民邮电出版社', publishDate: '2017-03-01', category: '编程', price: 69.00, stock: 4, description: 'Go语言实践', image: 'https://picsum.photos/200/200?random=16' },
    { id: 17, isbn: '9787111464747', title: 'Redis设计与实现', author: '黄健宏', publisher: '人民邮电出版社', publishDate: '2014-06-01', category: '数据库', price: 79.00, stock: 3, description: 'Redis内部设计', image: 'https://picsum.photos/200/200?random=17' },
    { id: 18, isbn: '9787111340752', title: 'MySQL技术内幕', author: '姜承尧', publisher: '机械工业出版社', publishDate: '2013-05-01', category: '数据库', price: 69.00, stock: 4, description: 'MySQL内核分析', image: 'https://picsum.photos/200/200?random=18' },
    { id: 19, isbn: '9787111498847', title: 'Spring实战', author: 'Craig Walls', publisher: '人民邮电出版社', publishDate: '2016-08-01', category: '编程', price: 89.00, stock: 5, description: 'Spring框架实战', image: 'https://picsum.photos/200/200?random=19' },
    { id: 20, isbn: '9787115485706', title: 'Vue.js实战', author: '梁灏', publisher: '人民邮电出版社', publishDate: '2017-11-01', category: '编程', price: 79.00, stock: 6, description: 'Vue.js入门与实践', image: 'https://picsum.photos/200/200?random=20' }
];

function loadBooks() {
    console.log('加载图书列表');
    apiFetch('/api/book')
    .then(response => response.json())
    .then(books => {
        renderBooks(books);
    })
    .catch(() => {
        console.log('使用mock数据');
        renderBooks(mockBooks);
    });
}

function renderBooks(books) {
    const tbody = document.getElementById('bookTable');
    tbody.innerHTML = '';
    
    if (!Array.isArray(books) || books.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align:center;">暂无图书</td></tr>';
        return;
    }
    
    books.forEach(book => {
        const row = document.createElement('tr');
        const actions = currentUser && isAdmin(currentUser) ? `
            <td>
                <button class="btn btn-edit" onclick="showEditModal(${book.id})">编辑</button>
                <button class="btn btn-delete" onclick="deleteBook(${book.id})">删除</button>
            </td>
        ` : '<td>-</td>';
        
        row.innerHTML = `
            <td><img src="${resolveImage(book.image) || 'https://picsum.photos/50/50?random=book'}" alt="${book.title || '封面'}" style="width:50px;height:50px;object-fit:cover;border-radius:4px;"></td>
            <td>${book.isbn || '-'}</td>
            <td>${book.title || '未知'}</td>
            <td>${book.author || '未知'}</td>
            <td>${book.publisher || '-'}</td>
            <td>${book.category || '-'}</td>
            <td>¥${(book.price || 0).toFixed(2)}</td>
            <td>${book.stock || 0}</td>
            ${actions}
        `;
        tbody.appendChild(row);
    });
}

function showAddModal() {
    document.getElementById('modalTitle').textContent = '添加图书';
    document.getElementById('bookId').value = '';
    document.getElementById('isbn').value = '';
    document.getElementById('title').value = '';
    document.getElementById('author').value = '';
    document.getElementById('publisher').value = '';
    document.getElementById('publishDate').value = '';
    document.getElementById('category').value = '编程';
    document.getElementById('price').value = '';
    document.getElementById('stock').value = '';
    document.getElementById('description').value = '';
    document.getElementById('image').value = '';
    document.getElementById('imageFile').value = '';
    document.getElementById('imagePreview').style.display = 'none';
    
    const modal = document.getElementById('bookModal');
    modal.classList.add('show');
    modal.onclick = function(e) {
        if (e.target === modal) {
            closeModal();
        }
    };
}

function showEditModal(id) {
    apiFetch(`/api/book/${id}`)
    .then(response => response.json())
    .then(book => {
        if (book) {
            document.getElementById('modalTitle').textContent = '编辑图书';
            document.getElementById('bookId').value = book.id;
            document.getElementById('isbn').value = book.isbn || '';
            document.getElementById('title').value = book.title || '';
            document.getElementById('author').value = book.author || '';
            document.getElementById('publisher').value = book.publisher || '';
            document.getElementById('publishDate').value = formatDate(book.publishDate);
            document.getElementById('category').value = book.category || '编程';
            document.getElementById('price').value = book.price || '';
            document.getElementById('stock').value = book.stock || '';
            document.getElementById('description').value = book.description || '';
            document.getElementById('image').value = book.image || '';

            if (book.image) {
                document.getElementById('imagePreview').src = resolveImage(book.image);
                document.getElementById('imagePreview').style.display = 'block';
            } else {
                document.getElementById('imagePreview').style.display = 'none';
            }

            const modal = document.getElementById('bookModal');
            modal.classList.add('show');
            modal.onclick = function(e) {
                if (e.target === modal) {
                    closeModal();
                }
            };
        }
    })
    .catch(() => {
        const book = mockBooks.find(b => b.id === id);
        if (book) {
            document.getElementById('modalTitle').textContent = '编辑图书';
            document.getElementById('bookId').value = book.id;
            document.getElementById('isbn').value = book.isbn || '';
            document.getElementById('title').value = book.title || '';
            document.getElementById('author').value = book.author || '';
            document.getElementById('publisher').value = book.publisher || '';
            document.getElementById('publishDate').value = formatDate(book.publishDate);
            document.getElementById('category').value = book.category || '编程';
            document.getElementById('price').value = book.price || '';
            document.getElementById('stock').value = book.stock || '';
            document.getElementById('description').value = book.description || '';
            document.getElementById('image').value = book.image || '';
            
            if (book.image) {
                document.getElementById('imagePreview').src = resolveImage(book.image);
                document.getElementById('imagePreview').style.display = 'block';
            } else {
                document.getElementById('imagePreview').style.display = 'none';
            }
            
            const modal = document.getElementById('bookModal');
            modal.classList.add('show');
            modal.onclick = function(e) {
                if (e.target === modal) {
                    closeModal();
                }
            };
        }
    });
}

function closeModal() {
    document.getElementById('bookModal').classList.remove('show');
}

function saveBook(e) {
    e.preventDefault();
    
    const id = document.getElementById('bookId').value;
    const book = {
        isbn: document.getElementById('isbn').value,
        title: document.getElementById('title').value,
        author: document.getElementById('author').value,
        publisher: document.getElementById('publisher').value,
        publishDate: document.getElementById('publishDate').value,
        category: document.getElementById('category').value,
        price: parseFloat(document.getElementById('price').value),
        stock: parseInt(document.getElementById('stock').value),
        description: document.getElementById('description').value,
        image: document.getElementById('image').value
    };
    
    if (id) {
        book.id = parseInt(id);
        apiFetch('/api/book', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams(book).toString()
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('更新成功');
                closeModal();
                loadBooks();
            }
        })
        .catch(() => {
            const index = mockBooks.findIndex(b => b.id === parseInt(id));
            if (index !== -1) {
                mockBooks[index] = { ...mockBooks[index], ...book };
                alert('更新成功');
                closeModal();
                renderBooks(mockBooks);
            }
        });
    } else {
        apiFetch('/api/book', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams(book).toString()
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('添加成功');
                closeModal();
                loadBooks();
            }
        })
        .catch(() => {
            book.id = Date.now();
            mockBooks.push(book);
            alert('添加成功');
            closeModal();
            renderBooks(mockBooks);
        });
    }
}

function deleteBook(id) {
    if (!confirm('确定要删除这本书吗？')) return;
    
    apiFetch(`/api/book/${id}`, { method: 'DELETE' })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            loadBooks();
        }
    })
    .catch(() => {
        const index = mockBooks.findIndex(b => b.id === id);
        if (index !== -1) {
            mockBooks.splice(index, 1);
            renderBooks(mockBooks);
            alert('删除成功');
        }
    });
}

function uploadImage() {
    const fileInput = document.getElementById('imageFile');
    const imageInput = document.getElementById('image');
    const preview = document.getElementById('imagePreview');
    
    const file = fileInput.files[0];
    if (!file) return;
    
    const reader = new FileReader();
    reader.onload = function(e) {
        imageInput.value = e.target.result;
        preview.src = e.target.result;
        preview.style.display = 'block';
    };
    reader.readAsDataURL(file);
}
