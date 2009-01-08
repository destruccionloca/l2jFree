-- UPDATE SMART CUBIC SKILLS
UPDATE IGNORE `character_skills` SET `skill_id` = 781 WHERE `skill_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 96);
UPDATE IGNORE `character_skills` SET `skill_id` = 781 WHERE `skill_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 1);
UPDATE IGNORE `character_skills` SET `skill_id` = 781 WHERE `skill_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 2);
UPDATE IGNORE `character_skills` SET `skill_id` = 781 WHERE `skill_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 3);
 
UPDATE IGNORE `character_skills` SET `skill_id` = 782 WHERE `skill_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 104);
UPDATE IGNORE `character_skills` SET `skill_id` = 782 WHERE `skill_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 1);
UPDATE IGNORE `character_skills` SET `skill_id` = 782 WHERE `skill_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 2);
UPDATE IGNORE `character_skills` SET `skill_id` = 782 WHERE `skill_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 3);
 
UPDATE IGNORE `character_skills` SET `skill_id` = 780 WHERE `skill_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 106);
UPDATE IGNORE `character_skills` SET `skill_id` = 780 WHERE `skill_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 1);
UPDATE IGNORE `character_skills` SET `skill_id` = 780 WHERE `skill_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 2);
UPDATE IGNORE `character_skills` SET `skill_id` = 780 WHERE `skill_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 3);
 
UPDATE IGNORE `character_skills` SET `skill_id` = 783 WHERE `skill_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 111);
UPDATE IGNORE `character_skills` SET `skill_id` = 783 WHERE `skill_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 1);
UPDATE IGNORE `character_skills` SET `skill_id` = 783 WHERE `skill_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 2);
UPDATE IGNORE `character_skills` SET `skill_id` = 783 WHERE `skill_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 3);

-- UPDATE SMART CUBIC SHORTCUTS
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 781 WHERE `shortcut_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 96);
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 781 WHERE `shortcut_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 1);
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 781 WHERE `shortcut_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 2);
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 781 WHERE `shortcut_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 3);
 
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 782 WHERE `shortcut_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 104);
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 782 WHERE `shortcut_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 1);
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 782 WHERE `shortcut_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 2);
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 782 WHERE `shortcut_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 3);
 
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 780 WHERE `shortcut_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 106);
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 780 WHERE `shortcut_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 1);
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 780 WHERE `shortcut_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 2);
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 780 WHERE `shortcut_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 3);
 
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 783 WHERE `shortcut_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 111);
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 783 WHERE `shortcut_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 1);
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 783 WHERE `shortcut_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 2);
UPDATE IGNORE `character_shortcuts` SET `shortcut_id` = 783 WHERE `shortcut_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 3);

-- DELETE INCORRECT SMART CUBIC SKILLS
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 96);
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 1);
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 2);
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 3);
 
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 104);
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 1);
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 2);
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 3);
 
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 106);
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 1);
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 2);
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 3);
 
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 111);
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 1);
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 2);
DELETE FROM `character_skills` WHERE `skill_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 3);

-- DELETE INCORRECT SMART CUBIC SHORTCUTS
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 96);
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 1);
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 2);
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 96 AND `class_index` = 3);
 
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 104);
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 1);
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 2);
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 104 AND `class_index` = 3);
 
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 106);
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 1);
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 2);
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 106 AND `class_index` = 3);
 
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 0 AND `charid` IN (SELECT `charid` FROM `characters` WHERE `base_class` = 111);
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 1 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 1);
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 2 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 2);
DELETE FROM `character_shortcuts` WHERE `shortcut_id` = 779 AND `class_index` = 3 AND `charid` IN (SELECT `charid` FROM `character_subclasses` WHERE `class_id` = 111 AND `class_index` = 3);