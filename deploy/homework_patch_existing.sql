-- 当前项目部署用数据库补丁脚本（适用于已有 homework 库）
-- 用途：
-- 1. 已有旧库，缺字段/缺表时执行
-- 2. 不删除原有数据，只补齐当前代码所需结构
-- 3. 执行前请先备份数据库

USE `homework`;

SET @role_column_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user'
      AND COLUMN_NAME = 'role'
);

SET @role_column_sql = IF(
    @role_column_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT ''vision'' AFTER `email`',
    'SELECT 1'
);
PREPARE stmt_role FROM @role_column_sql;
EXECUTE stmt_role;
DEALLOCATE PREPARE stmt_role;

CREATE TABLE IF NOT EXISTS `admin_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `family_user_binding` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `family_user_id` BIGINT NOT NULL,
  `vision_user_id` BIGINT NOT NULL,
  `relationship` VARCHAR(50) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_care_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `subject_name` VARCHAR(50) DEFAULT NULL,
  `gender` VARCHAR(20) DEFAULT NULL,
  `age` VARCHAR(20) DEFAULT NULL,
  `vision_level` VARCHAR(50) DEFAULT NULL,
  `address` VARCHAR(255) DEFAULT NULL,
  `notes` TEXT,
  `emergency_name` VARCHAR(50) DEFAULT NULL,
  `emergency_relation` VARCHAR(50) DEFAULT NULL,
  `emergency_phone` VARCHAR(20) DEFAULT NULL,
  `emergency_backup_phone` VARCHAR(20) DEFAULT NULL,
  `medicine` TEXT,
  `disease_note` TEXT,
  `allergy` TEXT,
  `reminder` TEXT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @uk_family_binding_family_user_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'family_binding'
      AND INDEX_NAME = 'uk_family_binding_family_user'
);

SET @uk_family_binding_family_user_sql = IF(
    @uk_family_binding_family_user_exists = 0,
    'ALTER TABLE family_binding ADD UNIQUE KEY uk_family_binding_family_user (family_member_id, user_id)',
    'SELECT 1'
);
PREPARE stmt_family_binding_unique FROM @uk_family_binding_family_user_sql;
EXECUTE stmt_family_binding_unique;
DEALLOCATE PREPARE stmt_family_binding_unique;

SET @idx_family_user_binding_family_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'family_user_binding'
      AND INDEX_NAME = 'idx_family_user_binding_family'
);

SET @idx_family_user_binding_family_sql = IF(
    @idx_family_user_binding_family_exists = 0,
    'ALTER TABLE family_user_binding ADD KEY idx_family_user_binding_family (family_user_id)',
    'SELECT 1'
);
PREPARE stmt_family_user_binding_family FROM @idx_family_user_binding_family_sql;
EXECUTE stmt_family_user_binding_family;
DEALLOCATE PREPARE stmt_family_user_binding_family;

SET @idx_family_user_binding_vision_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'family_user_binding'
      AND INDEX_NAME = 'idx_family_user_binding_vision'
);

SET @idx_family_user_binding_vision_sql = IF(
    @idx_family_user_binding_vision_exists = 0,
    'ALTER TABLE family_user_binding ADD KEY idx_family_user_binding_vision (vision_user_id)',
    'SELECT 1'
);
PREPARE stmt_family_user_binding_vision FROM @idx_family_user_binding_vision_sql;
EXECUTE stmt_family_user_binding_vision;
DEALLOCATE PREPARE stmt_family_user_binding_vision;

SET @uk_family_user_binding_pair_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'family_user_binding'
      AND INDEX_NAME = 'uk_family_user_binding_pair'
);

SET @uk_family_user_binding_pair_sql = IF(
    @uk_family_user_binding_pair_exists = 0,
    'ALTER TABLE family_user_binding ADD UNIQUE KEY uk_family_user_binding_pair (family_user_id, vision_user_id)',
    'SELECT 1'
);
PREPARE stmt_family_user_binding_pair FROM @uk_family_user_binding_pair_sql;
EXECUTE stmt_family_user_binding_pair;
DEALLOCATE PREPARE stmt_family_user_binding_pair;

SET @uk_user_care_profile_user_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_care_profile'
      AND INDEX_NAME = 'uk_user_care_profile_user'
);

SET @uk_user_care_profile_user_sql = IF(
    @uk_user_care_profile_user_exists = 0,
    'ALTER TABLE user_care_profile ADD UNIQUE KEY uk_user_care_profile_user (user_id)',
    'SELECT 1'
);
PREPARE stmt_user_care_profile_unique FROM @uk_user_care_profile_user_sql;
EXECUTE stmt_user_care_profile_unique;
DEALLOCATE PREPARE stmt_user_care_profile_unique;

SET @idx_scan_record_user_time_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'scan_record'
      AND INDEX_NAME = 'idx_scan_record_user_time'
);

SET @idx_scan_record_user_time_sql = IF(
    @idx_scan_record_user_time_exists = 0,
    'ALTER TABLE scan_record ADD KEY idx_scan_record_user_time (user_id, captured_at)',
    'SELECT 1'
);
PREPARE stmt_scan_record_user_time FROM @idx_scan_record_user_time_sql;
EXECUTE stmt_scan_record_user_time;
DEALLOCATE PREPARE stmt_scan_record_user_time;

SET @idx_scan_record_session_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'scan_record'
      AND INDEX_NAME = 'idx_scan_record_session'
);

SET @idx_scan_record_session_sql = IF(
    @idx_scan_record_session_exists = 0,
    'ALTER TABLE scan_record ADD KEY idx_scan_record_session (session_id)',
    'SELECT 1'
);
PREPARE stmt_scan_record_session FROM @idx_scan_record_session_sql;
EXECUTE stmt_scan_record_session;
DEALLOCATE PREPARE stmt_scan_record_session;

SET @idx_scan_item_record_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'scan_item'
      AND INDEX_NAME = 'idx_scan_item_record'
);

SET @idx_scan_item_record_sql = IF(
    @idx_scan_item_record_exists = 0,
    'ALTER TABLE scan_item ADD KEY idx_scan_item_record (record_id)',
    'SELECT 1'
);
PREPARE stmt_scan_item_record FROM @idx_scan_item_record_sql;
EXECUTE stmt_scan_item_record;
DEALLOCATE PREPARE stmt_scan_item_record;

SET @idx_medicine_profile_user_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medicine_profile'
      AND INDEX_NAME = 'idx_medicine_profile_user'
);

SET @idx_medicine_profile_user_sql = IF(
    @idx_medicine_profile_user_exists = 0,
    'ALTER TABLE medicine_profile ADD KEY idx_medicine_profile_user (user_id, updated_at)',
    'SELECT 1'
);
PREPARE stmt_medicine_profile_user FROM @idx_medicine_profile_user_sql;
EXECUTE stmt_medicine_profile_user;
DEALLOCATE PREPARE stmt_medicine_profile_user;

SET @idx_medicine_alert_user_time_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medicine_scan_alert'
      AND INDEX_NAME = 'idx_medicine_alert_user_time'
);

SET @idx_medicine_alert_user_time_sql = IF(
    @idx_medicine_alert_user_time_exists = 0,
    'ALTER TABLE medicine_scan_alert ADD KEY idx_medicine_alert_user_time (user_id, created_at)',
    'SELECT 1'
);
PREPARE stmt_medicine_alert_user_time FROM @idx_medicine_alert_user_time_sql;
EXECUTE stmt_medicine_alert_user_time;
DEALLOCATE PREPARE stmt_medicine_alert_user_time;
