-- DUPLICATED SMART CUBIC FIX
UPDATE `character_skills` SET `skill_id` = 781 WHERE `skill_id` = 779 AND `class_index` = 0 AND `charid` IN ( SELECT `charid` FROM `characters` WHERE `base_class` = 96 );
UPDATE `character_skills` SET `skill_id` = 781 WHERE `skill_id` = 779 AND `class_index` = 1 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 1 ); 
UPDATE `character_skills` SET `skill_id` = 781 WHERE `skill_id` = 779 AND `class_index` = 2 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 2 ); 
UPDATE `character_skills` SET `skill_id` = 781 WHERE `skill_id` = 779 AND `class_index` = 3 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 3 ); 
 
UPDATE `character_skills` SET `skill_id` = 782 WHERE `skill_id` = 779 AND `class_index` = 0 AND `charid` IN ( SELECT `charid` FROM `characters` WHERE `base_class` = 104 );
UPDATE `character_skills` SET `skill_id` = 782 WHERE `skill_id` = 779 AND `class_index` = 1 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 1); 
UPDATE `character_skills` SET `skill_id` = 782 WHERE `skill_id` = 779 AND `class_index` = 2 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 2); 
UPDATE `character_skills` SET `skill_id` = 782 WHERE `skill_id` = 779 AND `class_index` = 3 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 3); 
 
UPDATE `character_skills` SET `skill_id` = 780 WHERE `skill_id` = 779 AND `class_index` = 0 AND `charid` IN ( SELECT `charid` FROM `characters` WHERE `base_class` = 106 );
UPDATE `character_skills` SET `skill_id` = 780 WHERE `skill_id` = 779 AND `class_index` = 1 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 1 );
UPDATE `character_skills` SET `skill_id` = 780 WHERE `skill_id` = 779 AND `class_index` = 2 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 2 );
UPDATE `character_skills` SET `skill_id` = 780 WHERE `skill_id` = 779 AND `class_index` = 3 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 3 );
 
UPDATE `character_skills` SET `skill_id` = 783 WHERE `skill_id` = 779 AND `class_index` = 0 AND `charid` IN ( SELECT `charid` FROM `characters` WHERE `base_class` = 111 );
UPDATE `character_skills` SET `skill_id` = 783 WHERE `skill_id` = 779 AND `class_index` = 1 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 1 );
UPDATE `character_skills` SET `skill_id` = 783 WHERE `skill_id` = 779 AND `class_index` = 2 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 2 );
UPDATE `character_skills` SET `skill_id` = 783 WHERE `skill_id` = 779 AND `class_index` = 3 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 3 );

-- UPDATE SMART CUBIC SHORTCUTS
UPDATE `character_shortcuts` SET `shortcut_id` = 781 WHERE `shortcut_id` = 779 AND `class_index` = 0 AND `charid` IN ( SELECT `charid` FROM `characters` WHERE `base_class` = 96 );
UPDATE `character_shortcuts` SET `shortcut_id` = 781 WHERE `shortcut_id` = 779 AND `class_index` = 1 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 1 ); 
UPDATE `character_shortcuts` SET `shortcut_id` = 781 WHERE `shortcut_id` = 779 AND `class_index` = 2 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 2 ); 
UPDATE `character_shortcuts` SET `shortcut_id` = 781 WHERE `shortcut_id` = 779 AND `class_index` = 3 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 3 ); 
 
UPDATE `character_shortcuts` SET `shortcut_id` = 782 WHERE `shortcut_id` = 779 AND `class_index` = 0 AND `charid` IN ( SELECT `charid` FROM `characters` WHERE `base_class` = 104 );
UPDATE `character_shortcuts` SET `shortcut_id` = 782 WHERE `shortcut_id` = 779 AND `class_index` = 1 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 1); 
UPDATE `character_shortcuts` SET `shortcut_id` = 782 WHERE `shortcut_id` = 779 AND `class_index` = 2 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 2); 
UPDATE `character_shortcuts` SET `shortcut_id` = 782 WHERE `shortcut_id` = 779 AND `class_index` = 3 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 3); 
 
UPDATE `character_shortcuts` SET `shortcut_id` = 780 WHERE `shortcut_id` = 779 AND `class_index` = 0 AND `charid` IN ( SELECT `charid` FROM `characters` WHERE `base_class` = 106 );
UPDATE `character_shortcuts` SET `shortcut_id` = 780 WHERE `shortcut_id` = 779 AND `class_index` = 1 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 1 );
UPDATE `character_shortcuts` SET `shortcut_id` = 780 WHERE `shortcut_id` = 779 AND `class_index` = 2 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 2 );
UPDATE `character_shortcuts` SET `shortcut_id` = 780 WHERE `shortcut_id` = 779 AND `class_index` = 3 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 3 );
 
UPDATE `character_shortcuts` SET `shortcut_id` = 783 WHERE `shortcut_id` = 779 AND `class_index` = 0 AND `charid` IN ( SELECT `charid` FROM `characters` WHERE `base_class` = 111 );
UPDATE `character_shortcuts` SET `shortcut_id` = 783 WHERE `shortcut_id` = 779 AND `class_index` = 1 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 1 );
UPDATE `character_shortcuts` SET `shortcut_id` = 783 WHERE `shortcut_id` = 779 AND `class_index` = 2 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 2 );
UPDATE `character_shortcuts` SET `shortcut_id` = 783 WHERE `shortcut_id` = 779 AND `class_index` = 3 AND `charid` IN ( SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 3 );