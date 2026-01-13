CREATE TABLE car (
    id SERIAL PRIMARY KEY,
    brand VARCHAR(100),
    model VARCHAR(100),
    price DECIMAL
);

CREATE TABLE person (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    has_license BOOLEAN,
    car_id INT,
    CONSTRAINT fk_person_car
        FOREIGN KEY (car_id)
        REFERENCES car(id)
);