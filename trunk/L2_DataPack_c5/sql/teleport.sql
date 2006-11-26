-- Table structure for table `teleport`
-- 
DROP TABLE IF EXISTS teleport;
CREATE TABLE teleport (
  Description varchar(75) default NULL,
  id decimal(11,0) NOT NULL default '0',
  loc_x decimal(9,0) default NULL,
  loc_y decimal(9,0) default NULL,
  loc_z decimal(9,0) default NULL,
  price decimal(6,0) default NULL,
  PRIMARY KEY  (id)
) TYPE=MyISAM;

-- 
-- Dumping data for table `teleport`
-- 

INSERT INTO teleport VALUES 
('Dark Elven Town -> Town of Gludio',1,-12672,122776,-3116,10000),
('Elven Town  -> Town of Gludio',2,-12672,122776,-3116,9200),
('Elven Village -> Elven Forest',468,21362,51122,-3688,710),
('Elven Village -> Neutral Zone',469,-10612,75881,-3592,740),
('Elven Village -> Elven Fortress',470,29294,74968,-3776,820),
('Town of gludio -> Elven village',3,46934,51467,-2977,9200),
('Town of gludio -> Dark Elven village',4,9745,15606,-4574,10000),
('Town of gludio -> Village of Gludin',5,-80826,149775,-3043,7300),
('Town of gludio -> Dion',6,15670,142983,-2705,3400),
('Village of Gludin -> Orc village',7,-44836,-112524,-235,26000),
('Village of Gludin -> Dwarven village',8,115113,-178212,-901,38000),
('Village of Gludin -> TI',9,-84318,244579,-3730,9400),
('Village of Gludin -> Elven Village',10,46934,51467,-2977,16000),
('Village of Gludin -> Dark Elven Village',11,9745,15606,-4574,16000),
('Village of Gludin -> Town of Gludio',12,-12672,122776,-3116,7300),
('Village of Gludin -> Southern entrance of wastelands',13,-16730,209417,-3664,3400),
('Dark Elven Town -> Southern part of dark elven forest',14,-61095,75104,-3356,1100),
('DE Village -> Dark Forest',464,-22224,14168,-3232,890),
('DE Village -> Spider Nest',465,-56532,78321,-2960,3600),
('DE Village -> Swampland',466,-30777,49750,-3552,1100),
('DE Village -> Neutral Zone',467,-23520,68688,-3640,1700),
('TI -> Village of Gludin',15,-80826,149775,-3043,18000),
('TI -> Obelisk of Victory',1001,-99678,237562,-3567,470),
('TI -> Western Territory',1002,-101294,212553,-3093,1000),
('TI -> Elven Ruins',1003,-113329,235327,-3653,830),
('TI -> Singing Waterfall',1004,-107456,242669,-3493,770),
('Dwarf Town  -> Town of Gludio',16,-12672,122776,-3116,32000),
('Dwarf Town -> The Northeast Coast',17,169008,-208272,-3504,2400),
('Dwarven Town -> Abandoned Coal Mines',418,155535,-173560,2495,690),
('Dwarven Town -> Mithril Mines',419,179039,-184080,-319,2200),
('Dion Town -> Town of Gludio',18,-12672,122776,-3116,3400),
('Dion Town -> Town of Giran',19,83400,147943,-3404,6800),
('Dion Town -> Giran Harbor',20,47942,186764,-3485,6500),
('Cruma Tower Entrance -> Cruma Tower 1st floor',21,17724,114004,-11672,0),
('Cruma Tower 1st floor -> Cruma Tower Entrance',22,17192,114178,-3439,0),
('Cruma Tower 1st floor -> Cruma Tower 2nd floor',23,17730,108301,-9057,0),
('Cruma Tower 2nd floor -> Cruma Tower 1st floor',24,17714,107923,-11850,0),
('Town of Giran -> Dion Town',25,15670,142983,-2705,6800),
('Town of Giran -> Oren Town',26,82956,53162,-1495,9400),
('Town of Giran -> Hunter Village',27,116819,76994,-2714,4100),
('Town of Giran -> Hardins Private Academy',28,105918,109759,-3207,4400),
('Talking Island -> Obelisk of Victory',460,-99843,237583,-3568,200),
('Talking Island -> Western Territory of Talking Island (Northern Area)',461,-102850,215932,-3424,3000),
('Talking Island -> Elven Ruins',462,49315,248452,-5960,2500),
('Talking Island -> Singing Waterfall',463,-113686,235723,-3640,2300),
('TI Dungeon inside -> outside',29,-113329,235327,-3653,0),
('TI Dungeon outside -> inside',30,48736,248463,-6162,0),
('IvoryTower Basement',31,84915,15969,-4294,0),
('IvoryTower Ground Floor',32,85399,16197,-3679,0),
('IvoryTower 1st Floor',33,85399,16197,-2809,0),
('IvoryTower 2nd Floor',34,85399,16197,-2293,0),
('IvoryTower 3th Floor',35,85399,16197,-1776,0),
('IvoryTower Ground Floor -> Oren Castle Town',36,82956,53162,-1495,4400),
('IvoryTower Ground Floor -> Hunters Village',37,116819,76994,-2714,8200),
('IvoryTower Ground Floor -> Aden Castle Town',38,146331,25762,-2018,12000),
('Aden Town -> Ivory Tower',39,85348,16142,-3699,12000),
('Aden Town -> Oren Town',40,82956,53162,-1495,6900),
('Aden Town -> Hunters Village',41,116819,76994,-2714,5900),
('Hunters Village -> Giran Town',42,83400,147943,-3404,9400),
('Hunters Village -> Oren Town',43,82956,53162,-1495,4100),
('Hunters Village -> Ivory Tower',44,85348,16142,-3699,8200),
('Hunters Village -> Hardins Private Academy',45,105918,109759,-3207,3400),
('Hunters Village -> Aden Town',46,146331,25762,-2018,5900),
('Oren Town -> Giran Town',47,83400,147943,-3404,9400),
('Oren Town -> Ivory Tower',48,85348,16142,-3699,3700),
('Oren Town -> Hunters Village',49,116819,76994,-2714,4100),
('Oren Town -> Hardins Private Academy',50,105918,109759,-3207,6100),
('Oren Town -> Aden Town',51,146331,25762,-2018,6900),
('Hardins Private Academy -> Giran Town',52,83400,147943,-3404,5300),
('Hardins Private Academy -> Oren Town',53,82956,53162,-1495,7300),
('Hardins Private Academy -> Hunters Village',54,116819,76994,-2714,4100),
('Cruma level 2 -> Cruma level 3',55,17719,115590,-6584,0),
('Cruma level 3 -> Cruma Core',56,17692,112284,-6250,0),
('Cruma core -> Cruma level 3',57,17719,115590,-6584,0),
('Cruma Tower 3rd floor -> Cruma Tower 2nd Floor',58,17731,119465,-9067,0),
('Heine -> The Town of Giran',59,83400,147943,-3404,7600),
('Heine -> Giran Harbor',60,47942,186764,-3485,7100),
('Lair end -> Antharas Nest',61,173826,115333,-7708,0),
('Antharas Nest - > Giran castle town',62,83400,147943,-3404,0),
('Giran Harbor -> Giran Town',63,83400,147943,-3404,6300),
('Giran Harbor -> Dion Town',64,15670,142983,-2705,6500),
('Giran Harbor -> Heine',107,111409,219364,-3545,8500),
('Heine -> The Town of Dion',65,15670,142983,-2705,12000),
('Heine -> Field of Silence',66,82684,183551,-3597,2500),
('Heine -> Field of Whispers',67,91186,217104,-3649,2300),
('Heine -> Entrance to Alligator Islands',68,126450,174774,-3079,2100),
('Giran -> Dragon Valley',69,122824,110836,-3720,1800),
('Giran -> Heine',70,111409,219364,-3545,7600),
('Giran -> Patriots Necropolis',71,-25472,77728,-3440,15500),
('Giran -> Ascetics Necropolis',72,-55385,78667,-3012,18600),
('Giran -> Saints Necropolis',73,79296,209584,-3704,9800),
('Giran -> Catacomb of Dark Omens',74,-23165,13827,-3172,20400),
('Monster Derby Track',75,12661,181687,-3560,0),
('Aden -> Coliseum',76,146440,46723,-3432,2000),
('Aden -> Patriots Necropolis',77,-25472,77728,-3440,35000),
('Aden -> Ascetics Necropolis',78,-55385,78667,-3012,41000),
('Aden -> Saints Necropolis',79,79296,209584,-3704,39000),
('Aden -> Catacomb of Dark Omens',80,-23165,13827,-3172,33000),
('Aden -> Blazing Swamp',81,159455,-12931,-2872,6800),
('Aden -> The Forbidden Gateway',82,185319,20218,-3264,1400),
('Aden -> The Front of Anghell Waterfall',83,163341,91374,-3320,2400),
('Aden -> Forsaken Plains',84,167285,37109,-4008,1900),
('Dion -> Heine',85,111409,219364,-3545,12000),
('Dion -> Partisan Hideaway',86,46467,126885,-3720,1700),
('Dion -> Bee Hive',87,20505,189036,-3344,2900),
('Gludio -> Windawood Manor',88,-23789,169683,-3424,1400),
('Gludio -> Southern Pathway to the Wasteland',89,-16730,209417,-3664,2400),
('Gludin -> Abandoned Camp',90,-46932,140883,-2936,1200),
('Gludin -> Fellmere Harvest Grounds',91,-70387,115501,-3472,1400),
('Gludin -> Langk Lizardman Dwelling',92,-45210,202654,-3592,1800),
('Orc Village -> Immortal Plateau,Northern Region',93,-8804,-114748,-3088,960),
('Orc Village -> Immortal Plateau,Southern Region',94,-17870,-90980,-2528,2000),
('Orc Village -> Immortal Plateau,Southeast Region',95,8209,-93524,-2312,750),
('Orc Village -> Frozen Waterfall',96,7603,-138871,-920,1600),
('Orc Village -> Entrance to the Cave of Trials',471,9340,-112509,-2536,1500),
('Oren -> Plains of Lizardmen',97,87252,85514,-3056,1900),
('Oren -> Sea of Spores',98,64328,26803,-3768,2500),
('Hunters -> Northern Pathway of Enchanted Valley',99,104426,33746,-3800,3600),
('Hunters -> Southern Pathway of Enchanted Valley',100,124904,61992,-3920,1300),
('Hunters -> Entrance to the Forest of Mirrors',101,142065,81300,-3000,2000),
('Hunters -> The Front of Anghel Waterfall',102,163341,91374,-3320,4000),
('Hunters -> Patriots Necropolis',103,-25472,77728,-3440,11900),
('Hunters -> Ascetics Necropolis',104,-55385,78667,-3012,14500),
('Hunters -> Saints Necropolis',105,79296,209584,-3704,11500),
('Hunters -> Catacomb of Dark Omens',106,-23165,13827,-3172,12800),
('Goddard -> Aden Castle Town',132,146331,25762,-2018,8100),
('Goddard -> Rune Castle Village',108,43799,-47727,-798,10000),
('Goddard -> Varka Silenos Stronghold',109,125543,-40953,-3724,4200),
('Goddard -> Ketra Orc Outpost',110,146954,-67390,-3660,1800),
('Goddard -> Entrance to the Forge of the Gods',111,169178,-116244,-2421,2300),
('Goddard -> Martyrs Necropolis',112,115358,132811,-3103,38000),
('Goddard -> Catacomb of the Witch',113,137480,79641,-3701,27000),
('Goddard -> Ascetics Necropolis',114,-55385,78667,-3012,48000),
('Goddard -> Catacomb of the Forbidden Path',115,110399,84041,-4813,29000),
('Goddard -> Saints Necropolis',116,79296,209584,-3704,54000),
('Goddard -> Catacomb of Dark Omens',117,-23165,13827,-3172,36000),
('Goddard -> Disciples Necropolis',118,168882,-18057,-3173,8500),
('Rune -> Goddard Castle Village',119,147928,-55273,-2734,10000),
('Rune -> The Town of Giran',120,83400,147943,-3404,59000),
('Rune -> Aden Castle Town',121,146331,25762,-2018,37000),
('Rune -> Rune Castle Town Guild',122,38320,-48092,-1153,150),
('Rune -> Rune Castle Town Temple',123,38275,-48065,896,150),
('Rune -> Entrance to the Forest of the Dead',124,52112,-53939,-3159,1200),
('Rune -> Western Entrance to the Swamp of Screams',125,70006,-49902,-3251,3000),
('Rune -> Catacomb of the Apostate',126,74379,78887,-3397,25000),
('Rune -> Patriots Necropolis',127,-25472,77728,-3440,28000),
('Rune -> Martyrs Necropolis',128,115358,132811,-3103,38000),
('Rune -> Catacomb of the Witch',129,137480,79641,-3701,31000),
('Rune -> Ascetics Necropolis',130,-55385,78667,-3012,32000),
('Rune -> Rune Castle Town Store',131,43799,-47727,-798,150),
('Aden -> Rune',134,43799,-47727,-798,37000),
('Aden -> Goddard',135,147928,-55273,-2734,8100),
('Giran Town -> Giran Harbor',136,47942,186764,-3485,5200),

('TOI - 1st Floor',201,115168,16022,-5100,100000),
('TOI - 2nd Floor',202,114649,18587,-3609,150000),
('TOI - 3rd Floor',203,117918,16039,-2127,200000),
('TOI - 4th Floor',204,114622,12946,-645,250000),
('TOI - 5th Floor',205,112209,16078,928,300000),
('TOI - 6th Floor',206,112376,16099,1947,350000),
('TOI - 7th Floor',207,114448,16175,2994,400000),
('TOI - 8th Floor',208,111063,16118,3967,450000),
('TOI - 9th Floor',209,117147,18415,4977,500000),
('TOI - 10th Floor',210,118374,15973,5987,550000),
('TOI - 11th Floor',211,112209,16078,7028,600000),
('TOI - 12th Floor',212,114809,18711,7996,650000),
('TOI - 13th Floor',213,115178,16989,9007,700000),
('TOI - 14th Floor Outside Door',214,112714,14111,10077,800000),
('TOI - 14th Floor Inside On Roof',215,113098,14532,10077,900000),

('Cat Heretics Entrance',250,43050,143933,-5383,0),
('Cat Heretics Exit',251,42514,143917,-5385,0),
('Cat Branded Entrance',252,46217,170290,-4983,0),
('Cat Branded Exit',253,45770,170299,-4985,0),
('Cat Apostate Entrance',254,78042,78404,-5128,0),
('Cat Apostate Exit',255,77225,78362,-5119,0),
('Cat Witch Entrance',256,140404,79678,-5431,0),
('Cat Witch Exit',257,139965,79678,-5433,0),
('Cat DarkOmen Entrance',258,-19500,13508,-4905,0),
('Cat DarkOmen Exit',259,-19931,13502,-4905,0),
('Cat ForbiddenPath Entrance',260,113865,84543,-6545,0),
('Cat ForbiddenPath Exit',261,113429,84540,-6545,0),
('Necro Sacrifice Entrance',262,-41570,209785,-5089,0),
('Necro Sacrifice Exit',263,-41567,209292,-5091,0),
('Necro Pilgrims Entrance',264,45251,123890,-5415,0),
('Necro Pilgrims Exit',265,45250,124366,-5417,0),
('Necro Worshippers Entrance',266,111273,174015,-5417,0),
('Necro Worshippers Exit',267,110818,174010,-5443,0),
('Necro Patriots Entrance',268,-21726,77385,-5177,0),
('Necro Patriots Exit',269,-22197,77369,-5177,0),
('Necro Ascetics Entrance',270,-52254,79103,-4743,0),
('Necro Ascetics Exit',271,-52716,79106,-4745,0),
('Necro Martyrs Entrance',272,118308,132800,-4833,0),
('Necro Martyrs Exit',273,117793,132810,-4835,0),
('Necro Saints Entrance',274,83000,209213,-5443,0),
('Necro Saints Exit',275,82608,209225,-5443,0),
('Necro Disciples Entrance',276,172251,-17605,-4903,0),
('Necro Disciples Exit',277,171902,-17595,-4905,0),

('Dion(artifact -> out)',350,22967,157715,-2954,0),
('Dion(artifact -> hall)',351,22090,159871,-2711,0),
('Dion(artifact -> outofcastle)',352,22448,155798,-2958,0),
('Dion(in -> artifact)',353,22592,161530,-2775,0),
('Dion(in -> out)',354,22967,157715,-2954,0),
('Dion(in -> outofcastle)',355,22448,155798,-2958,0),
('Dion(out -> artifact)',356,22592,161530,-2775,0),
('Dion(out -> hall)',357,22090,159871,-2711,0),
('Dion(out -> outofcastle)',358,22448,155798,-2958,0),
('Dion(outofcastle -> artifact)',359,22592,161530,-2775,0),
('Dion(outofcastle -> out)',360,22967,157715,-2954,0),
('Dion(outofcastle -> hall)',361,22090,159871,-2711,0),
('Giran(artifact -> out)',362,113892,144175,-2714,0),
('Giran(artifact -> hall)',363,115984,145073,-2584,0),
('Giran(artifact -> outofcastle)',364,112016,144682,-2833,0),
('Giran(in -> artifact)',365,117619,144564,-2648,0),
('Giran(in -> out)',366,113892,144175,-2714,0),
('Giran(in -> outofcastle)',367,112016,144682,-2833,0),
('Giran(out -> artifact)',368,117619,144564,-2648,0),
('Giran(out -> hall)',369,115984,145073,-2584,0),
('Giran(out -> outofcastle)',370,112016,144682,-2833,0),
('Giran(outofcastle -> artifact)',371,117619,144564,-2648,0),
('Giran(outofcastle -> out)',372,113892,144175,-2714,0),
('Giran(outofcastle -> hall)',373,115984,145073,-2584,0),
('Oren(artifact -> out)',374,79956,36351,-2532,0),
('Oren(artifact -> hall)',375,82113,37217,-2311,0),
('Oren(artifact -> outofcastle)',376,78079,36809,-2566,0),
('Oren(in -> artifact)',377,83710,36713,-2375,0),
('Oren(in -> out)',378,79956,36351,-2532,0),
('Oren(in -> outofcastle)',379,78079,36809,-2566,0),
('Oren(out -> artifact)',380,83710,36713,-2375,0),
('Oren(out -> hall)',381,82113,37217,-2311,0),
('Oren(out -> outofcastle)',382,78079,36809,-2566,0),
('Oren(outofcastle -> artifact)',383,83710,36713,-2375,0),
('Oren(outofcastle -> out)',384,79956,36351,-2532,0),
('Oren(outofcastle -> hall)',385,82113,37217,-2311,0),
('Gludio(artifact -> out)',386,-18941,112085,-2762,0),
('Gludio(artifact -> hall)',387,-18129,109898,-2517,0),
('Gludio(artifact -> outofcastle)',388,-18484,113897,-2772,0),
('Gludio(in -> artifact)',389,-18592,108271,-2581,0),
('Gludio(in -> out)',390,-18941,112085,-2762,0),
('Gludio(in -> outofcastle)',391,-18484,113897,-2772,0),
('Gludio(out -> artifact)',392,-18592,108271,-2581,0),
('Gludio(out -> hall)',393,-18129,109898,-2517,0),
('Gludio(out -> outofcastle)',394,-18484,113897,-2772,0),
('Gludio(outofcastle -> artifact)',395,-18592,108271,-2581,0),
('Gludio(outofcastle -> out)',396,-18941,112085,-2762,0),
('Gludio(outofcastle -> hall)',397,-18129,109898,-2517,0),
('Aden(artifact -> out)',398,147723,7916,-475,0),
('Aden(artifact -> in)',399,148580,4578,-408,0),
('Aden(artifact -> outofcastle)',400,147582,8753,-496,0),
('Aden(artifact -> hall)',401,147520,6107,-409,0),
('Aden(in -> artifact)',402,1147499,2544,-473,0),
('Aden(in -> out)',403,147723,7916,-475,0),
('Aden(in -> outofcastle)',404,147582,8753,-496,0),
('Aden(in -> hall)',405,147520,6107,-409,0),
('Aden(out -> artifact)',406,147499,2544,-473,0),
('Aden(out -> in)',407,148580,4578,-408,0),
('Aden(out -> outofcastle)',408,147582,8753,-496,0),
('Aden(out -> hall)',409,147520,6107,-409,0),
('Aden(outofcastle -> artifact)',410,147499,2544,-473,0),
('Aden(outofcastle -> out)',411,147723,7916,-475,0),
('Aden(outofcastle -> in)',412,148580,4578,-408,0),
('Aden(outofcastle -> hall)',413,147520,6107,-409,0),
('Aden(hall) -> artifact)',414,147499,2544,-473,0),
('Aden(hall) -> out)',415,147723,7916,-475,0),
('Aden(hall) -> in)',416,148580,4578,-408,0),
('Aden(hall) -> outofcastle)',417,147582,8753,-496,0),
('Lair Of Valakas',420,208884,-113705,-256,0),
('Innadril(artifact -> out)', 421, 116046, 247094, -930, 0),
('Innadril(artifact -> hall)', 422, 116028, 249163, -787, 0),
('Innadril(artifact -> outofcastle)', 423, 116373, 244729, -1059, 0),
('Innadril(hall -> artifact)', 424, 116028, 250136, -867, 0),
('Innadril(hall -> out)', 425, 116046, 247094, -930, 0),
('Innadril(hall -> outofcastle)', 426, 116373, 244729, -1059, 0),
('Innadril(out -> artifact)', 427, 116028, 250136, -867, 0),
('Innadril(out -> hall)', 428, 116028, 249163, -787, 0),
('Innadril(out -> outofcastle)', 429, 116373, 244729, -1059, 0),
('Innadril(outofcastle -> artifact)', 430, 116028, 250136, -867, 0),
('Innadril(outofcastle -> out)', 431, 116046, 247094, -930, 0),
('Innadril(outofcastle -> hall)', 432, 116028, 249163, -787, 0),
('Goddard(artifact -> out)', 433, 147466, -48514, -2085, 0),
('Goddard(artifact -> hall)', 434, 147463, -48504, -2277, 0),
('Goddard(artifact -> outofcastle)', 435, 147265, -44908, -2085, 0),
('Goddard(hall -> artifact)', 436, 146165, -50478, -1604, 0),
('Goddard(hall -> out)', 437, 147466, -48514, -2085, 0),
('Goddard(hall -> outofcastle)', 438, 147265, -44908, -2085, 0),
('Goddard(out -> artifact)', 439, 146165, -50478, -1604, 0),
('Goddard(out -> hall)', 440, 147463, -48504, -2277, 0),
('Goddard(out -> outofcastle)', 441, 147265, -44908, -2085, 0),
('Goddard(outofcastle -> artifact)', 442, 146165, -50478, -1604, 0),
('Goddard(outofcastle -> out)', 443, 147466, -48514, -2085, 0),
('Goddard(outofcastle -> hall)', 444, 147463, -48504, -2277, 0),
('Disciples Necropolis -> Anakim/Lilith',450,184397,-11957,-5493,0),
('Anakim/Lilith -> Disciples Necropolis',451,183225,-11911,-4897,0),
('TI -> Dark Elven Village',1005,9745,15606,-4574,24000),
('TI -> Dwarven Village',1006,115113,-178212,-901,46000),
('TI -> Elven Village',1007,46934,51467,-2977,23000),
('TI -> Orc Village',1008,-44836,-112524,-235,35000),
('DE Village -> Dwarven Village',1009,115113,-178212,-901,22000),
('DE Village -> TI Village',1010,-84318,244579,-3730,24000),
('DE Village -> Elven Village',1011,46934,51467,-2977,5100),
('DE Village -> Orc Village',1012,-44836,-112524,-235,13000),
('Elven Village -> Neutral Zone',1013,-23520,68688,-3640,1700),
('Elven Village -> Dwarven Village',1014,115113,-178212,-901,23000),
('Elven Village -> TI Village',1015,-84318,244579,-3730,23000),
('Elven Village -> Orc Village',1016,-44836,-112524,-235,18000),
('Dwarven Village -> DE Village',1017,9745,15606,-4574,22000),
('Dwarven Village -> Elven Village',1018,46934,51467,-2977,23000),
('Dwarven Village -> TI Village',1019,-84318,244579,-3730,46000),
('Dwarven Village -> Schuttgart',1020,87386,-143246,-1293,4400),
('Dwarven Village -> Orc Village',1021,-44836,-112524,-235,17000),
('Dwarven Village -> Central Shore',1022,136910,-205082,-3664,970),
('Orc Village -> Town of Gludio',1023,-12672,122776,-3116,23000),
('Orc Village -> Schuttgart',1024,87386,-143246,-1293,13000),
('Orc Village -> DE Village',1025,9745,15606,-4574,13000),
('Orc Village -> Dwarven Village',1026,115113,-178212,-901,17000),
('Orc Village -> TI Village',1027,-84318,244579,-3730,35000),
('Orc Village -> Elven Village',1028,46934,51467,-2977,18000),
('Gludio -> Orc Village',1033,-44836,-112524,-235,23000),
('Gludin Village -> Windmill Hill',1029,-68628,162336,-3592,550),
('Gludin Village -> Forgotten Temple',1030,-52841,190730,-3518,2000),
('Gludio -> Ant Cave',1031,-9993,176457,-4182,2100),
('Gludin Village -> Red Rock Ridge',1032,-42475,198055,-2864,3700),
('Gludio -> Dwarven Village',1034,115113,-178212,-901,32000),
('Gludio -> Schuttgart',1035,87386,-143246,-1293,85000),
('Gludio -> Heine',1036,111409,219364,-3545,47000),
('Gludio -> Aden Castle Town',1037,146331,25762,-2018,56000),
('Gludio -> Oren Castle Town',1038,82956,53162,-1495,35000),
('Gludio -> Goddard Castle Town',1039,147928,-55273,-2734,71000),
('Gludio -> Giran Castle Town',1040,83400,147943,-3404,29000),
('Gludio -> Rune Castle Town',1041,43799,-47727,-798,53000),
('Gludio -> Ruins of Agony',1042,-42504,120046,-3519,790),
('Gludio -> Ruins of Despair',1043,-20057,137618,-3897,610),
('Gludin -> Turek Orc',1044,-89839,105362,-3580,1800),
('Gludio -> Windmill Hill',1045,-68628,162336,-3592,2600),
('Dion -> Goddard',1046,147928,-55273,-2734,71000),
('Dion -> Rune Castle Town',1047,43799,-47727,-798,57000),
('Dion -> Schuttgart',1048,87386,-143246,-1293,88000),
('Dion -> Aden',1049,146331,25762,-2018,52000),
('Dion -> Oren',1050,82956,53162,-1495,33000),
('Dion -> Cruma Swamp',1051,13329,123601,-3688,760),
('Dion -> Cruma Tower',1052,17192,114178,-3439,2300),
('Dion -> Dion Plains',1053, 3030, 173367,-3056,1500),
('Dion -> Tanor Canyon',1054,51147,165543,-2829,3800),
('Giran -> Gludio',1055,-12672,122776,-3116,29000),
('Giran -> Goddard',1056,147928,-55273,-2734,63000),
('Giran -> Rune',1057,43799,-47727,-798,59000),
('Giran -> Schuttgart',1058,87386,-143246,-1293,87000),
('Giran -> Aden',1059,146331,25762,-2018,13000),
('Giran -> Antharas Lair',1060,132828,114421,-3725,7000),
('Giran -> Devils Island',1061,43101,204857,-3758,5700),
('Giran -> Brekas Stronghold',1062,79798,130624,-3677,1000),
('Heine -> Oren',1063,82956,53162,-1495,50000),
('Heine -> Aden',1064,146331,25762,-2018,59000),
('Heine -> Goddard',1065,147928,-55273,-2734,83000),
('Heine -> Rune',1066,43799,-47727,-798,82000),
('Heine -> Schuttgart',1067,87386,-143246,-1293,100000),
('Heine -> Gludio',1068,-12672,122776,-3116,47000),
('Heine -> Garden of Eva',1069,86006,231069,-3600,2400),
('Oren -> Rune',1070,43799,-47727,-798,10000),
('Oren -> Goddard',1071,147928,-55273,-2734,37000),
('Oren -> Heine',1072,111409,219364,-3545,50000),
('Oren -> Dion',1073,15670,142983,-2705,33000),
('Oren -> Schuttgart',1074,87386,-143246,-1293,59000),
('Oren -> Gludio',1075,-12672,122776,-3116,35000),
('Oren -> Skyshadow Meadow',1076,82764,61145,-3502,780),
('Oren -> Forest Outlaw',1077,85995,-2433,-3528,5200),
('Aden -> Giran',1078,83400,147943,-3404,13000),
('Aden -> Heine',1079,111409,219364,-3545,59000),
('Aden -> Schuttgart',1080,87386,-143246,-1293,53000),
('Aden -> Dion',1081,15670,142983,-2705,52000),
('Aden -> Gludio',1082,-12672,122776,-3116,56000),
('Aden -> Seal of Shilen',1083,188611,20588,-3696,3300),
('Aden -> Forest of Mirrors',1084,142065,81300,-3000,4400),
('Aden -> Fields of Massacre',1085,179718,-7843,-3517,6500),
('Aden -> Ancient Battleground',1086,126516,-7421,3912,5900),
('Aden -> Silent Valley',1087,177318,48447,-3835,6100),
('Aden -> ToI',1088,121291,15960,-4964,4200),
('Goddard -> Gludio',1089,-12672,122776,-3116,71000),
('Goddard -> Giran',1090,83400,147943,-3404,63000),
('Goddard -> Dion',1091,15670,142983,-2705,71000),
('Goddard -> Heine',1092,111409,219364,-3545,83000),
('Goddard -> Schuttgart',1093,87386,-143246,-1293,10000),
('Goddard -> Oren',1094,82956,53162,-1495,37000),
('Goddard -> Hot Springs',1095,149594,-112698,-2065,9300),
('Goddard -> Wall of Argos',1096,174062,-50141,-3390,2200),
('Goddard -> Monastery of silence',1097,123743,-75032,-2902,10000),
('Rune -> Dion',1098,15670,142983,-2705,57000),
('Rune -> Gludio',1099,-12672,122776,-3116,53000),
('Rune -> Heine',1100,111409,219364,-3545,82000),
('Rune -> Schuttgart',1101,87386,-143246,-1293,10000),
('Rune -> Oren',1102,82956,53162,-1495,10000),
('Rune -> Beast Farm',1103,57059,-82976,-2847,4800),
('Rune -> Valley of Saints',1104,67992,-72012,-3748,3800),
('Rune -> Monastery of Silence',1105,123743,-75032,-2902,14000),
('Schuttgart -> Rune',1106,43799,-47727,-798,10000),
('Schuttgart -> Goddard',1107,147928,-55273,-2734,10000),
('Schuttgart -> Aden',1108,146331,25762,-2018,53000),
('Schuttgart -> Oren',1109,82956,53162,-1495,59000),
('Schuttgart -> Heine',1110,111409,219364,-3545,100000),
('Schuttgart -> Giran',1111,83400,147943,-3404,87000),
('Schuttgart -> Dion',1112,15670,142983,-2705,88000),
('Schuttgart -> Gludio',1113,-12672,122776,-3116,85000),
('Schuttgart -> Orc Village',1114,-44836,-112524,-235,13000),
('Schuttgart -> Dwarven Village',1115,115113,-178212,-901,4400),
('Schuttgart -> Nest of Evil Spirit',1116,76860,-125169,-3414,3000),
('Schuttgart -> Wasteland of plunder',1117,109024,-159223,-1778,1600),
('Schuttgart -> Labyrinth of winter',1118,122162,-118893,-2603,3500),
('Schuttgart -> Graveyard of disgrace',1119,56095,-118952,-3290,1900),
('Schuttgart -> Farbel Ruins',1120,88288,-125692,-3816,2100);

-- ---------------------------------------------
-- Clan Hall teleports
-- ---------------------------------------------
INSERT INTO `teleport` VALUES ('Dion Territory', '501', '0', '0', '0', '500');
INSERT INTO `teleport` VALUES ('Execution Grounds', '502', '50568', '152408', '-2656', '500');
INSERT INTO `teleport` VALUES ('Fortress of Resistance', '503', '47261', '116866', '-2344', '500');
INSERT INTO `teleport` VALUES ('Cruma Marshlands', '504', '5106', '126916', '-3664', '500');
INSERT INTO `teleport` VALUES ('Cruma Tower', '505', '17225', '114173', '-3440', '500');
INSERT INTO `teleport` VALUES ('Mandragora Farm', '506', '38291', '148029', '-3696', '500');
INSERT INTO `teleport` VALUES ('Town of Dion', '507', '16856', '144673', '-3000', '500');
INSERT INTO `teleport` VALUES ('Floran Village', '508', '17308', '170368', '-3495', '500');
INSERT INTO `teleport` VALUES ('Tanor Canyon', '510', '59170', '164817', '-2856', '500');
INSERT INTO `teleport` VALUES ('Bee Hive', '511', '22944', '182122', '-2640', '500');
INSERT INTO `teleport` VALUES ('Dion Hills', '512', '29928', '151415', '2392', '500');
INSERT INTO `teleport` VALUES ('Floran Agricultural Area', '513', '10610', '156322', '-2472', '500');
INSERT INTO `teleport` VALUES ('Plains of Dion', '514', '630', '179184', '-3720', '500');
INSERT INTO `teleport` VALUES ('Fortress of Resistance', '515', '45302', '109537', '-2024', '500');
INSERT INTO `teleport` VALUES ('Arena', '629', '12443', '183467', '-3560', '500');
INSERT INTO `teleport` VALUES ('Giran Territory', '516', '0', '0', '0', '500');
INSERT INTO `teleport` VALUES ('Hardin\'s Academy', '517', '105918', '109759', '-3170', '500');
INSERT INTO `teleport` VALUES ('Dragon Valley', '518', '79745', '115299', '-3720', '500');
INSERT INTO `teleport` VALUES ('Death Pass', '521', '67933', '117045', '-3544', '500');
INSERT INTO `teleport` VALUES ('Pirate Tunnel', '522', '41528', '198358', '-4648', '500');
INSERT INTO `teleport` VALUES ('Giran Harbor', '524', '47938', '186864', '-3420', '500');
INSERT INTO `teleport` VALUES ('Giran Castle Town', '525', '83475', '147966', '-3404', '500');
INSERT INTO `teleport` VALUES ('Arena', '526', '73579', '142709', '-3768', '500');
INSERT INTO `teleport` VALUES ('Breka\'s Stronghold', '528', '85546', '131328', '-3672', '500');
INSERT INTO `teleport` VALUES ('Gorgon Flower Garden', '529', '113553', '134813', '-3540', '500');
INSERT INTO `teleport` VALUES ('Gludio Territory', '530', '0', '0', '0', '500');
INSERT INTO `teleport` VALUES ('Ruins of Despair', '531', '-19120', '136816', '-3762', '500');
INSERT INTO `teleport` VALUES ('Ruins of Agony', '532', '-42628', '119766', '-3528', '500');
INSERT INTO `teleport` VALUES ('Wasteland', '533', '-22726', '190368', '-4304', '500');
INSERT INTO `teleport` VALUES ('The Ant Nest', '534', '-9959', '176184', '-4160', '500');
INSERT INTO `teleport` VALUES ('Gludin Village', '535', '-80684', '149770', '-3043', '500');
INSERT INTO `teleport` VALUES ('Gludin Harbor', '536', '-91101', '150344', '-3624', '500');
INSERT INTO `teleport` VALUES ('Town of Gludio', '537', '-12787', '122779', '-3114', '500');
INSERT INTO `teleport` VALUES ('Abandoned Camp', '538', '-49853', '147089', '-2784', '500');
INSERT INTO `teleport` VALUES ('Orc Barracks', '539', '-89763', '105359', '-3576', '500');
INSERT INTO `teleport` VALUES ('Forgotten Temple', '540', '-53001', '191425', '-3568', '500');
INSERT INTO `teleport` VALUES ('Fellmere Lake', '541', '-57798', '127629', '-2928', '500');
INSERT INTO `teleport` VALUES ('Arena', '542', '-87328', '142266', '-3640', '500');
INSERT INTO `teleport` VALUES ('Windy Hill', '544', '-88539', '83389', '-2864', '500');
INSERT INTO `teleport` VALUES ('Red Rock Ridge', '545', '-44829', '188171', '-3256', '500');
INSERT INTO `teleport` VALUES ('Langk Lizardmen Dwellings', '546', '-44763', '203497', '-3592', '500');
INSERT INTO `teleport` VALUES ('Maille Lizardmen Barracks', '547', '-25283', '106820', '-3416', '500');
INSERT INTO `teleport` VALUES ('Talking Island', '548', '-84141', '244623', '-3729', '500');
INSERT INTO `teleport` VALUES ('Talking Island Village', '549', '-84141', '244623', '-3729', '500');
INSERT INTO `teleport` VALUES ('Cedric\'s Training Hall', '550', '-72674', '256819', '-3112', '500');
INSERT INTO `teleport` VALUES ('Einhovant\'s School of Magic', '551', '-89041', '248907', '-3568', '500');
INSERT INTO `teleport` VALUES ('Obelisk of Victory', '552', '-99586', '237637', '-3568', '500');
INSERT INTO `teleport` VALUES ('Elven Ruins', '553', '-112367', '234703', '-3688', '500');
INSERT INTO `teleport` VALUES ('Talking Island Harbor', '554', '-96811', '259153', '-3616', '500');
INSERT INTO `teleport` VALUES ('Talking Island, Western Territory', '555', '-95336', '240478', '-3264', '500');
INSERT INTO `teleport` VALUES ('Talking Island, Eastern Territory', '556', '-104344', '226217', '-3616', '500');
INSERT INTO `teleport` VALUES ('Fellmere Harvesting Grounds', '557', '-63736', '101522', '-3552', '500');
INSERT INTO `teleport` VALUES ('Windmill Hill', '558', '-72417', '173629', '-3648', '500');
INSERT INTO `teleport` VALUES ('Ruins of Agony Bend', '559', '-50174', '129303', '-2912', '500');
INSERT INTO `teleport` VALUES ('Evil Hunting Grounds', '560', '-6989', '109503', '-3040', '500');
INSERT INTO `teleport` VALUES ('Entrance to the Ruins of Despair', '561', '-36652', '135591', '-3160', '500');
INSERT INTO `teleport` VALUES ('Windawood Manor', '562', '-24794', '156502', '-2880', '500');
INSERT INTO `teleport` VALUES ('Ol Mahum Checkpoint', '563', '-6661', '201880', '-3632', '500');
INSERT INTO `teleport` VALUES ('Ant Incubator', '564', '-26489', '195307', '-3928', '500');
INSERT INTO `teleport` VALUES ('Singing Waterfall', '565', '-111728', '244330', '-3448', '500');
INSERT INTO `teleport` VALUES ('The Neutral Zone', '566', '-10612', '75881', '-3592', '500');
INSERT INTO `teleport` VALUES ('Oren Territory', '567', '0', '0', '0', '500');
INSERT INTO `teleport` VALUES ('Ivory Tower', '581', '85391', '16228', '-3640', '500');
INSERT INTO `teleport` VALUES ('Town of Oren', '582', '82971', '53207', '-1470', '500');
INSERT INTO `teleport` VALUES ('Plains of the Lizardmen', '584', '87252', '85514', '-3103', '500');
INSERT INTO `teleport` VALUES ('Skyshadow Meadow', '585', '89914', '46276', '-3616', '500');
INSERT INTO `teleport` VALUES ('Shilen\'s Garden', '586', '23863', '11068', '-3720', '500');
INSERT INTO `teleport` VALUES ('Black Rock Hill', '587', '-29466', '66678', '-3496', '500');
INSERT INTO `teleport` VALUES ('Spider Nest', '588', '-61095', '75104', '-3383', '500');
INSERT INTO `teleport` VALUES ('Timak Outpost', '589', '67097', '68815', '-3648', '500');
INSERT INTO `teleport` VALUES ('Ivory Tower Crater', '590', '85391', '16228', '-3640', '500');
INSERT INTO `teleport` VALUES ('Forest of Evil', '591', '93218', '16969', '-3904', '500');
INSERT INTO `teleport` VALUES ('Outlaw Forest', '592', '91539', '-12204', '-2440', '500');
INSERT INTO `teleport` VALUES ('Misty Mountains', '593', '61740', '94946', '-1488', '500');
INSERT INTO `teleport` VALUES ('Starlight Waterfall', '594', '58502', '53453', '-3624', '500');
INSERT INTO `teleport` VALUES ('Undine Waterfall', '595', '-7233', '57006', '-3520', '500');
INSERT INTO `teleport` VALUES ('The Gods\' Falls', '596', '70456', '6591', '-3632', '500');
INSERT INTO `teleport` VALUES ('Aden Territory', '597', '0', '0', '0', '500');
INSERT INTO `teleport` VALUES ('Tower of Insolence', '598', '114649', '11115', '-5100', '500');
INSERT INTO `teleport` VALUES ('Blazing Swamp', '599', '159455', '-12931', '-2872', '500');
INSERT INTO `teleport` VALUES ('The Forbidden Gateway', '601', '185319', '20218', '-3264', '500');
INSERT INTO `teleport` VALUES ('The Giant\'s Cave', '602', '181737', '46469', '-4276', '500');
INSERT INTO `teleport` VALUES ('The Enchanted Valley', '603', '124904', '61992', '-3973', '500');
INSERT INTO `teleport` VALUES ('The Cemetery', '604', '167047', '20304', '-3328', '500');
INSERT INTO `teleport` VALUES ('The Forest of Mirrors', '605', '142065', '81300', '-3000', '500');
INSERT INTO `teleport` VALUES ('Anghel Waterfall', '606', '166304', '91741', '-3168', '500');
INSERT INTO `teleport` VALUES ('Town of Aden', '607', '146783', '25808', '-2000', '500');
INSERT INTO `teleport` VALUES ('Hunters Village', '608', '117088', '76931', '-2670', '500');
INSERT INTO `teleport` VALUES ('Eastern Border Outpost', '609', '112405', '-16607', '-1864', '500');
INSERT INTO `teleport` VALUES ('Coliseum', '610', '146440', '46723', '-3400', '500');
INSERT INTO `teleport` VALUES ('Narsell Lake', '611', '146440', '46723', '-3400', '500');
INSERT INTO `teleport` VALUES ('Ancient Battleground', '613', '106517', '-2871', '-3454', '500');
INSERT INTO `teleport` VALUES ('Forsaken Plains', '614', '167285', '37109', '-4008', '500');
INSERT INTO `teleport` VALUES ('Silent Valley', '615', '170838', '55776', '-5280', '500');
INSERT INTO `teleport` VALUES ('Hunters Valley', '616', '114306', '86573', '-3112', '500');
INSERT INTO `teleport` VALUES ('Plains of Glory', '617', '135580', '19467', '-3424', '500');
INSERT INTO `teleport` VALUES ('Fields of Massacre', '618', '183543', '-14974', '-2768', '500');
INSERT INTO `teleport` VALUES ('War-Torn Plains', '619', '156898', '11217', '-4032', '500');
INSERT INTO `teleport` VALUES ('Western Border Outpost', '620', '158141', '-24543', '-1288', '500');
INSERT INTO `teleport` VALUES ('Innadril Territory', '621', '0', '0', '0', '500');
INSERT INTO `teleport` VALUES ('Field of Silence', '622', '91088', '182384', '-3192', '500');
INSERT INTO `teleport` VALUES ('Field of Whispers', '623', '74592', '207656', '-3032', '500');
INSERT INTO `teleport` VALUES ('Garden of Eva', '624', '84413', '234334', '-3656', '500');
INSERT INTO `teleport` VALUES ('Alligator Island', '625', '115583', '192261', '-3488', '500');
INSERT INTO `teleport` VALUES ('Heine', '626', '111455', '219400', '-3546', '500');
INSERT INTO `teleport` VALUES ('Alligator Beach', '628', '116267', '201177', '-3432', '500');
INSERT INTO `teleport` VALUES ('Cruise Liner Docks', '715', '111418', '225960', '-3624', '500');
INSERT INTO `teleport` VALUES ('Schuttgart Territory', '638', '0', '0', '0', '500');
INSERT INTO `teleport` VALUES ('Strip Mine', '639', '106561', '-173949', '-400', '500');
INSERT INTO `teleport` VALUES ('Dwarven Village', '640', '115120', '-178224', '-917', '500');
INSERT INTO `teleport` VALUES ('Spine Mountains', '641', '147493', '-200840', '192', '500');
INSERT INTO `teleport` VALUES ('Abandoned Coal Mines', '642', '139714', '-177456', '-1536', '500');
INSERT INTO `teleport` VALUES ('Mithril Mines', '643', '171946', '-173352', '3440', '500');
INSERT INTO `teleport` VALUES ('Frozen Valley', '644', '112971', '-174924', '-608', '500');
INSERT INTO `teleport` VALUES ('Western Mining Zone', '645', '128527', '-204036', '-3408', '500');
INSERT INTO `teleport` VALUES ('Eastern Mining Zone', '646', '175836', '-205837', '-3384', '500');
INSERT INTO `teleport` VALUES ('Mining Zone Passage', '647', '113826', '-171150', '-160', '500');
INSERT INTO `teleport` VALUES ('Plunderous Plains', '648', '115298', '-160886', '-1296', '500');
INSERT INTO `teleport` VALUES ('Frozen Labyrinth', '649', '123037', '-118112', '-2576', '500');
INSERT INTO `teleport` VALUES ('Pavel Ruins', '651', '91129', '-123951', '-4128', '500');
INSERT INTO `teleport` VALUES ('Caron\'s Dungeon', '652', '76021', '-110477', '-1704', '500');
INSERT INTO `teleport` VALUES ('Den of Evil', '653', '68693', '-110438', '-1946', '500');
INSERT INTO `teleport` VALUES ('Crypts of Disgrace', '654', '47692', '-115745', '-3744', '500');
INSERT INTO `teleport` VALUES ('Valley of the Lords', '655', '32173', '-122954', '-792', '500');
INSERT INTO `teleport` VALUES ('Town of Schuttgart', '657', '88249', '-142713', '-1336', '500');
INSERT INTO `teleport` VALUES ('Ruins of an Old Research Lab', '658', '90418', '-107317', '-3328', '500');
INSERT INTO `teleport` VALUES ('Frost Lake', '659', '108251', '-120886', '-3744', '500');
INSERT INTO `teleport` VALUES ('Sky Wagon Relic', '660', '121618', '-141554', '-1496', '500');
INSERT INTO `teleport` VALUES ('Ice Merchant\'s Hut', '716', '113750', '-109163', '-832', '500');
INSERT INTO `teleport` VALUES ('Brigand Stronghold', '717', '126272', '-159336', '-1232', '500');
INSERT INTO `teleport` VALUES ('Goddard Territory', '680', '0', '0', '0', '500');
INSERT INTO `teleport` VALUES ('Town  of Goddard', '681', '148024', '-55281', '-2728', '500');
INSERT INTO `teleport` VALUES ('Garden of Beasts', '683', '132997', '-60608', '-2960', '500');
INSERT INTO `teleport` VALUES ('Hot Springs', '684', '151778', '-106829', '-2888', '500');
INSERT INTO `teleport` VALUES ('Rainbow Springs Chateau', '685', '139997', '-124860', '-1896', '500');
INSERT INTO `teleport` VALUES ('Forge of the Gods', '686', '168902', '-116703', '-2417', '500');
INSERT INTO `teleport` VALUES ('Hall of Flames', '687', '189964', '-116820', '-1624', '500');
INSERT INTO `teleport` VALUES ('Valakas\' Lair', '688', '215378', '-116635', '-1608', '500');
INSERT INTO `teleport` VALUES ('Ketra Orc Outpost', '689', '146990', '-67128', '-3640', '500');
INSERT INTO `teleport` VALUES ('Ketra Orc Village', '690', '149548', '-82014', '-5592', '500');
INSERT INTO `teleport` VALUES ('Imperial Tomb', '691', '186699', '-75915', '-2826', '500');
INSERT INTO `teleport` VALUES ('Pilgrim\'s Temple', '692', '168982', '-86455', '-3007', '500');
INSERT INTO `teleport` VALUES ('Wall of Argos', '693', '165054', '-47861', '-3560', '500');
INSERT INTO `teleport` VALUES ('Shrine of Loyalty', '694', '190112', '-61776', '-2944', '500');
INSERT INTO `teleport` VALUES ('Varka Silenos Barracks', '695', '125740', '-40864', '-3736', '500');
INSERT INTO `teleport` VALUES ('Varka Silenos Village', '696', '108155', '-53670', '-2472', '500');
INSERT INTO `teleport` VALUES ('Four Sepulchers', '697', '178127', '-84435', '-7215', '500');
INSERT INTO `teleport` VALUES ('Devil\'s Pass', '698', '102621', '-60798', '-2144', '500');
INSERT INTO `teleport` VALUES ('The Last Imperial Tomb', '699', '174141', '-88685', '-5112', '500');
INSERT INTO `teleport` VALUES ('Rune Territory', '700', '0', '0', '0', '500');
INSERT INTO `teleport` VALUES ('Rune Territory=>Rune Township', '701', '43835', '-47749', '-792', '500');
INSERT INTO `teleport` VALUES ('Rune Territory=>Rune Harbor', '703', '36839', '-38435', '-3640', '500');
INSERT INTO `teleport` VALUES ('Rune Territory=>Windtail Waterfall', '704', '40723', '-94881', '-2096', '500');
INSERT INTO `teleport` VALUES ('Rune Territory=>Beast Farm', '705', '43805', '-88010', '-2780', '500');
INSERT INTO `teleport` VALUES ('Rune Territory=>Wild Beast Reserve', '706', '55133', '-93217', '-1360', '500');
INSERT INTO `teleport` VALUES ('Rune Territory=>Valley of Saints', '707', '79981', '-82301', '-3680', '500');
INSERT INTO `teleport` VALUES ('Rune Territory=>Forest of the Dead', '708', '52107', '-54328', '-3158', '500');
INSERT INTO `teleport` VALUES ('Rune Territory=>Cursed Village', '709', '57670', '-41672', '-3154', '500');
INSERT INTO `teleport` VALUES ('Rune Territory=>Fortress of the Dead', '710', '58028', '-24610', '-928', '500');
INSERT INTO `teleport` VALUES ('Rune Territory=>Swamp of Screams', '711', '69340', '-50203', '-3314', '500');
INSERT INTO `teleport` VALUES ('Rune Territory=>Monastery of Silence', '712', '125480', '-75834', '-2945', '500');
INSERT INTO `teleport` VALUES ('Rune Territory=>The Pagan Temple', '713', '35630', '-49748', '-760', '500');
INSERT INTO `teleport` VALUES ('Rune Territory=>Stakato Nest', '714', '90134', '-45130', '-2168', '500');
