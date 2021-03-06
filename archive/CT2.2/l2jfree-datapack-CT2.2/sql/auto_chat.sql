-- ---------------------------
-- Table structure for `auto_chat`
-- ---------------------------
DROP TABLE IF EXISTS `auto_chat`;
CREATE TABLE `auto_chat` (
  `groupId` INT UNSIGNED NOT NULL DEFAULT 0,
  `groupName` VARCHAR(128) NOT NULL DEFAULT "",
  `npcId` MEDIUMINT UNSIGNED NOT NULL,
  `chatDelay` BIGINT NOT NULL DEFAULT -1,
  `chatRange` MEDIUMINT(6) NOT NULL DEFAULT -1,
  `chatRandom` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`groupId`)
) DEFAULT CHARSET=utf8;

INSERT INTO `auto_chat` VALUES 
(1,"Preacher of Doom",31093,-1,-1,0),
(2,"Preacher of Doom",31172,-1,-1,0),
(3,"Preacher of Doom",31174,-1,-1,0),
(4,"Preacher of Doom",31176,-1,-1,0),
(5,"Preacher of Doom",31178,-1,-1,0),
(6,"Preacher of Doom",31180,-1,-1,0),
(7,"Preacher of Doom",31182,-1,-1,0),
(8,"Preacher of Doom",31184,-1,-1,0),
(9,"Preacher of Doom",31186,-1,-1,0),
(10,"Preacher of Doom",31188,-1,-1,0),
(11,"Preacher of Doom",31190,-1,-1,0),
(12,"Preacher of Doom",31192,-1,-1,0),
(13,"Preacher of Doom",31194,-1,-1,0),
(14,"Preacher of Doom",31196,-1,-1,0),
(15,"Preacher of Doom",31198,-1,-1,0),
(16,"Preacher of Doom",31200,-1,-1,0),

(17,"Orator of Revelations",31094,-1,-1,0),
(18,"Orator of Revelations",31173,-1,-1,0),
(19,"Orator of Revelations",31175,-1,-1,0),
(20,"Orator of Revelations",31177,-1,-1,0),
(21,"Orator of Revelations",31179,-1,-1,0),
(22,"Orator of Revelations",31181,-1,-1,0),
(23,"Orator of Revelations",31183,-1,-1,0),
(24,"Orator of Revelations",31185,-1,-1,0),
(25,"Orator of Revelations",31187,-1,-1,0),
(26,"Orator of Revelations",31189,-1,-1,0),
(27,"Orator of Revelations",31191,-1,-1,0),
(28,"Orator of Revelations",31193,-1,-1,0),
(29,"Orator of Revelations",31195,-1,-1,0),
(30,"Orator of Revelations",31197,-1,-1,0),
(31,"Orator of Revelations",31199,-1,-1,0),
(32,"Orator of Revelations",31201,-1,-1,0);
