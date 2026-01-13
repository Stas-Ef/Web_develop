ALTER TABLE student
    ALTER COLUMN age SET DEFAULT 20;

ALTER TABLE student
    ADD CONSTRAINT student_age_check CHECK (age >= 16);

ALTER TABLE student
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE student
    ADD CONSTRAINT student_name_unique UNIQUE (name);


ALTER TABLE faculty
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE faculty
    ALTER COLUMN color SET NOT NULL;

ALTER TABLE faculty
    ADD CONSTRAINT faculty_name_color_unique UNIQUE (name, color);