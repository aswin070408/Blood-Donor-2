CREATE DATABASE blood_donor_finder;

USE blood_donor_finder;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    email VARCHAR(100),
    city VARCHAR(100),
    blood_group VARCHAR(5)
);

CREATE TABLE donors (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    phone VARCHAR(15),
    city VARCHAR(100),
    available BOOLEAN DEFAULT TRUE
);

INSERT INTO donors
(name, blood_group, phone, city, available)
VALUES
('Arun', 'O+', '9876543210', 'Thanjavur', TRUE),
('Kumar', 'A+', '9876543211', 'Trichy', TRUE),
('Ravi', 'B+', '9876543212', 'Chennai', TRUE),
('Vijay', 'O-', '9876543213', 'Kumbakonam', TRUE);