-- ---------------------------
-- Table structure for `castle_doorupgrade`
-- ---------------------------
CREATE TABLE IF NOT EXISTS `castle_doorupgrade` (
  `doorId` INT NOT NULL DEFAULT 0,
  `hp` INT NOT NULL DEFAULT 0,
  `pDef` INT NOT NULL DEFAULT 0,
  `mDef` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`doorId`)
) DEFAULT CHARSET=utf8;