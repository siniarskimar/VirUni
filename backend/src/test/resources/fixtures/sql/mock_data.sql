-- 1, 'ROLE_USER'
-- 2, 'ROLE_TEACHER'
-- 3, 'ROLE_ADMIN'

TRUNCATE TABLE grade CASCADE;
TRUNCATE TABLE subject_participants CASCADE;
TRUNCATE TABLE subject CASCADE;
TRUNCATE TABLE account CASCADE;

INSERT INTO account(id, firstname, lastname, username, password, role_id) VALUES
(1, 'Alicia', 'Price', 'aliciaprice', '$2a$10$Ev.DNUb0yjg/XK/YxhmiCenRzv3LqPcSAw7zeE8mt3Z1J5os7WpSq', 1), -- pass: magics
(2, 'Angela', 'Ramirez', 'ramirezangela', '$2a$10$Ev.DNUb0yjg/XK/YxhmiCenRzv3LqPcSAw7zeE8mt3Z1J5os7WpSq', 1), -- pass: magics
(3, 'Angelica', 'Charles', 'charlesangelica', '$2a$10$FNOo9memi9db8Wo60DVvAO/x.AHjv.K5pWH9RkSgHdS/ga2ez51oi', 2), -- pass: secret
(4, 'Maria', 'Santos', 'maria.santos', '$2a$10$QPn8UiG7z1g.N5Jo1iNFp.TxPztWlxaTqEYPpMCAZWmpBGuS4Az0S', 2), -- pass: T3stP@ssw0rd
(5, 'System', 'admin', 'admin', '$2a$10$rY.CN/kJ4CeOTD3hUM17LuuSFvFhPyOFGxDlJFNJ5Atp11Q8.cLdS', 3) -- pass: admin
;

INSERT INTO subject(id, created_at, description, name, leading_teacher_id) VALUES
(1, '2015-10-19 10:23:54+02', NULL, 'Principles of Macroeconomics', 3),
(2, '2015-10-19 10:40:43+02', NULL, 'Linear Algebra', 4)
;

INSERT INTO subject_participants(subjects_id, participants_id) VALUES
(1, 3),
(1, 1),
(2, 4),
(2, 1),
(2, 2)
;

INSERT INTO grade(id, creation, value, student_id, teacher_id, subject_id) VALUES
(1, '2015-12-19 15:23:45+02', 4.5,  1, 3, 1),
(2, '2016-03-01 11:23:54+02', 3.2,  2, 4, 2),
(3, '2015-12-19 15:23:43+02', 3.5,  1, 3, 1),
(4, '2016-03-01 11:24:06+02', 1.99, 2, 4, 2)
;
