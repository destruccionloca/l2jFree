ALTER TABLE teleport ADD COLUMN fornoble INT(1) NOT NULL DEFAULT 0 AFTER price;
ALTER TABLE `character_skills` CHANGE `skill_name` `skill_name` varchar(35);
ALTER TABLE character_skills CHANGE skill_name skill_name varchar(35);