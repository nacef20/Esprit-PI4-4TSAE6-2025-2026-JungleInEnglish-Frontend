-- Run this script on your MySQL database to fix image_url update errors.
-- Execute: mysql -u root -p eventmodule < src/main/resources/sql/fix-evenement-image-url.sql
-- Or run each statement in MySQL Workbench / your DB client.

-- 1) Make image_url large enough for base64 images (TEXT = 64KB, MEDIUMTEXT = 16MB)
ALTER TABLE evenement MODIFY COLUMN image_url MEDIUMTEXT NULL;

-- 2) Allow large packets (optional: only if you still get "Packet for query is too large")
-- Run as a user with SUPER privilege, or add to my.ini / my.cnf: max_allowed_packet=32M
SET GLOBAL max_allowed_packet = 33554432;
