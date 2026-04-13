CREATE TABLE IF NOT EXISTS admin_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  phone VARCHAR(20) DEFAULT NULL,
  email VARCHAR(100) DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_login_at DATETIME DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  phone VARCHAR(20) DEFAULT NULL,
  email VARCHAR(100) DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  last_login_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS scan_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  session_id VARCHAR(100) DEFAULT NULL,
  mode VARCHAR(30) NOT NULL,
  trigger_command VARCHAR(255) DEFAULT NULL,
  scene VARCHAR(255) DEFAULT NULL,
  narration TEXT,
  position_summary VARCHAR(255) DEFAULT NULL,
  captured_at DATETIME NOT NULL,
  latitude DOUBLE DEFAULT NULL,
  longitude DOUBLE DEFAULT NULL,
  location_text VARCHAR(255) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS scan_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  record_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  position VARCHAR(100) DEFAULT NULL,
  confidence INT DEFAULT NULL,
  attributes_json TEXT,
  is_primary TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS family_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) DEFAULT NULL,
  email VARCHAR(100) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS family_binding (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_member_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  relationship VARCHAR(50) DEFAULT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medicine_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  family_member_id BIGINT DEFAULT NULL,
  medicine_name VARCHAR(100) NOT NULL,
  generic_name VARCHAR(100) DEFAULT NULL,
  description TEXT,
  dosage_usage TEXT,
  suitable_people TEXT,
  contraindications TEXT,
  expiry_date DATE DEFAULT NULL,
  barcode_or_alias VARCHAR(255) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medicine_scan_alert (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  scan_record_id BIGINT NOT NULL,
  medicine_profile_id BIGINT DEFAULT NULL,
  alert_type VARCHAR(30) NOT NULL,
  alert_message VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_username ON `user`(username);
CREATE UNIQUE INDEX uk_phone ON `user`(phone);
CREATE UNIQUE INDEX uk_email ON `user`(email);
CREATE INDEX idx_scan_record_user_time ON scan_record(user_id, captured_at);
CREATE INDEX idx_scan_record_session ON scan_record(session_id);
CREATE INDEX idx_scan_item_record ON scan_item(record_id);
CREATE INDEX idx_family_binding_family_user ON family_binding(family_member_id, user_id);
CREATE INDEX idx_medicine_profile_user ON medicine_profile(user_id, updated_at);
CREATE INDEX idx_medicine_alert_user_time ON medicine_scan_alert(user_id, created_at);
