-- AceFlow Seed Data
-- Passwords are BCrypt hashed. Plain text: admin123 / student123

INSERT INTO users (name, email, password_hash, xp_points, streak_days, role) VALUES
    ('Admin',         'admin@aceflow.kz',   '$2a$12$K8sQZ5LfN3mP7tX2vR9oOeWbA1cD4gHjIlMnPqRsTuVwXyZ0123456', 9999, 30, 'ADMIN'),
    ('Yeldar Bazarbay','yeldar@example.kz', '$2a$12$K8sQZ5LfN3mP7tX2vR9oOeWbA1cD4gHjIlMnPqRsTuVwXyZ0123456', 3840, 12, 'STUDENT'),
    ('Aizat Kalieva', 'aizat@example.kz',  '$2a$12$K8sQZ5LfN3mP7tX2vR9oOeWbA1cD4gHjIlMnPqRsTuVwXyZ0123456', 1200, 5,  'STUDENT');

INSERT INTO achievements (title, description, xp_reward, category, condition_type, condition_value) VALUES
    ('7-Day Streak',   'Study 7 days in a row',      100,  'STREAK',    'STREAK_DAYS',      7),
    ('Speed Demon',    'Score 100% under 5 minutes',  200,  'TESTS',     'PERFECT_SCORE',    1),
    ('Bookworm',       'Upload 10 documents',         150,  'DOCUMENTS', 'DOCUMENTS_COUNT',  10),
    ('First Perfect',  'Score 100% on any test',      300,  'TESTS',     'PERFECT_SCORE',    1),
    ('Card Master',    'Master 100 flashcards',       250,  'FLASHCARDS','MASTERED_CARDS',   100),
    ('Night Owl',      'Study after 11 PM',            50,  'STREAK',    'NIGHT_SESSION',    1),
    ('Early Bird',     'Study before 7 AM',            50,  'STREAK',    'MORNING_SESSION',  1),
    ('30-Day Streak',  'Study 30 days in a row',      500,  'STREAK',    'STREAK_DAYS',      30),
    ('Test Machine',   'Complete 50 tests',           400,  'TESTS',     'TESTS_COUNT',      50),
    ('Goal Crusher',   'Complete 10 goals',           350,  'GOALS',     'GOALS_COMPLETED',  10),
    ('Grand Master',   'Reach Level 20',             1000,  'LEVEL',     'USER_LEVEL',       20),
    ('Scholar',        'Master 500 flashcards',       800,  'FLASHCARDS','MASTERED_CARDS',   500);

INSERT INTO study_plans (user_id, exam_name, exam_type, exam_date, start_date, status) VALUES
    (2, 'IELTS Academic', 'LANGUAGE', '2026-08-15', '2026-06-01', 'ACTIVE');

INSERT INTO goals (user_id, title, deadline, is_completed, xp_reward, priority) VALUES
    (2, 'Complete 10 practice tests', '2026-07-01', false, 500, 'HIGH'),
    (2, 'Master 200 flashcards',      '2026-07-10', false, 300, 'MEDIUM'),
    (2, 'Reach IELTS Band 7.5+',      '2026-08-15', false, 1000, 'HIGH'),
    (2, '30-day study streak',        '2026-07-20', false, 800, 'MEDIUM');

INSERT INTO tests (doc_id, title, test_type, time_limit, difficulty) VALUES
    (NULL, 'Macroeconomics Mid-term Mock',      'MULTIPLE_CHOICE', 30, 'HARD'),
    (NULL, 'Supply & Demand Concepts',          'MULTIPLE_CHOICE', 20, 'MEDIUM'),
    (NULL, 'Monetary Policy Fundamentals',      'MULTIPLE_CHOICE', 15, 'EASY'),
    (NULL, 'Trade & Globalization Quiz',        'MULTIPLE_CHOICE', 18, 'MEDIUM'),
    (NULL, 'IELTS Reading Practice Set 3',      'MIXED',           60, 'HARD');

INSERT INTO questions (test_id, question_text, correct_answer, option_a, option_b, option_c, option_d, points, explanation) VALUES
    (1, 'What does GDP stand for?',
        'A',
        'Gross Domestic Product',
        'General Development Plan',
        'Gross Development Plan',
        'Government Debt Policy',
        10,
        'GDP measures the total monetary value of all goods and services produced within a country.'),
    (1, 'Which is an example of fiscal policy?',
        'B',
        'Central bank raising interest rates',
        'Government increasing public spending',
        'Reducing money supply',
        'Lowering exchange rates',
        10,
        'Fiscal policy involves government decisions about spending and taxation.'),
    (1, 'When demand increases and supply stays constant, what happens to price?',
        'C',
        'Price falls',
        'Price stays the same',
        'Price rises',
        'Supply increases proportionally',
        10,
        'When demand increases with constant supply, equilibrium price rises.'),
    (1, 'What is the primary tool of monetary policy?',
        'B',
        'Tax rates',
        'Interest rates',
        'Government spending',
        'Trade tariffs',
        10,
        'Central banks use interest rates as their primary monetary policy tool.'),
    (1, 'Which best describes inflation?',
        'B',
        'Decrease in money supply',
        'General rise in price level over time',
        'Increase in government revenue',
        'Fall in unemployment',
        10,
        'Inflation is the general rise in price levels over time.');

INSERT INTO test_results (user_id, test_id, score, duration) VALUES
    (2, 1, 85.00, 24),
    (2, 2, 72.00, 18),
    (2, 3, 91.00, 12),
    (2, 4, 63.00, 16);
