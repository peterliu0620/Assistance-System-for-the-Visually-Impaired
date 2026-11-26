-- =============================================
-- 视障辅助系统 - 数据库完整初始化脚本
-- =============================================
-- 使用说明：
-- 1. 以 root 用户或具有 CREATE USER 和 CREATE DATABASE 权限的用户登录 MySQL
-- 2. 执行此脚本
-- =============================================

-- 1. 创建数据库用户（如果不存在）
CREATE USER IF NOT EXISTS 'homework'@'%' IDENTIFIED BY 'Dog.1128620';
CREATE USER IF NOT EXISTS 'homework'@'localhost' IDENTIFIED BY 'Dog.1128620';

-- 2. 创建数据库
CREATE DATABASE IF NOT EXISTS `homework`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- 3. 授予权限
GRANT ALL PRIVILEGES ON `homework`.* TO 'homework'@'%';
GRANT ALL PRIVILEGES ON `homework`.* TO 'homework'@'localhost';
FLUSH PRIVILEGES;

-- 4. 使用数据库
USE `homework`;

-- 5. 创建数据表
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

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `role` VARCHAR(20) NOT NULL DEFAULT 'vision',
  `status` TINYINT NOT NULL DEFAULT 1,
  `last_login_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `scan_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `session_id` VARCHAR(100) DEFAULT NULL,
  `mode` VARCHAR(30) NOT NULL,
  `trigger_command` VARCHAR(255) DEFAULT NULL,
  `scene` VARCHAR(255) DEFAULT NULL,
  `narration` TEXT,
  `position_summary` VARCHAR(255) DEFAULT NULL,
  `captured_at` DATETIME NOT NULL,
  `latitude` DOUBLE DEFAULT NULL,
  `longitude` DOUBLE DEFAULT NULL,
  `location_text` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_scan_record_user_time` (`user_id`, `captured_at`),
  KEY `idx_scan_record_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `scan_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `record_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `position` VARCHAR(100) DEFAULT NULL,
  `confidence` INT DEFAULT NULL,
  `attributes_json` TEXT,
  `is_primary` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_scan_item_record` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `family_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `family_binding` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `family_member_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `relationship` VARCHAR(50) DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_binding_family_user` (`family_member_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `family_user_binding` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `family_user_id` BIGINT NOT NULL,
  `vision_user_id` BIGINT NOT NULL,
  `relationship` VARCHAR(50) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_user_binding_pair` (`family_user_id`, `vision_user_id`),
  KEY `idx_family_user_binding_family` (`family_user_id`),
  KEY `idx_family_user_binding_vision` (`vision_user_id`)
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_care_profile_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `medicine_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `family_member_id` BIGINT DEFAULT NULL,
  `medicine_name` VARCHAR(100) NOT NULL,
  `generic_name` VARCHAR(100) DEFAULT NULL,
  `description` TEXT,
  `dosage_usage` TEXT,
  `suitable_people` TEXT,
  `contraindications` TEXT,
  `expiry_date` DATE DEFAULT NULL,
  `barcode_or_alias` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_medicine_profile_user` (`user_id`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `medicine_scan_alert` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `scan_record_id` BIGINT NOT NULL,
  `medicine_profile_id` BIGINT DEFAULT NULL,
  `alert_type` VARCHAR(30) NOT NULL,
  `alert_message` VARCHAR(255) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_medicine_alert_user_time` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 数据库表结构创建完成
-- 注意：管理后台首次启动时会自动创建默认管理员账号（admin / 123456）

SELECT '数据库初始化完成！' AS message;
SELECT '请启动管理后台，系统会自动创建默认管理员账号' AS note;
SELECT '默认账号：admin / 123456' AS default_credentials;
