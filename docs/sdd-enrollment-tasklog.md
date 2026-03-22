// ...SDD for student enrollment and task logging...
SDD: Student Enrollment and Task Logging

Scope
- Allow authenticated students to enroll in courses and track their learning tasks.
- Enforce max 3 concurrent enrollments per student.
- Track enrollment date and expected completion (enrollment + 6 months).
- Allow students to CRUD task logs tied to their own enrollments.

Domain
- Enrollment(id, student, course, enrollmentDate, expectedCompletionDate) with unique (student, course).
- TaskLog(id, enrollment, date, category, description, timeSpentMinutes).
- TaskCategory: PESQUISA | PRATICA | ASSISTIR_VIDEOAULA.

Business Rules
- Student may have at most 3 enrollments.
- expectedCompletionDate = enrollmentDate + 6 months.
- Task timeSpentMinutes must be >0 and multiple of 30.
- Students may only access their own enrollments and task logs.

APIs
- POST /enrollments — body: {courseId}; creates enrollment for authenticated student.
- GET /enrollments/my — list enrollments for authenticated student.
- POST /tasks — create task log.
- PUT /tasks/{id} — update task log (ownership enforced).
- DELETE /tasks/{id} — delete task log (ownership enforced).
- GET /tasks — list task logs for authenticated student (optional date filter).

Validation/Error Handling
- 404 when course/enrollment/task not found for student.
- 400 on business violations (max enrollments, duplicate enrollment, invalid time increment).

Security
- Endpoints restricted to ROLE_STUDENT (already provided by JWT and SecurityConfig).
- Ownership enforced in services via student id checks.

Data Access
- EnrollmentRepository: countByStudentId, existsByStudentIdAndCourseId, findByStudentId, findByIdAndStudentId.
- TaskLogRepository: findAllByEnrollmentStudentId, findByIdAndEnrollmentStudentId, optional date range filter.

