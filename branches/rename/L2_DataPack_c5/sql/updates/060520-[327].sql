UPDATE npc SET type = 'L2PenaltyMonster' WHERE id BETWEEN 13245 AND 13252;
UPDATE etcitem SET item_type = 'lure' WHERE name LIKE '%lure%' AND name NOT LIKE '%chest%';
UPDATE weapon SET soulshots=1,spiritshots=1 WHERE weaponType='pet';
ALTER TABLE character_subclasses ADD COLUMN class_index INT(1) NOT NULL DEFAULT 0 AFTER level;
