USE db_book;

UPDATE tb_user SET role = 2 WHERE username = 'user1';
UPDATE tb_user SET role = 3 WHERE username = 'user2';
UPDATE tb_user SET role = 4 WHERE username = 'user3';

INSERT INTO tb_user (username, password, name, role, email, phone) VALUES
('teacher1', 'teacher123', '李老师', 3, 'teacher1@example.com', '13800138004'),
('student1', 'student123', '小明', 2, 'student1@example.com', '13800138005'),
('social1', 'social123', '王先生', 4, 'social1@example.com', '13800138006');