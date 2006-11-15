--
-- Table structure for table `minions`
--

DROP TABLE IF EXISTS `minions`;
CREATE TABLE `minions` (
  `boss_id` int(11) NOT NULL default '0',
  `minion_id` int(11) NOT NULL default '0',
  `amount_min` int(4) NOT NULL default '0',
  `amount_max` int(4) NOT NULL default '0',
  PRIMARY KEY  (`boss_id`,`minion_id`)
) TYPE=MyISAM;

--
-- Dumping data for table `minions`
--

-- c1 mobs
INSERT INTO `minions` VALUES
(117,118,1,3),
(376,377,1,2),
(398,399,1,2),
(520,445,3,5),
(522,524,2,4),
(738,739,3,5),
(745,746,1,2), 
(747,748,1,2), 
(749,750,1,2), 
(751,752,3,3), 
(753,1040,4,4), 
(758,759,1,1), 
(758,760,1,1), 
(761,762,2,3), 
(763,764,1,1),
(763,765,1,1),
(763,766,1,1),
(767,768,1,1), 
(767,769,1,1), 
(767,770,1,1), 
(771,772,1,3), 
(773,774,2,4),
(779,750,1,3),
(930,928,1,1), 
(930,929,1,1), 
(933,931,1,1), 
(933,932,1,1), 
(935,934,1,3), 
(936,937,1,1),
(936,938,1,1),
(936,939,1,1),
(941,940,3,3),
(944,942,1,1),
(944,943,2,2),
(947,945,1,2), 
(947,946,1,2), 
(950,948,1,2), 
(950,949,1,2), 
(953,951,1,2), 
(953,952,1,2), 
(956,954,1,2), 
(956,955,1,2), 
(959,957,1,2), 
(959,958,1,2), 
(963,960,1,1), 
(963,961,1,1), 
(963,962,1,1), 
(966,964,1,2), 
(966,965,1,2), 
(969,967,1,2), 
(969,968,1,2), 
(973,970,1,1), 
(973,971,1,1), 
(973,972,1,1), 
(974,975,1,2), 
(974,976,1,2), 
(977,978,1,1), 
(977,979,1,1), 
(980,981,1,1), 
(980,982,1,1), 
(986,987,1,2), 
(986,988,1,2), 
(989,990,1,1), 
(991,992,1,2), 
(991,993,1,2), 
(994,995,3,4);

INSERT INTO `minions` VALUES 
(1058,1059,1,2), 
(1058,1060,1,2), 
(1075,1076,1,1),
(1075,1077,1,2), 
(1078,1079,1,1), 
(1078,1080,1,2), 
(1081,1082,1,1), 
(1081,1083,1,3),
(1312,1313,2,2),
(1090,1091,1,1),
(1090,1092,1,1),
(5022,367,1,3),
(5036,5037,2,3);

-- raid bosses
INSERT INTO `minions` VALUES 
(10001,10002,1,2),
(10001,10003,1,2),
(10004,10005,2,3),
(10004,10006,1,1),
(10007,10008,1,1),
(10007,10009,1,2),
(10010,10011,2,3),
(10010,10012,1,1),
(10013,10014,1,2),
(10013,10015,1,2),
(10016,10017,1,2),
(10016,10018,1,2),
(10217,10218,2,3),
(10217,10219,2,4),
(10020,10021,1,1),
(10020,10022,1,1),
(10023,10024,3,5),
(10023,10025,4,5),
(10026,10027,1,2),
(10026,10028,2,4),
(10029,10030,1,2),
(10029,10031,1,3),
(10032,10033,1,2),
(10032,10034,4,6),
(10035,10036,1,2),
(10035,10037,2,3),
(10038,10039,1,1),
(10038,10040,2,4),
(10041,10042,2,3),
(10041,10043,1,1),
(10044,10045,2,4),
(10044,10046,1,1),
(10047,10048,1,2),
(10047,10049,1,2),
(10051,10052,1,1),
(10051,10053,1,1),
(10054,10055,1,1),
(10054,10056,1,1),
(10057,10058,1,2),
(10057,10059,2,3),
(10060,10061,1,2),
(10060,10062,1,2),
(10064,10065,1,2),
(10064,10066,1,2),
(10067,10068,1,2),
(10067,10069,1,2),
(10070,10071,1,2),
(10073,10074,1,2),
(10076,10077,1,2),
(10079,10080,1,2),
(10070,10072,1,2),
(10073,10075,1,2),
(10076,10078,1,2),
(10079,10081,1,2),
(10082,10083,1,2),
(10082,10084,1,2),
(10085,10086,1,2),
(10085,10087,1,2),
(10089,10091,1,2),
(10089,10090,1,2),
(10092,10093,1,2),
(10092,10094,1,2),
(10095,10096,1,2),
(10095,10097,1,2),
(10099,10100,1,2),
(10099,10101,1,2),
(10103,10104,1,2),
(10103,10105,1,2),
(10106,10107,1,2),
(10106,10108,1,2),
(10109,10110,1,2),
(10109,10111,1,2),
(10112,10113,1,2),
(10112,10114,1,2),
(10115,10116,1,2),
(10115,10117,1,2),
(10119,10120,1,2),
(10119,10121,1,2),
(10122,10123,1,2),
(10122,10124,1,2),
(10128,10129,1,2),
(10128,10130,1,2),
(10131,10132,1,2),
(10131,10133,1,2),
(10134,10135,1,2),
(10134,10136,1,2),
(10137,10138,1,2),
(10137,10139,1,2),
(10140,10141,1,2),
(10140,10142,1,2),
(10143,10144,1,2),
(10143,10145,1,2),
(10146,10147,1,2),
(10146,10148,1,2),
(10149,10150,1,2),
(10149,10151,1,2),
(10152,10153,1,2),
(10152,10154,1,2),
(10155,10156,1,2),
(10155,10157,1,2),
(10159,10160,1,2),
(10159,10161,1,2),
(10163,10164,1,2),
(10163,10165,1,2),
(10166,10167,1,2),
(10166,10168,1,2),
(10170,10171,1,2),
(10170,10172,1,2),
(10173,10174,1,2),
(10173,10175,1,2),
(10176,10177,1,2),
(10176,10178,1,2),
(10179,10180,1,2),
(10179,10181,1,2),
(10182,10183,1,2),
(10182,10184,1,2),
(10185,10186,1,2),
(10185,10187,1,2),
(10189,10190,1,2),
(10189,10191,1,2),
(10192,10193,1,2),
(10192,10194,1,2),
(10199,10200,1,2),
(10199,10201,1,2),
(10202,10203,1,2),
(10202,10204,1,2),
(10205,10206,1,2),
(10205,10207,1,2),
(10208,10209,1,2),
(10208,10210,1,2),
(10211,10212,1,2),
(10211,10213,1,2),
(10214,10215,1,2),
(10214,10216,1,2),
(10220,10221,1,2),
(10220,10222,1,2),
(10223,10224,2,3),
(10223,10225,1,1),
(10226,10227,1,2),
(10226,10228,1,2),
(10230,10231,1,1),
(10230,10232,2,4),
(10235,10236,1,2),
(10235,10237,1,2),
(10238,10239,1,2),
(10238,10240,1,2),
(10241,10242,1,1),
(10241,10243,1,3),
(10245,10246,1,1),
(10245,10247,1,4),
(10249,10250,2,3),
(10249,10251,1,1),
(10252,10253,1,3),
(10252,10254,1,1),
(10256,10257,2,3),
(10256,10258,1,2),
(10260,10261,1,2),
(10260,10262,1,3),
(10263,10264,1,1),
(10263,10265,1,3),
(10266,10267,1,1),
(10266,10268,2,4),
(10269,10270,1,3),
(10269,10271,1,2),
(10283,10284,1,3),
(10283,10285,1,3),
(10286,10287,1,2),
(10286,10288,1,2),
(10286,10289,1,2),
(10290,10291,1,2),
(10290,10292,2,4),
(10293,10294,1,2),
(10293,10295,2,3),
(10296,10297,2,4),
(10296,10298,1,3),
(10299,10300,1,2),
(10299,10301,2,4),
(10302,10303,1,3),
(10302,10304,1,3),
(10306,10307,1,2),
(10306,10308,2,4),
(10309,10310,1,3),
(10309,10311,1,3),
(10312,10313,1,2),
(10312,10314,2,4),
(10316,10317,1,2),
(10316,10318,2,4),
(10319,10320,2,3),
(10319,10321,1,2),
(10322,10323,2,3),
(10322,10324,1,3),
(10325,10326,1,2),
(10325,10327,2,3),
(10328,10329,1,1),
(10328,10330,1,1),
(10328,10331,1,1),
(10328,10332,1,1),
(10339,10340,1,1),
(10339,10341,1,1),
(10342,10343,1,1),
(10342,10344,1,1),
(10342,10345,1,1),
(10346,10347,1,1),
(10346,10348,1,1),
(10349,10350,1,1),
(10349,10351,1,1),
(10352,10353,3,6),
(10354,10355,2,4),
(10354,10356,2,4),
(10357,10358,1,3),
(10357,10359,1,3),
(10360,10361,4,6),
(10362,10363,2,4),
(10362,10364,1,3),
(10366,10367,2,4),
(10366,10368,1,2),
(10369,10370,1,3),
(10369,10371,1,3),
(10373,10374,2,4),
(10375,10376,2,4),
(10375,10377,1,3),
(10378,10379,1,4),
(10380,10381,1,3),
(10380,10382,1,3),
(10383,10384,3,5),
(10385,10386,1,3),
(10385,10387,1,3),
(10388,10389,1,3),
(10388,10390,2,5),
(10392,10393,4,7),
(10395,10396,1,1),
(10395,10397,2,2),
(10398,10399,1,4),
(10398,10400,1,2),
(10401,10402,2,3),
(10401,10403,3,5),
(10404,10405,2,4),
(10404,10406,2,4),
(10407,10408,2,3),
(10407,10409,2,4),
(10410,10411,4,8),
(10412,10413,1,2),
(10412,10414,1,4),
(10415,10416,1,3),
(10415,10417,1,3),
(10418,10419,2,5),
(10420,10421,2,5),
(10420,10422,2,3),
(10423,10424,6,8),
(10423,10425,3,4),
(10426,10427,2,3),
(10426,10428,2,4),
(10429,10430,6,9),
(10431,10432,3,4),
(10431,10433,3,4),
(10434,10435,3,4),
(10434,10436,2,3),
(10438,10439,3,5),
(10438,10440,2,4),
(10441,10442,2,5),
(10441,10443,2,4),
(10444,10445,3,5),
(10444,10446,2,4),
(10447,10448,2,4),
(10447,10449,3,5),
(10450,10451,2,3),
(10450,10452,2,5),
(10453,10454,3,5),
(10453,10455,2,4),
(10456,10457,3,4),
(10456,10458,3,4),
(10456,10459,3,4),
(10460,10461,3,5),
(10460,10462,3,5),
(10463,10464,2,4),
(10463,10465,2,3),
(10463,10466,3,5),
(10467,10468,3,5),
(10467,10469,2,4),
(10470,10471,3,4),
(10470,10472,3,5),
(10473,10474,6,8),
(10475,10476,3,4),
(10475,10477,2,3),
(10478,10479,3,5),
(10478,10480,2,4),
(10481,10482,2,4),
(10481,10483,2,4),
(10484,10485,3,5),
(10484,10486,2,4),
(10487,10488,2,3),
(10487,10489,1,4),
(10490,10491,2,3),
(10490,10492,1,4),
(10493,10494,3,4),
(10493,10495,3,4),
(10496,10497,5,9),
(10498,10499,2,4),
(10498,10500,1,5),
(12001,12003,5,8),
(12001,12004,6,9),
(12169,12170,6,8),
(12169,12171,4,7),
(12169,12172,6,8),
(12169,12173,4,7); 
