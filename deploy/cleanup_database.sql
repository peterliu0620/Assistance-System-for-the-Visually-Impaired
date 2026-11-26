-- =============================================
-- 数据库清理脚本（用于重新初始化）
-- =============================================
-- 警告：此脚本会删除数据库和用户！
-- =============================================

-- 删除数据库
DROP DATABASE IF EXISTS `homework`;

-- 删除用户
DROP USER IF EXISTS 'homework'@'%';
DROP USER IF EXISTS 'homework'@'localhost';

FLUSH PRIVILEGES;

SELECT '数据库清理完成！' AS message;
