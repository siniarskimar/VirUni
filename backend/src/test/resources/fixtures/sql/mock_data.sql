TRUNCATE TABLE grade CASCADE;
TRUNCATE TABLE subject_participants CASCADE;
TRUNCATE TABLE subject CASCADE;
TRUNCATE TABLE account CASCADE;

-- 1, 'ROLE_USER'
-- 2, 'ROLE_TEACHER'
-- 3, 'ROLE_ADMIN'
INSERT INTO account(id, firstname, lastname, username, password, role_id) VALUES
(1, 'Alicia', 'Price', 'aliciaprice', '$2a$10$Ev.DNUb0yjg/XK/YxhmiCenRzv3LqPcSAw7zeE8mt3Z1J5os7WpSq', 1), -- pass: magics
(2, 'Angela', 'Ramirez', 'ramirezangela', '$2a$10$Ev.DNUb0yjg/XK/YxhmiCenRzv3LqPcSAw7zeE8mt3Z1J5os7WpSq', 1), -- pass: magics
(3, 'Angelica', 'Charles', 'charlesangelica', '$2a$10$FNOo9memi9db8Wo60DVvAO/x.AHjv.K5pWH9RkSgHdS/ga2ez51oi', 2), -- pass: secret
(4, 'Maria', 'Santos', 'maria.santos', '$2a$10$QPn8UiG7z1g.N5Jo1iNFp.TxPztWlxaTqEYPpMCAZWmpBGuS4Az0S', 2), -- pass: T3stP@ssw0rd
(5, 'System', 'admin', 'admin', '$2a$10$rY.CN/kJ4CeOTD3hUM17LuuSFvFhPyOFGxDlJFNJ5Atp11Q8.cLdS', 3), -- pass: admin
(6, 'Bruno', 'Keller', 'brunokeller', '$2a$10$Ev.DNUb0yjg/XK/YxhmiCenRzv3ZqPcSAw7zeE8mt3Z1J5os7WpSq', 1), -- pass: magics (regular, will NOT participate in any subject)
(7, 'Priya', 'Shah', 'priyashah',    '$2a$10$Ev.DNUb0yjg/XK/YxhmiCenRzv3ZqPcSAw7zeE8mt3Z1J5os7WpSq', 1), -- pass: magics (regular, will be participant but no grades)
(8, 'Lars', 'Nielsen', 'larsnielsen', '$2a$10$FNOo9memi9db8Wo60DVvAO/x.AHjv.K5pWH9RkSgHdS/ga2ez51oi', 2), -- pass: secret (teacher)
(9, 'Sofia', 'Martinez', 'sofiamartinez', '$2a$10$QPn8UiG7z1g.N5Jo1iNFp.TxPztWlxaTqEYPpMCAZWmpBGuS4Az0S', 2) -- pass: T3stP@ssw0rd (teacher)
;
ALTER SEQUENCE account_id_seq RESTART 10;

INSERT INTO subject(id, created_at, description, name, leading_teacher_id) VALUES
(1, '2015-10-19 10:23:54+02', NULL, 'Principles of Macroeconomics', 3), -- lead by Angelica Charles
(2, '2015-10-19 10:40:43+02', NULL, 'Linear Algebra', 4), -- lead by Maria Santos
(3, '2016-09-01 09:00:00+02', NULL, 'Introduction to Statistics', 8), -- lead by Lars Nielsen
(4, '2016-09-01 09:00:00+02', NULL, 'Data Structures', 9), -- lead by Sofia Martinez
(5, '2016-09-01 09:00:01+02', NULL, 'Philosophy of Science', 3), -- lead by Angelica Charles
(6, '2016-09-01 09:00:01+02', NULL, 'Operating Systems', 4) -- lead by Maria Santos
;
ALTER SEQUENCE subject_id_seq RESTART 7;

-- Note: account 6 (Bruno) does not participate in any subject
INSERT INTO subject_participants(subjects_id, participants_id) VALUES
(1, 1),  -- Alicia enrolled in Principles of Macroeconomics
(1, 3),  -- Angelica enrolled in Principles of Macroeconomics
(2, 1),  -- Alicia enrolled in Linear
(2, 2),  -- Angela enrolled in Linear Algebra
(2, 4),  -- Maria enrolled in Linear Algebra
(3, 1),  -- Alicia enrolled in Statistics
(3, 7),  -- Priya enrolled in Statistics (no grades)
(3, 8),  -- Lars (teacher) also listed as participant (if teachers appear as participants)
(4, 9),  -- Sofia (teacher) participant
(4, 1),  -- Alicia in Data Structures
(4, 2),  -- Angela in Data Structures
(5, 2),  -- Angela in Philosophy of Science
(5, 3),  -- Angelica (teacher) in Philosophy of Science (leading teacher also participant)
(5, 7),  -- Priya in Philosophy of Science (still no grades)
(6, 1),  -- Alicia in Operating Systems
(6, 4),  -- Maria (teacher) in Operating Systems
(6, 9)   -- Sofia in Operating Systems
;

-- Note: account 7 (Priya) is a participant but receives NO grades.
INSERT INTO grade(id, creation, value, student_id, teacher_id, subject_id) VALUES
(1, '2015-12-19 15:23:45+02', 4.5,  1, 3, 1),
(2, '2016-03-01 11:23:54+02', 3.2,  2, 4, 2),
(3, '2015-12-19 15:23:43+02', 3.5,  1, 3, 1),
(4, '2016-03-01 11:24:06+02', 1.99, 2, 4, 2),
(5, '2016-10-10 10:00:00+02', 5.0,  1, 8, 3),  -- Alicia: Statistics by Lars
(6, '2016-10-11 11:15:00+02', 4.2,  1, 9, 4),  -- Alicia: Data Structures by Sofia
(7, '2016-10-12 09:30:00+02', 3.8,  2, 9, 4),  -- Angela: Data Structures by Sofia
(8, '2016-10-13 14:45:00+02', 4.0,  2, 3, 5),  -- Angela: Philosophy of Science by Angelica
(9, '2016-10-14 12:00:00+02', 2.5,  1, 4, 6),  -- Alicia: Operating Systems by Maria
(10,'2016-10-15 13:30:00+02', 3.7,  3, 3, 5)   -- Angelica: Philosophy of Science by Angelica
;
ALTER SEQUENCE grade_id_seq RESTART 11;