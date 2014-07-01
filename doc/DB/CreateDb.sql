CREATE DATABASE IF NOT EXISTS dataorigin_test;

CREATE USER 'dataorigin_test'@'localhost' IDENTIFIED BY 'dataorigin_test';
GRANT ALL PRIVILEGES ON `dataorigin_test`.* TO 'dataorigin_test'@'localhost';


CREATE DATABASE IF NOT EXISTS dataorigin_test_editing;

CREATE USER 'dataorigin_test2'@'localhost' IDENTIFIED BY 'dataorigin_test2';
GRANT ALL PRIVILEGES ON `dataorigin_test_editing`.* TO 'dataorigin_test2'@'localhost';

flush privileges;