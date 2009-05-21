
-- Be carefull, this patch should not be applied several times !!!!
-- check in the table version that select max(dpversion) from version is < 22. If it is greater or equal than 22, this patch was already made


CREATE TABLE IF NOT EXISTS `version` (
`dbVersion` INT NOT NULL
) TYPE = MYISAM ;

insert INTO `version` ( `dbVersion` )
VALUES ('22');


CREATE TABLE IF NOT EXISTS `topic_tmp` (
  `topic_id` int(8) NOT NULL default '0',
  `topic_forum_id` int(8) NOT NULL default '0',
  `topic_name` varchar(255) NOT NULL default '',
  `topic_date` decimal(20,0) NOT NULL default '0',
  `topic_ownername` varchar(255) NOT NULL default '0',
  `topic_ownerid` int(8) NOT NULL default '0',
  `topic_type` int(8) NOT NULL default '0',
  `topic_reply` int(8) NOT NULL default '0'
) DEFAULT CHARSET=utf8;

insert into topic_tmp 
select * from topic;

truncate table topic;

-- add primary key with auto increment property
ALTER TABLE `topic` ADD PRIMARY KEY ( `topic_id` ) ;
ALTER TABLE `topic` CHANGE `topic_id` `topic_id` INT( 8 ) NOT NULL  AUTO_INCREMENT  ;
ALTER TABLE `topic` ADD INDEX ( `topic_forum_id` ) ;

ALTER TABLE `forums` ADD INDEX ( `forum_name` ( 10 ) ) ;
ALTER TABLE `forums` ADD INDEX ( `forum_parent` ) ;
ALTER TABLE `forums` ADD INDEX ( `forum_type` ) ;

insert into topic (topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply)
select topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply from topic_tmp;

drop table topic_tmp;

-- drop auto increment properties (used for migration)
ALTER TABLE `topic` CHANGE `topic_id` `topic_id` INT( 8 ) NOT NULL ;

CREATE TABLE IF NOT EXISTS `posts_tmp` (
  `post_id` int(8) NOT NULL default '0',
  `post_owner_name` varchar(255) NOT NULL default '',
  `post_ownerid` int(8) NOT NULL default '0',
  `post_date` decimal(20,0) NOT NULL default '0',
  `post_topic_id` int(8) NOT NULL default '0',
  `post_forum_id` int(8) NOT NULL default '0',
  `post_txt` text NOT NULL
) DEFAULT CHARSET=utf8;

insert into posts_tmp 
select * from posts;

truncate table posts;

ALTER TABLE `posts` ADD PRIMARY KEY ( `post_id` ) ;
ALTER TABLE `posts` CHANGE `post_id` `post_id` INT( 8 ) NOT NULL  AUTO_INCREMENT  ;
ALTER TABLE `posts` DROP `post_forum_id` ;
ALTER TABLE `posts` ADD INDEX ( `post_topic_id` ) ;

insert into posts (post_owner_name,post_ownerid,post_date,post_topic_id,post_txt)
select post_owner_name,post_ownerid,post_date,post_topic_id,post_txt from posts_tmp;

drop table posts_tmp;

-- drop auto increment properties (used for migration)
ALTER TABLE `posts` CHANGE `post_id` `post_id` INT( 8 ) NOT NULL ;
