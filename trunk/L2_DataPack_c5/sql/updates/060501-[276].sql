-- Add column class_index to character_subclasses - Yesod
ALTER TABLE character_subclasses ADD COLUMN class_index INT(1) NOT NULL DEFAULT 0 AFTER level; 