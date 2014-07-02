CREATE DATABASE IF NOT EXISTS dataorigin_test;
CREATE DATABASE IF NOT EXISTS dataorigin_test_editing;

CREATE USER 'dataorigin_test'@'localhost' IDENTIFIED BY 'dataorigin_test';
GRANT ALL PRIVILEGES ON `dataorigin_test`.* TO 'dataorigin_test'@'localhost';

CREATE USER 'dataorigin_test'@'localhost' IDENTIFIED BY 'dataorigin_test';
GRANT ALL PRIVILEGES ON `dataorigin_test_editing`.* TO 'dataorigin_test'@'localhost';

flush privileges;