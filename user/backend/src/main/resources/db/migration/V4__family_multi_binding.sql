SET @drop_old_unique = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'family_user_binding'
              AND INDEX_NAME = 'uk_family_user_binding_family'
        ),
        'ALTER TABLE family_user_binding DROP INDEX uk_family_user_binding_family',
        'SELECT 1'
    )
);
PREPARE stmt_drop_old_unique FROM @drop_old_unique;
EXECUTE stmt_drop_old_unique;
DEALLOCATE PREPARE stmt_drop_old_unique;

SET @add_family_index = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'family_user_binding'
              AND INDEX_NAME = 'idx_family_user_binding_family'
        ),
        'SELECT 1',
        'ALTER TABLE family_user_binding ADD KEY idx_family_user_binding_family (family_user_id)'
    )
);
PREPARE stmt_add_family_index FROM @add_family_index;
EXECUTE stmt_add_family_index;
DEALLOCATE PREPARE stmt_add_family_index;

SET @add_pair_unique = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'family_user_binding'
              AND INDEX_NAME = 'uk_family_user_binding_pair'
        ),
        'SELECT 1',
        'ALTER TABLE family_user_binding ADD UNIQUE KEY uk_family_user_binding_pair (family_user_id, vision_user_id)'
    )
);
PREPARE stmt_add_pair_unique FROM @add_pair_unique;
EXECUTE stmt_add_pair_unique;
DEALLOCATE PREPARE stmt_add_pair_unique;
