CREATE TABLE students (
    id UUID PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    birth_date DATE NOT NULL,
    email VARCHAR(120) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    password VARCHAR(120) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    CONSTRAINT student_email UNIQUE (email),
    CONSTRAINT student_role_check CHECK (role IN ('ADMIN', 'STUDENT'))
);

CREATE TABLE courses (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    CONSTRAINT course_name_unique UNIQUE (name)
);

CREATE TABLE enrollments (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    enrollment_date DATE NOT NULL,
    expected_completion_date DATE NOT NULL,
    CONSTRAINT enrollment_student_course_unique UNIQUE (student_id, course_id),
    CONSTRAINT enrollment_student_fk FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT enrollment_course_fk FOREIGN KEY (course_id) REFERENCES courses (id)
);

CREATE TABLE task_logs (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL,
    task_date DATE NOT NULL,
    category VARCHAR(40) NOT NULL,
    description VARCHAR(500) NOT NULL,
    time_spent_minutes INTEGER NOT NULL,
    CONSTRAINT task_log_enrollment_fk FOREIGN KEY (enrollment_id) REFERENCES enrollments (id),
    CONSTRAINT task_log_category_check CHECK (category IN ('PESQUISA', 'PRATICA', 'ASSISTIR_VIDEOAULA'))
);
