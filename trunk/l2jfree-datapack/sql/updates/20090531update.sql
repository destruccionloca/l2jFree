ALTER TABLE `custom_etcitem` 
DROP `html`,
ADD `handler` VARCHAR(70) NOT NULL DEFAULT 'none' AFTER `tradeable`;