-- -----------------------------------
-- Table structure for item_attributes
-- -----------------------------------
CREATE TABLE IF NOT EXISTS item_attributes (
  itemId int(11) NOT NULL DEFAULT 0,
  augAttributes int(11) NOT NULL DEFAULT -1,
  augSkillId int(11) NOT NULL DEFAULT -1,
  augSkillLevel int(11) NOT NULL DEFAULT -1,
  elemType tinyint(1) NOT NULL DEFAULT -1,
  elemValue int(11) NOT NULL DEFAULT -1,
  PRIMARY KEY (itemId)
) DEFAULT CHARSET=utf8;