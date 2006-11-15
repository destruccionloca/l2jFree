ALTER TABLE raidboss_spawnlist ADD COLUMN currentHp decimal(8,0) default NULL;
ALTER TABLE raidboss_spawnlist ADD COLUMN currentMp decimal(4,0) default NULL; 
UPDATE npc SET type = 'L2Adventurer' WHERE id BETWEEN 8729 AND 8738;