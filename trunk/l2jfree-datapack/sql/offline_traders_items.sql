-- Table to keep items of offline traders
-- author hex1r0
DROP TABLE IF EXISTS `offline_traders_items`;
CREATE TABLE `offline_traders_items` (
  `char_id` INT NOT NULL ,
  `item_obj_id` INT NOT NULL ,
  `count` BIGINT NOT NULL ,
  `price` BIGINT NOT NULL
)DEFAULT CHARSET=utf8;