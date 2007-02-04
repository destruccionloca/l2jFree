-- ----------------------------
-- Table structure for gm auditioning
-- ----------------------------
CREATE TABLE IF NOT EXISTS GM_AUDIT (
  ID_ACTION_AUDIT INT(10) NOT NULL AUTO_INCREMENT,
  GM_NAME varchar(45) ,
  ACTION varchar(200) ,
  TARGET varchar(45) ,
  PARAM varchar(200) ,
  DATE date,
  PRIMARY KEY  (ID_ACTION_AUDIT)
);