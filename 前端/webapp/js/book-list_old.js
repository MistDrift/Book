let currentUser = null;

document.addEventListener('DOMContentLoaded', function() {
    checkLogin();
    loadBooks();

    document.getElementById('bookForm').addEventListener('submit', saveBook);
});

function checkLogin() {
    fetch('/api/login')
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            currentUser = data.user;
            document.getElementById('userName').textContent = data.user.name;
            document.getElementById('userRole').textContent = data.user.role === 1 ? '(管理员)' : '(普通用户)';

            if (data.user.role === 1) {
                document.getElementById('userListLink').style.display = 'block';
            } else {
                document.getElementById('addBtn').style.display = 'none';
            }
        } else {
            checkLocalStorageUser();
        }
    })
    .catch(() => {
        checkLocalStorageUser();
    });
}

function checkLocalStorageUser() {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
        try {
            currentUser = JSON.parse(storedUser);
            document.getElementById('userName').textContent = currentUser.name;
            document.getElementById('userRole').textContent = currentUser.role === 1 ? '(管理员)' : '(普通用户)';

            if (currentUser.role === 1) {
                document.getElementById('userListLink').style.display = 'block';
            } else {
                document.getElementById('addBtn').style.display = 'none';
            }
        } catch (e) {
            window.location.href = 'index.html';
        }
    } else {
        window.location.href = 'index.html';
    }
}

function logout() {
    fetch('/api/logout', {
        method: 'POST'
    })
    .then(() => {
        localStorage.removeItem('user');
        window.location.href = 'index.html';
    })
    .catch(() => {
        localStorage.removeItem('user');
        window.location.href = 'index.html';
    });
}

const mockBooks = [
    { id: 1, isbn: '9787111544937', title: 'Java编程思想', author: 'Bruce Eckel', publisher: '机械工业出版社', publishDate: '2018-01-01', category: '编程', price: 109.00, stock: 5, description: 'Java学习经典书籍' },
    { id: 2, isbn: '9787508698875', title: '深入理解计算机系统', author: 'Randal E. Bryant', publisher: '机械工业出版社', publishDate: '2019-01-01', category: '编程', price: 129.00, stock: 3, description: '计算机系统导论' },
    { id: 3, isbn: '9787115428577', title: 'Python编程：从入门到实践', author: 'Eric Matthes', publisher: '人民邮电出版社', publishDate: '2020-01-01', category: '编程', price: 79.00, stock: 10, description: 'Python入门书籍' },
    { id: 4, isbn: '9787111407010', title: '算法导论', author: 'Thomas H. Cormen', publisher: '机械工业出版社', publishDate: '2017-01-01', category: '编程', price: 139.00, stock: 2, description: '算法经典教材' },
    { id: 5, isbn: '9787519807386', title: '三体', author: '刘慈欣', publisher: '重庆出版社', publishDate: '2015-01-01', category: '小说', price: 68.00, stock: 8, description: '科幻小说经典' },
    { id: 6, isbn: '9787532766923', title: '百年孤独', author: 'Gabriel García Márquez', publisher: '上海文艺出版社', publishDate: '2011-01-01', category: '小说', price: 59.00, stock: 6, description: '魔幻现实主义经典' }
];

function loadBooks() {
    fetch('/api/book')
    .then(response => response.json())
    .then(books => {
        renderBooks(books);
    })
    .catch(() => {
        renderBooks(mockBooks);
    });
}

function renderBooks(books) {
    const tbody = document.getElementById('bookTable');
    tbody.innerHTML = '';

    books.forEach(book => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${book.isbn}</td>
            <td>${book.title}</td>
            <td>${book.author}</td>
            <td>${book.publisher}</td>
            <td>${book.category}</td>
            <td>¥${book.price}</td>
            <td>${book.stock}</td>
            <td>
                ${currentUser && currentUser.role === 1 ? `
                    <button class="btn btn-edit" onclick="showEditModal(${book.id})">编辑</button>
                    <button class="btn btn-delete" onclick="deleteBook(${book.id})">删除</button>
                ` : ''}
                ${currentUser && currentUser.role === 0 && book.stock > 0 ? `
                    <button class="btn btn-borrow" onclick="borrowBook(${book.id})">借阅</button>
                ` : ''}
            </td>
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
    document.getElementById('bookModal').style.display = 'flex';
}

function showEditModal(id) {
    const book = mockBooks.find(b => b.id === id);
    if (book) {
        document.getElementById('modalTitle').textContent = '编辑图书';
        document.getElementById('bookId').value = book.id;
        document.getElementById('isbn').value = book.isbn;
        document.getElementById('title').value = book.title;
        document.getElementById('author').value = book.author;
        document.getElementById('publisher').value = book.publisher;
        document.getElementById('publishDate').value = book.publishDate;
        document.getElementById('category').value = book.category;
        document.getElementById('price').value = book.price;
        document.getElementById('stock').value = book.stock;
        document.getElementById('description').value = book.description || '';
        document.getElementById('bookModal').style.display = 'flex';
    } else {
        fetch(`/api/book/${id}`)
        .then(response => response.json())
        .then(book => {
            document.getElementById('modalTitle').textContent = '编辑图书';
            document.getElementById('bookId').value = book.id;
            document.getElementById('isbn').value = book.isbn;
            document.getElementById('title').value = book.title;
            document.getElementById('author').value = book.author;
            document.getElementById('publisher').value = book.publisher;
            document.getElementById('publishDate').value = book.publishDate ? book.publishDate.split('T')[0] : '';
            document.getElementById('category').value = book.category;
            document.getElementById('price').value = book.price;
            document.getElementById('stock').value = book.stock;
            document.getElementById('description').value = book.description || '';
            document.getElementById('bookModal').style.display = 'flex';
        });
    }
}

function closeModal() {
    document.getElementById('bookModal').style.display = 'none';
}

function saveBook(e) {
    e.preventDefault();

    const id = document.getElementById('bookId').value;
    const isbn = document.getElementById('isbn').value;
    const title = document.getElementById('title').value;
    const author = document.getElementById('author').value;
    const publisher = document.getElementById('publisher').value;
    const publishDate = document.getElementById('publishDate').value;
    const category = document.getElementById('category').value;
    const price = document.getElementById('price').value;
    const stock = document.getElementById('stock').value;
    const description = document.getElementById('description').value;

    const bookData = { isbn, title, author, publisher, publishDate, category, price: parseFloat(price), stock: parseInt(stock), description };

    if (id) {
        fetch('/api/book', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `id=${id}&isbn=${isbn}&title=${title}&author=${author}&publisher=${publisher}&publishDate=${publishDate}&category=${category}&price=${price}&stock=${stock}&description=${description}`
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                closeModal();
                loadBooks();
                alert('更新成功');
            } else {
                alert(data.message);
            }
        })
        .catch(() => {
            saveBookMock(id, bookData);
        });
    } else {
        fetch('/api/book', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `isbn=${isbn}&title=${title}&author=${author}&publisher=${publisher}&publishDate=${publishDate}&category=${category}&price=${price}&stock=${stock}&description=${description}`
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                closeModal();
                loadBooks();
                alert('添加成功');
            } else {
                alert(data.message);
            }
        })
        .catch(() => {
            saveBookMock(null, bookData);
        });
    }
}

function saveBookMock(id, bookData) {
    if (id) {
        const index = mockBooks.findIndex(b => b.id == id);
        if (index !== -1) {
            mockBooks[index] = { ...mockBooks[index], ...bookData };
            alert('更新成功');
        }
    } else {
        const newId = Math.max(...mockBooks.map(b => b.id), 0) + 1;
        mockBooks.push({ id: newId, ...bookData });
        alert('添加成功');
    }
    closeModal();
    renderBooks(mockBooks);
}

function deleteBook(id) {
    if (!confirm('确定要删除这本图书吗？')) return;

    fetch(`/api/book/${id}`, { method: 'DELETE' })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            loadBooks();
        } else {
            alert(data.message);
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

function borrowBook(bookId) {
    if (!confirm('确定要借阅这本图书吗？')) return;

    fetch('/api/borrow', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `bookId=${bookId}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('借阅成功');
            loadBooks();
        } else {
            alert(data.message);
        }
    })
    .catch(() => {
        const book = mockBooks.find(b => b.id === bookId);
        if (book && book.stock > 0) {
            book.stock -= 1;
            renderBooks(mockBooks);
            alert('借阅成功');
        } else {
            alert('库存不足');
        }
    });
}