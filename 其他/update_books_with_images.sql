USE db_book;

-- 为每本书添加封面图片
UPDATE tb_book SET image = 'https://picsum.photos/seed/java1/200/300.jpg' WHERE id = 1;
UPDATE tb_book SET image = 'https://picsum.photos/seed/computer1/200/300.jpg' WHERE id = 2;
UPDATE tb_book SET image = 'https://picsum.photos/seed/python1/200/300.jpg' WHERE id = 3;
UPDATE tb_book SET image = 'https://picsum.photos/seed/database1/200/300.jpg' WHERE id = 4;
UPDATE tb_book SET image = 'https://picsum.photos/seed/algorithm1/200/300.jpg' WHERE id = 5;
UPDATE tb_book SET image = 'https://picsum.photos/seed/js1/200/300.jpg' WHERE id = 6;
UPDATE tb_book SET image = 'https://picsum.photos/seed/vue1/200/300.jpg' WHERE id = 7;
UPDATE tb_book SET image = 'https://picsum.photos/seed/javabook1/200/300.jpg' WHERE id = 8;

SELECT id, title, image FROM tb_book;