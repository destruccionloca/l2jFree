-- fix agrro on npc.sql raidboss minions and raidbooss for l2jfree 

-- raidboss minion 
update npc set aggro = 0 where id in(10218,10219,12003,10002,10003,10005,10006,10008,10009);
update npc set aggro = 0 where id in(10011,10012,10014,10015,10017,10018,10021,10022,10024);
update npc set aggro = 0 where id in(10025,10027,10028,10030,10031,10033,10034,10036,10037);
update npc set aggro = 0 where id in(10039,10040,10042,10043,10045,10046,10048,10049,10052);
update npc set aggro = 0 where id in(10053,10055,10056,10058,10059,10061,10062,10065,10066);
update npc set aggro = 0 where id in(10068,10069,10071,10074,10077,10080,10083,10086,10072);
update npc set aggro = 0 where id in(10068,10069,10071,10074,10077,10080,10083,10086,10072);
update npc set aggro = 0 where id in(10075,10078,10081,10084,10087,10094,10093,10091,10090);
update npc set aggro = 0 where id in(10096,10097,10100,10101,10104,10105,10107,10108,10110);
update npc set aggro = 0 where id in(10111,10113,10114,10116,10117,10120,10121,10123,10124);
update npc set aggro = 0 where id in(10129,10130,10132,10133,10135,10136,10138,10139,10141);
update npc set aggro = 0 where id in(10142,10144,10145,10147,10148,10150,10151,10153,10154);
update npc set aggro = 0 where id in(10156,10157,10160,10161,10164,10165,10167,10168,10171);
update npc set aggro = 0 where id in(10172,10174,10175,10177,10178,10180,10181,10183,10184);
update npc set aggro = 0 where id in(10186,10187,10190,10191,10193,10194,10200,10201,10203);
update npc set aggro = 0 where id in(10204,10206,10207,10209,10210,10212,10213,10215,10216);
update npc set aggro = 0 where id in(10221,10222,10224,10225,10227,10228,10231,10232,10236);
update npc set aggro = 0 where id in(10237,10239,10240,10242,10243,10246,10247,10250,10251);
update npc set aggro = 0 where id in(10253,10254,10257,10258,10261,10262,10264,10265,10267);
update npc set aggro = 0 where id in(10268,10270,10271,10284,10285,10287,10288,10289);
-- raidbooss
update npc set aggro = 0 where id in(10019,10189,10057,10179,10182,10143);
-- valakas
update npc set aggro = 500 where id in(12899);
-- baium
update npc set patk = 9182 where id in(12372);
update npc set pdef = 6214 where id in(12372);
update npc set matk = 9211 where id in(12372);
update npc set mdef = 4191 where id in(12372);
update npc set matkspd = 380 where id in(12372);
update npc set atkspd = 380 where id in(12372);
update npc set runspd = 130 where id in(12372);
-- mobs baium lair
update npc set hp = 22153 where id in(12373);
-- elder lavasaurous
update npc set runspd = 40 where id in(1395);
update npc set wit=35;
-- gk for oll toi .. 

DELETE FROM `spawnlist` WHERE (`npc_templateid` = 12563);
-- npc toi crystal teleport 
INSERT INTO `spawnlist` (`location`, `count`, `npc_templateid`, `locx`, `locy`, `locz`, `randomx`, `randomy`, `heading`, `respawn_delay`, `loc_id`) VALUES
('toi_npc_1o' ,  1, 7952, 110859, 15995, -4404,0, 0, 0, 60, 0), 
('toi_npc_5o' ,  1, 7953, 114089, 19970, 909,  0, 0, 0, 60, 0), 
('toi_npc_10o',  1, 7954, 118593, 16675, 5961, 0, 0, 0, 60, 0); 

INSERT INTO merchant_buylists VALUES
(4577,50000,9,110),
(4578,50000,9,111),
(4579,50000,9,112),
(4580,50000,9,113),
(4581,50000,9,114),
(4582,50000,9,115),
(4583,50000,9,116),
(4584,50000,9,117),
(4585,50000,9,118),
(4586,50000,9,119),
(4587,50000,9,120),
(4588,50000,9,121);