DROP TABLE IF EXISTS `armorsets`;
CREATE TABLE `armorsets` (
 `id` SMALLINT UNSIGNED NOT NULL auto_increment,
 `chest` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 `legs` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 `head` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 `gloves` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 `feet` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 `skill` VARCHAR(70) NOT NULL DEFAULT '0-0;',
 `shield` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 `shield_skill_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 `enchant6skill` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 `mw_legs` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 `mw_head` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 `mw_gloves` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 `mw_feet` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 `mw_shield` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`,`chest`)
) DEFAULT CHARSET=utf8;

INSERT INTO `armorsets` VALUES
-- NO GRADE Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (1,   23,    2386, 43,   0,    0,    '3006-1;3500-1;',               0,    0,    0,        0,       0,       0,         0,       0),     -- Wooden Breastplate set (heavy)
  (2,   1101,  1104, 44,   0,    0,    '3006-1;3501-1;',               0,    0,    0,        0,       0,       0,         0,       0),     -- Devotion robe set (robe)

-- D GRADE Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (3,   58,    59,   47,   0,    0,    '3006-1;3502-1;',               628,  3543, 3611,     0,       0,       0,         0,       0),     -- Mithril Breastplate set(heavy)
  (4,   352,   2378, 2411, 0,    0,    '3006-1;3506-1;',               2493, 3544, 3611,     0,       0,       0,         0,       0),     -- Brigandine Armor set

  (5,   394,   416,  0,    0,    2422, '3006-1;3503-1;',               0,    0,    3612,     0,       0,       0,         0,       0),     -- Reinforced leather set
  (6,   395,   417,  0,    0,    2424, '3006-1;3505-1;',               0,    0,    3612,     0,       0,       0,         0,       0),     -- Manticore skin set

  (7,   436,   469,  0,    2447, 0,    '3006-1;3504-1;',               0,    0,    3613,     0,       0,       0,         0,       0),     -- Tunic of knowledge set
  (8,   437,   470,  0,    2450, 0,    '3006-1;3507-1;',               0,    0,    3613,     0,       0,       0,         0,       0),     -- Mithril Tunic

-- C GRADE Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (9,   354,   381,  2413, 0,    0,    '3006-1;3509-1;',               2495, 3545, 3614,     0,       0,       0,         0,       0),     -- Chain Mail Shirt set
  (10,  60,    0,    517,  0,    0,    '3006-1;3512-1;',               107,  3546, 3614,     0,       0,       0,         0,       0),     -- Composite Armor set
  (11,  356,   0,    2414, 0,    0,    '3006-1;3516-1;',               2497, 3547, 3614,     0,       0,       0,         0,       0),     -- Full Plate Armor set

  (12,  397,   2387, 0,    0,    62,   '3006-1;3508-1;',               0,    0,    3615,     0,       0,       0,         0,       0),     -- Mithrill shirt set
  (13,  398,   418,  0,    0,    2431, '3006-1;3511-1;',               0,    0,    3615,     0,       0,       0,         0,       0),     -- Plated leather set
  (14,  400,   420,  0,    0,    2436, '3006-1;3514-1;',               0,    0,    3615,     0,       0,       0,         0,       0),     -- Theca leather set
  (15,  401,   0,    0,    0,    2437, '3006-1;3515-1;',               0,    0,    3615,     0,       0,       0,         0,       0),     -- Drake leather set

  (16,  439,   471,  0,    2454, 0,    '3006-1;3510-1;',               0,    0,    3616,     0,       0,       0,         0,       0),     -- Karmian robe set
  (17,  441,   472,  0,    2459, 0,    '3006-1;3513-1;',               0,    0,    3616,     0,       0,       0,         0,       0),     -- Demon robe set
  (18,  442,   473,  0,    2463, 0,    '3006-1;3517-1;',               0,    0,    3616,     0,       0,       0,         0,       0),     -- Divine robe set

-- B GRADE Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (19,  357,   383,  503,  5710, 5726, '3006-1;3518-1;',               0,    0,    3617,     11355,   11363,   11356,     11359,   0),     -- Zubei's Breastplate set
  (20,  2384,  2388, 503,  5711, 5727, '3006-1;3520-1;',               0,    0,    3618,     11353,   12978,   11357,     11360,   0),     -- Zubei's leather set
  (21,  2397,  2402, 503,  5712, 5728, '3006-1;3522-1;',               0,    0,    3619,     11378,   12979,   11358,     11361,   0),     -- Zubei robe set

  (22,  2376,  2379, 2415, 5714, 5730, '3006-1;3519-1;',               673,  3548, 3617,     11375,   11373,   11365,     11370,   11374), -- Avadon heavy set
  (23,  2390,  0,    2415, 5715, 5731, '3006-1;3521-1;',               0,    0,    3618,     0,       12980,   11366,     11371,   0),     -- Avadon leather set
  (24,  2406,  0,    2415, 5716, 5732, '3006-1;3523-1;',               0,    0,    3619,     0,       12981,   11367,     11372,   0),     -- Avadon robe set

  (25,  358,   2380, 2416, 5718, 5734, '3006-1;3524-1;',               0,    0,    3617,     11394,   11403,   11399,     11396,   0),     -- Blue Wolf's Breastplate set
  (26,  2391,  0,    2416, 5719, 5735, '3006-1;3526-1;',               0,    0,    3618,     0,       12984,   11400,     11397,   0),     -- Blue wolf leather set
  (27,  2398,  2403, 2416, 5720, 5736, '3006-1;3528-1;',               0,    0,    3619,     11404,   12985,   11401,     11398,   0),     -- Blue Wolf robe set

  (28,  2381,  0,    2417, 5722, 5738, '3006-1;3525-1;',               110,  3549, 3617,     0,       11387,   11379,     11382,   11385), -- Doom plate heavy set
  (29,  2392,  0,    2417, 5723, 5739, '3006-1;3527-1;',               0,    0,    3618,     0,       12982,   11380,     11383,   0),     -- Doom leather set
  (30,  2399,  2404, 2417, 5724, 5740, '3006-1;3529-1;',               0,    0,    3619,     11406,   12983,   11381,     11384,   0),     -- Doom robe set

-- A GRADE Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (31,  365,   388,  512,  5765, 5777, '3006-1;3530-1;',               641,  3550, 3620,     11407,   11417,   11408,     11413,   11416), -- Dark Crystal Breastplate set
  (32,  2385,  2389, 512,  5766, 5778, '3006-1;3532-1;',               0,    0,    3621,     11419,   12986,   11409,     11414,   0),     -- Dark Crystal leather set
  (33,  2407,  0,    512,  5767, 5779, '3006-1;3535-1;',               0,    0,    3622,     0,       12987,   11410,     11415,   0),     -- Dark Crystal robe set

  (34,  2382,  0,    547,  5768, 5780, '3006-1;3531-1;',               0,    0,    3620,     0,       11446,   11437,     11441,   0),     -- Tallum plate heavy set
  (35,  2393,  0,    547,  5769, 5781, '3006-1;3533-1;',               0,    0,    3621,     0,       12988,   11438,     11442,   0),     -- Tallum leather set
  (36,  2400,  2405, 547,  5770, 5782, '3006-1;3534-1;',               0,    0,    3622,     11447,   12989,   11439,     11443,   0),     -- Tallum robe set

  (37,  374,   0,    2418, 5771, 5783, '3006-1;3536-1;',               2498, 3551, 3620,     0,       11481,   11472,     11477,   11480), -- Nightmare heavy set
  (38,  2394,  0,    2418, 5772, 5784, '3006-1;3538-1;',               0,    0,    3621,     0,       12992,   11473,     11478,   0),     -- Nightmare leather set
  (39,  2408,  0,    2418, 5773, 5785, '3006-1;3540-1;',               0,    0,    3622,     0,       12993,   11474,     11479,   0),     -- Robe of nightmare set

  (40,  2383,  0,    2419, 5774, 5786, '3006-1;3537-1;',               0,    0,    3620,     0,       11456,   11448,     11453,   0),     -- Majestic plate heavy set
  (41,  2395,  0,    2419, 5775, 5787, '3006-1;3539-1;',               0,    0,    3621,     0,       12990,   11449,     11454,   0),     -- Majestic leather set
  (42,  2409,  0,    2419, 5776, 5788, '3006-1;3541-1;',               0,    0,    3622,     0,       12991,   11450,     11455,   0),     -- Majestic robe set

-- S GRADE Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (43,  6373,  6374, 6378, 6375, 6376, '3006-1;3553-1;',               6377, 3554, 3623,     11505,   11509,   11506,     11507,   11508), -- Imperial crusader set
  (44,  6379,  0,    6382, 6380, 6381, '3006-1;3555-1;',               0,    0,    3624,     0,       11486,   11483,     11484,   0),     -- Draconic leather set
  (45,  6383,  0,    6386, 6384, 6385, '3006-1;3556-1;',               0,    0,    3625,     0,       11490,   11487,     11489,   0),     -- Major arcana robe set

-- Clan Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (46,  7851,  0,    7850, 7852, 7853, '3006-1;3605-1;',               0,    0,    3611,     0,       0,       0,         0,       0),     -- Clan oath Armor set (heavy)
  (47,  7854,  0,    7850, 7855, 7856, '3006-1;3606-1;',               0,    0,    3612,     0,       0,       0,         0,       0),     -- Clan Oath Brigandine set (light)
  (48,  7857,  0,    7850, 7858, 7859, '3006-1;3607-1;',               0,    0,    3613,     0,       0,       0,         0,       0),     -- Clan Oath Aketon set (robe)

  (49,  9821,  0,    9820, 9822, 9823, '3006-1;3605-1;',               0,    0,    3611,     0,       0,       0,         0,       0),     -- Shadow Item: Clan oath Armor set (heavy)
  (50,  9824,  0,    9820, 9825, 9826, '3006-1;3606-1;',               0,    0,    3612,     0,       0,       0,         0,       0),     -- Shadow Item: Clan Oath Brigandine set (light)
  (51,  9827,  0,    9820, 9828, 9829, '3006-1;3607-1;',               0,    0,    3613,     0,       0,       0,         0,       0),     -- Shadow Item: Clan Oath Aketon set (robe)

  (52,  7861,  0,    7860, 7862, 7863, '3006-1;3608-1;',               0,    0,    3620,     0,       0,       0,         0,       0),     -- Apella plate armor set (heavy)
  (53,  7864,  0,    7860, 7865, 7866, '3006-1;3609-1;',               0,    0,    3621,     0,       0,       0,         0,       0),     -- Apella Brigandine set (light)
  (54,  7867,  0,    7860, 7868, 7869, '3006-1;3610-1;',               0,    0,    3622,     0,       0,       0,         0,       0),     -- Apella Doublet set (robe)

  (55,  9831,  0,    9830, 9832, 9833, '3006-1;3608-2;',               0,    0,    3620,     0,       0,       0,         0,       0),     -- Improved Apella plate armor set (heavy)
  (56,  9834,  0,    9830, 9835, 9836, '3006-1;3609-2;',               0,    0,    3621,     0,       0,       0,         0,       0),     -- Improved Apella Brigandine set (light)
  (57,  9837,  0,    9830, 9838, 9839, '3006-1;3610-2;',               0,    0,    3622,     0,       0,       0,         0,       0),     -- Improved Apella Doublet set (robe)

  (58,  14583, 0,    14582, 14584, 14585, '3006-1;3608-2;',               0,    0,    3620,     0,       0,       0,         0,       0),     -- Apella Combat Armor set (heavy)
  (59,  14586, 0,    14582, 14587, 14588, '3006-1;3609-2;',               0,    0,    3621,     0,       0,       0,         0,       0),     -- Apella Combat Clothes set (light)
  (60,  14589, 0,    14582, 14590, 14591, '3006-1;3610-2;',               0,    0,    3622,     0,       0,       0,         0,       0),     -- Apella Combat Overcoat set (robe)

-- S80 Dynasty Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (61,  9416,  9421, 9422, 9423, 9424, '3006-1;3412-1;',               9441, 3417, 3623,     11512,   11557,   11513,     11526,   11532), -- Dynasty Breast Plate
  (62,  9417,  9421, 9422, 9423, 9424, '3006-1;3348-1;',               9441, 3417, 3623,     11512,   11557,   11513,     11526,   11532), -- Dynasty Breast Plate - Shield Master
  (63,  9418,  9421, 9422, 9423, 9424, '3006-1;3349-1;',               0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Breast Plate - Weapon Master
  (64,  9419,  9421, 9422, 9423, 9424, '3006-1;3350-1;',               0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Breast Plate - Force Master
  (65,  9420,  9421, 9422, 9423, 9424, '3006-1;3351-1;',               0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Breast Plate - Bard

  (66,  9425,  9428, 9429, 9430, 9431, '3006-1;3413-1;',               0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor
  (67,  9426,  9428, 9429, 9430, 9431, '3006-1;3352-1;',               0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor - Dagger Master
  (68,  9427,  9428, 9429, 9430, 9431, '3006-1;3353-1;',               0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor - Bow Master

  (69,  9432,  9437, 9438, 9439, 9440, '3006-1;3416-1;',               0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Tunic
  (70,  9433,  9437, 9438, 9439, 9440, '3006-1;3354-1;',               0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Tunic - Healer
  (71,  9434,  9437, 9438, 9439, 9440, '3006-1;3355-1;',               0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Tunic - Enchanter
  (72,  9435,  9437, 9438, 9439, 9440, '3006-1;3356-1;',               0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Tunic - Summoner
  (73,  9436,  9437, 9438, 9439, 9440, '3006-1;3357-1;',               0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Tunic - Human Wizard

  (74,  10126, 9428, 9429, 9430, 9431, '3006-1;3414-1;',               0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor - Force Master
  (75,  10127, 9428, 9429, 9430, 9431, '3006-1;3415-1;',               0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor - Weapon Master

  (76,  10168, 9428, 9429, 9430, 9431, '3006-1;3355-1;',               0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor - Enchanter

  (77,  10214, 9428, 9429, 9430, 9431, '3006-1;3420-1;',               0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor - Summoner

  (78,  10228, 9421, 9422, 9423, 9424, '3006-1;3636-1;',               9441, 3417, 3623,     11512,   11557,   11513,     11526,   11532), -- Dynasty Platinum Plate - Shield Master
  (79,  10229, 9421, 9422, 9423, 9424, '3006-1;3637-1;',               0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Platinum Plate - Weapon Master
  (80,  10230, 9421, 9422, 9423, 9424, '3006-1;3639-1;',               0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Platinum Plate - Force Master
  (81,  10231, 9421, 9422, 9423, 9424, '3006-1;3638-1;',               0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Platinum Plate - Bard

  (82,  10233, 9428, 9429, 9430, 9431, '3006-1;3640-1;',               0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Mail Dagger Master
  (83,  10234, 9428, 9429, 9430, 9431, '3006-1;3641-1;',               0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Mail Bow Master

  (84,  10236, 9437, 9438, 9439, 9440, '3006-1;3645-1;',               0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Silver Satin Tunic Healer
  (85,  10237, 9437, 9438, 9439, 9440, '3006-1;3646-1;',               0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Silver Satin Tunic Enchanter
  (86,  10238, 9437, 9438, 9439, 9440, '3006-1;3647-1;',               0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Silver Satin Tunic Summoner
  (87,  10239, 9437, 9438, 9439, 9440, '3006-1;3648-1;',               0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Silver Satin Tunic Human Wizard

  (88,  10487, 9428, 9429, 9430, 9431, '3006-1;3642-1;',               0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jeweled Leather Armor Force Master
  (89,  10488, 9428, 9429, 9430, 9431, '3006-1;3643-1;',               0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jeweled Leather Armor Weapon Master
  (90,  10489, 9428, 9429, 9430, 9431, '3006-1;3646-1;',               0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jeweled Leather Armor Enchanter
  (91,  10490, 9428, 9429, 9430, 9431, '3006-1;3644-1;',               0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jeweled Leather Armor Summoner

-- S84 Vesper Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (92,  13432, 13438,13137,13439,13440,'3006-1;8283-1;',               13471,3417, 3623,     0,       0,       0,         0,       0),     -- Vesper Breastplate
  (93,  13433, 13441,13138,13442,13443,'3006-1;8285-1;',               0,    0,    3624,     0,       0,       0,         0,       0),     -- Vesper Leather Breastplate
  (94,  13434, 13444,13139,13445,13446,'3006-1;8287-1;',               0,    0,    3625,     0,       0,       0,         0,       0),     -- Vesper Tunic

  (95,  13435, 13448,13140,13449,13450,'3006-1;8284-1;',               13471,3417, 3623,     0,       0,       0,         0,       0),     -- Vesper Noble Breastplate
  (96,  13436, 13451,13141,13452,13453,'3006-1;8286-1;',               0,    0,    3624,     0,       0,       0,         0,       0),     -- Vesper Noble Leather Breastplate
  (97,  13437, 13454,13142,13455,13456,'3006-1;8288-1;',               0,    0,    3625,     0,       0,       0,         0,       0),     -- Vesper Noble Tunic

-- A GRADE PVP Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (98,  10793, 0,    2418, 5771, 5783, '3006-1;8193-1;3659-1;3662-1;', 2498, 3551, 3620,     0,       11481,   11472,     11477,   11480), -- Armor of Nightmare - PvP
  (99,  10794, 0,    2419, 5774, 5786, '3006-1;8194-1;3659-1;3662-1;', 0,    0,    3620,     0,       11456,   11448,     11453,   0),     -- Majestic Plate Armor - PvP
  (100, 10795, 0,    2418, 5772, 5784, '3006-1;8195-1;3663-1;',        0,    0,    3621,     0,       12992,   11473,     11478,   0),     -- Leather Armor of Nightmare - PvP
  (101, 10796, 0,    2419, 5775, 5787, '3006-1;8196-1;3663-1;',        0,    0,    3621,     0,       12990,   11449,     11454,   0),     -- Majestic Leather Mail - PvP
  (102, 10797, 0,    2418, 5773, 5785, '3006-1;8197-1;3660-1;',        0,    0,    3622,     0,       12993,   11474,     11479,   0),     -- Robe of Nightmare - PvP
  (103, 10798, 0,    2419, 5776, 5788, '3006-1;8198-1;3660-1;',        0,    0,    3622,     0,       12991,   11450,     11455,   0),     -- Majestic Robe - PvP

-- S GRADE PVP Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (104, 10799, 6374, 6378, 6375, 6376, '3006-1;8199-1;3659-1;3662-1;', 6377, 3554, 3623,     11505,   11509,   11506,     11507,   11508), -- Imperial Crusader Breastplate - PvP
  (105, 10800, 0,    6382, 6380, 6381, '3006-1;8200-1;3663-1;',        0,    0,    3624,     0,       11486,   11483,     11484,   0),     -- Draconic Leather Armor - PvP
  (106, 10801, 0,    6386, 6384, 6385, '3006-1;8201-1;3660-1;',        0,    0,    3625,     0,       11490,   11487,     11489,   0),     -- Major Arcana Robe - PvP

-- S80 Dynasty PVP Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (107, 10802, 9421, 9422, 9423, 9424, '3006-1;8202-1;3659-1;3662-1;', 9441, 3417, 3623,     11512,   11557,   11513,     11526,   11532), -- Dynasty Breastplate - PvP
  (108, 10803, 9421, 9422, 9423, 9424, '3006-1;8203-1;3659-1;3662-1;', 9441, 3417, 3623,     11512,   11557,   11513,     11526,   11532), -- Dynasty Breastplate - PvP Shield Master
  (109, 10804, 9421, 9422, 9423, 9424, '3006-1;8204-1;3659-1;3662-1;', 0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Breastplate - PvP Weapon Master
  (110, 10805, 9421, 9422, 9423, 9424, '3006-1;8205-1;3659-1;3662-1;', 0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Breastplate - PvP Force Master
  (111, 10806, 9421, 9422, 9423, 9424, '3006-1;8206-1;3659-1;3662-1;', 0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Breastplate - PvP Bard

  (112, 10807, 9428, 9429, 9430, 9431, '3006-1;8207-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor - PvP
  (113, 10808, 9428, 9429, 9430, 9431, '3006-1;8208-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor - PvP Dagger Master
  (114, 10809, 9428, 9429, 9430, 9431, '3006-1;8209-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor - PvP Bow Master

  (115, 10810, 9437, 9438, 9439, 9440, '3006-1;8210-1;3660-1;',        0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Tunic - PvP
  (116, 10811, 9437, 9438, 9439, 9440, '3006-1;8211-1;3660-1;',        0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Tunic - PvP Healer
  (117, 10812, 9437, 9438, 9439, 9440, '3006-1;8212-1;3660-1;',        0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Tunic - PvP Enchanter
  (118, 10813, 9437, 9438, 9439, 9440, '3006-1;8213-1;3660-1;',        0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Tunic - PvP Summoner
  (119, 10814, 9437, 9438, 9439, 9440, '3006-1;8214-1;3660-1;',        0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Tunic - PvP Wizard

  (120, 10815, 9428, 9429, 9430, 9431, '3006-1;8215-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor - PvP Force Master
  (121, 10816, 9428, 9429, 9430, 9431, '3006-1;8216-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor - PvP Weapon Master
  (122, 10817, 9428, 9429, 9430, 9431, '3006-1;8217-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor - PvP Enchanter
  (123, 10818, 9428, 9429, 9430, 9431, '3006-1;8218-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Leather Armor - PvP Summoner

  (124, 10820, 9421, 9422, 9423, 9424, '3006-1;8219-1;3659-1;3662-1;', 9441, 3417, 3623,     11512,   11557,   11513,     11526,   11532), -- Dynasty Platinum Breastplate - PvP Shield Master
  (125, 10821, 9421, 9422, 9423, 9424, '3006-1;8220-1;3659-1;3662-1;', 0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Platinum Breastplate - PvP Weapon Master
  (126, 10822, 9421, 9422, 9423, 9424, '3006-1;8222-1;3659-1;3662-1;', 0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Platinum Breastplate - PvP Force Master
  (127, 10823, 9421, 9422, 9423, 9424, '3006-1;8221-1;3659-1;3662-1;', 0,    0,    3623,     11512,   11557,   11513,     11526,   0),     -- Dynasty Platinum Breastplate - PvP Bard

  (128, 10825, 9428, 9429, 9430, 9431, '3006-1;8223-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Armor - PvP Dagger Master
  (129, 10826, 9428, 9429, 9430, 9431, '3006-1;8224-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Armor - PvP Bow Master

  (130, 10828, 9437, 9438, 9439, 9440, '3006-1;8229-1;3660-1;',        0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Silver Satin Tunic - PvP Healer
  (131, 10829, 9437, 9438, 9439, 9440, '3006-1;8230-1;3660-1;',        0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Silver Satin Tunic - PvP Enchanter
  (132, 10830, 9437, 9438, 9439, 9440, '3006-1;8231-1;3660-1;',        0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Silver Satin Tunic - PvP Summoner
  (133, 10831, 9437, 9438, 9439, 9440, '3006-1;8232-1;3660-1;',        0,    0,    3625,     11558,   11539,   11514,     11533,   0),     -- Dynasty Silver Satin Tunic - PvP Wizard

  (134, 10832, 9428, 9429, 9430, 9431, '3006-1;8225-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Armor - PvP	Force Master
  (135, 10833, 9428, 9429, 9430, 9431, '3006-1;8226-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Armor - PvP	Weapon Master
  (136, 10834, 9428, 9429, 9430, 9431, '3006-1;8228-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Armor - PvP	Enchanter
  (137, 10835, 9428, 9429, 9430, 9431, '3006-1;8227-1;3663-1;',        0,    0,    3624,     11516,   11525,   11515,     11524,   0),     -- Dynasty Jewel Leather Armor - PvP	Summoner

-- S84 Vesper PVP Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (138, 14520, 13438,13137,13439,13440,'3006-1;8301-1;3659-1;3662-1;', 13471,3417, 3623,     0,       0,       0,         0,       0),     -- Vesper Breastplate {PvP}
  (139, 14521, 13441,13138,13442,13443,'3006-1;8303-1;3663-1;',        0,    0,    3624,     0,       0,       0,         0,       0),     -- Vesper Leather Breastplate {PvP}
  (140, 14522, 13444,13139,13445,13446,'3006-1;8305-1;3660-1;',        0,    0,    3625,     0,       0,       0,         0,       0),     -- Vesper Tunic {PvP}

  (141, 14523, 13448,13140,13449,13450,'3006-1;8302-1;3659-1;3662-1;', 13471,3417, 3623,     0,       0,       0,         0,       0),     -- Vesper Noble Breastplate {PvP}
  (142, 14524, 13451,13141,13452,13453,'3006-1;8304-1;3663-1;',        0,    0,    3624,     0,       0,       0,         0,       0),     -- Vesper Noble Leather Breastplate {PvP}
  (143, 14525, 13454,13142,13455,13456,'3006-1;8306-1;3660-1;',        0,    0,    3625,     0,       0,       0,         0,       0),     -- Vesper Noble Tunic {PvP}

-- Special Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
-- (144,  9670,  9671, 9669,0,    0,    '8277-1;',                      0,    0,    0,        0,       0,       0,         0,       0),     -- Natives Set (bestows Native transform skill when all are worn together)
-- (145, 13806, 13807,13805,0,    0,    '8278-1;',                      0,    0,    0,        0,       0,       0,         0,       0),     -- Guard of Dawns Set (bestows Guard transform skill when all are worn together)

--  Newly added or custom sets should be added here, at the end of the file adjusting IDs to avoid conflics

-- L2JFree Customs
-- SHADOW B GRADE
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (144,  9067,  9068,  9069,  9071,  9072, '3006-1;3518-1;',              0,     0,    3617,     0,       0,       0,         0,       0),--	Shadow Item: Zubei's Breastplate set
  (145,  9073,  9074,  9069,  9075,  9076, '3006-1;3520-1;',              0,     0,    3618,     0,       0,       0,         0,       0),--	Shadow Item: Zubei's leather set
  (146,  9077,  9078,  9069,  9079,  9080, '3006-1;3522-1;',              0,     0,    3619,     0,       0,       0,         0,       0),--	Shadow Item: Zubei robe set

-- SHADOW A GRADE
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (147,  9081,  9082,  9083,  9085,  9086, '3006-1;3530-1;',              641,   3550, 3620,     0,       0,       0,         0,       0),--	Shadow Item: Dark Crystal Breastplate set
  (148,  9087,  9088,  9083,  9089,  9090, '3006-1;3532-1;',              0,     0,    3621,     0,       0,       0,         0,       0),--	Shadow Item: Dark Crystal leather set
  (149,  9091,     0,  9083,  9092,  9093, '3006-1;3535-1;',              0,     0,    3622,     0,       0,       0,         0,       0),--	Shadow Item: Dark Crystal robe set

  (150,  9094,     0,  9095,  9096,  9097, '3006-1;3537-1;',              0,     0,    3620,     0,       0,       0,         0,       0),--	Shadow Item: Majestic plate heavy set
  (151,  9098,     0,  9095,  9099,  9100, '3006-1;3539-1;',              0,     0,    3621,     0,       0,       0,         0,       0),--	Shadow Item: Majestic leather set
  (152,  9101,     0,  9095,  9102,  9103, '3006-1;3541-1;',              0,     0,    3622,     0,       0,       0,         0,       0),--	Shadow Item: Majestic robe set

-- MasterWorks B GRADE
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (153, 11364,    383,  503,  5710,  5726, '3006-1;3518-1;',              0,     0,    3617, 11355,   11363,   11356,     11359,       0),--	Masterwork Zubei's Breastplate set
  (154, 11354,   2388,  503,  5711,  5727, '3006-1;3520-1;',              0,     0,    3618, 11353,   12978,   11357,     11360,       0),--	Masterwork Zubei's leather set
  (155, 11377,   2402,  503,  5712,  5728, '3006-1;3522-1;',              0,     0,    3619, 11378,   12979,   11358,     11361,       0),--	Masterwork Zubei robe set

  (156, 11376,  2379,  2415,  5714,  5730, '3006-1;3519-1;',              673,   3548, 3617, 11375,   11373,   11365,     11370,   11374),--	Masterwork Avadon heavy set
  (157, 11368,     0,  2415,  5715,  5731, '3006-1;3521-1;',              0,     0,    3618,     0,   12980,   11366,     11371,       0),--	Masterwork Avadon leather set
  (158, 11369,     0,  2415,  5716,  5732, '3006-1;3523-1;',              0,     0,    3619,     0,   12981,   11367,     11372,       0),--	Masterwork Avadon robe set

  (159, 11405,  2380,  2416,  5718,  5734, '3006-1;3524-1;',              0,     0,    3617, 11395,   11403,   11399,     11396,       0),--	Masterwork Blue Wolf's Breastplate set
  (160, 11395,     0,  2416,  5719,  5735, '3006-1;3526-1;',              0,     0,    3618,     0,   12984,   11400,     11397,       0),--	Masterwork Blue wolf leather set
  (161, 11402,  2403,  2416,  5720,  5736, '3006-1;3528-1;',              0,     0,    3619, 11404,   12985,   11401,     11398,       0),--	Masterwork Blue Wolf robe set

  (162, 11386,     0,  2417,  5722,  5738, '3006-1;3525-1;',              110,  3549,  3617,     0,   11387,   11379,     11382,   11385),--	Masterwork Doom plate heavy set
  (163, 11388,     0,  2417,  5723,  5739, '3006-1;3527-1;',              0,     0,    3618,     0,   12982,   11380,     11383,       0),--	Masterwork Doom leather set
  (164, 11393,  2404,  2417,  5724,  5740, '3006-1;3529-1;',              0,     0,    3619, 11406,   12983,   11381,     11384,       0),--	Masterwork Doom robe set

-- MasterWorks A GRADE
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (165, 11418,   388,   512,  5765,  5777, '3006-1;3530-1;',              641,   3550, 3620, 11407,   11417,   11408,     11413,   11416),--	Masterwork Dark Crystal Breastplate set
  (166, 11411,  2389,   512,  5766,  5778, '3006-1;3532-1;',              0,     0,    3621, 11419,   12986,   11409,     11414,       0),--	Masterwork Dark Crystal leather set
  (167, 11412,     0,   512,  5767,  5779, '3006-1;3535-1;',              0,     0,    3622,     0,   12987,   11410,     11415,       0),--	Masterwork Dark Crystal robe set

  (168, 11445,     0,   547,  5768,  5780, '3006-1;3531-1;',              0,     0,    3620,     0,   11446,   11437,     11441,       0),--	Masterwork Tallum plate heavy set
  (169, 11440,     0,   547,  5769,  5781, '3006-1;3533-1;',              0,     0,    3621,     0,   12988,   11438,     11442,       0),--	Masterwork Tallum leather set
  (170, 11444,  2405,   547,  5770,  5782, '3006-1;3534-1;',              0,     0,    3622, 11447,   12989,   11439,     11443,       0),--	Masterwork Tallum robe set

  (171, 11471,     0,  2418,  5771,  5783, '3006-1;3536-1;',              2498,  3551, 3620,     0,   11481,   11472,     11477,   11480),--	Masterwork Nightmare heavy set
  (172, 11475,     0,  2418,  5772,  5784, '3006-1;3538-1;',              0,     0,    3621,     0,   12992,   11473,     11478,       0),--	Masterwork Nightmare leather set
  (173, 11476,     0,  2418,  5773,  5785, '3006-1;3540-1;',              0,     0,    3622,     0,   12993,   11474,     11479,       0),--	Masterwork Robe of nightmare set

  (174, 11457,     0,  2419,  5774,  5786, '3006-1;3537-1;',              0,     0,    3620,     0,   11456,   11448,     11453,       0),--	Masterwork Majestic plate heavy set
  (175, 11451,     0,  2419,  5775,  5787, '3006-1;3539-1;',              0,     0,    3621,     0,   12990,   11449,     11454,       0),--	Masterwork Majestic leather set
  (176, 11452,     0,  2419,  5776,  5788, '3006-1;3541-1;',              0,     0,    3622,     0,   12991,   11450,     11455,       0),--	Masterwork Majestic robe set

-- MasterWorks S GRADE
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (177, 11510,  6374,  6378,  6375,  6376, '3006-1;3553-1;',              6377,  3554, 3623, 11505,   11509,   11506,     11507,   11508),--	Masterwork Imperial crusader set
  (178, 11485,     0,  6382,  6380,  6381, '3006-1;3555-1;',              0,     0,    3624,     0,   11486,   11483,     11484,       0),--	Masterwork Draconic leather set
  (179, 11488,     0,  6386,  6384,  6385, '3006-1;3556-1;',              0,     0,    3625,     0,   11490,   11487,     11489,       0),--	Masterwork Major arcana robe set

  (180, 10819,  9421,  9422,  9423,  9424, '0-0;',                        9441,  3417, 3623,     0,       0,       0,         0,       0),--	Dynasty Platinum Breastplate - PvP
  (181, 10824,  9428,  9429,  9430,  9431, '0-0;',                        0,     0,    3624,     0,       0,       0,         0,       0),--	Dynasty Jewel Leather Armor - PvP
  (182, 10827,  9437,  9438,  9439,  9440, '0-0;',                        0,     0,    3625,     0,       0,       0,         0,       0),--	Dynasty Silver Satin Tunic - PvP

-- Some other Dynasty Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (183, 10232,  9428,  9429,  9430,  9431, '0-0;',                        0,     0,    3624,     0,       0,       0,         0,       0),--	Dynasty Jewel Leather Mail
  (184, 10235,  9437,  9438,  9439,  9440, '0-0;',                        0,     0,    3625,     0,       0,       0,         0,       0),--	Dynasty Silver Satin Tunic

-- Special Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (185,  9670,  9671,  9669,     0,     0, '3006-1;8277-1;',              0,     0,       0,     0,       0,       0,         0,       0),--	Natives Set (bestows Native transform skill when all are worn together)
  (186, 13806, 13807, 13805,     0,     0, '3006-1;8278-1;',              0,     0,       0,     0,       0,       0,         0,       0),--	Guard of Dawns Set (bestows Guard transform skill when all are worn together)

-- Clan Armor Sets
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (187, 14583,     0, 14582, 14584, 14585, '3006-1;3608-2;',              0,     0,    3620,     0,       0,       0,         0,       0),--	Apella Combat Armor set (heavy)
  (188, 14586,     0, 14582, 14587, 14588, '3006-1;3609-2;',              0,     0,    3621,     0,       0,       0,         0,       0),--	Apella Combat Clothes set (light)
  (189, 14589,     0, 14582, 14590, 14591, '3006-1;3610-2;',              0,     0,    3622,     0,       0,       0,         0,       0),--	Apella Combat Overcoat set (robe)

-- Friendship Sets 30 day limited period
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (190, 14978,     0, 14979, 14980, 14981, '3006-1;3535-1;',              0,     0,    3622,     0,       0,       0,         0,       0),--	Dark Crystal robe set
  (191, 14983,     0, 14984, 14985, 14986, '3006-1;3523-1;',              0,     0,    3619,     0,       0,       0,         0,       0),--	Avadon robe set
  (192, 14988, 14989,     0, 14990,     0, '3006-1;3510-1;',              0,     0,    3616,     0,       0,       0,         0,       0),--	Karmian robe set
  (193, 14992, 14993,     0, 14994,     0, '3006-1;3507-1;',              0,     0,    3613,     0,       0,       0,         0,       0),--	Mithril Tunic
  (194, 14996, 14997, 14979, 14998, 14999, '3006-1;3532-1;',              0,     0,    3621,     0,       0,       0,         0,       0),--	Dark Crystal leather set
  (195, 15000,     0, 15001, 15003, 15005, '3006-1;3527-1;',              0,     0,    3618,     0,       0,       0,         0,       0),--	Doom leather set
  (196, 15006,     0, 15001, 15002, 15004, '3006-1;3525-1;',              15007, 3549, 3617,     0,       0,       0,         0,       0),--	Doom plate heavy set
  (197, 15008, 15009,     0,     0, 15010, '3006-1;3511-1;',              0,     0,    3615,     0,       0,       0,         0,       0),--	Plated leather set
  (198, 15013,     0, 15012,     0,     0, '3006-1;3516-1;',              15016, 3547, 3614,     0,       0,       0,         0,       0),--	Full Plate Armor set
  (199, 15017, 15018,     0,     0, 15019, '3006-1;3505-1;',              0,     0,    3612,     0,       0,       0,         0,       0),--	Manticore skin set
  (200, 15021, 15022, 15023,     0,     0, '3006-1;3506-1;',              15026, 3544, 3611,     0,       0,       0,         0,       0),--	Brigandine Armor set
  (201, 15027,     0, 15028, 15029, 15030, '3006-1;3536-1;',              15031, 3551, 3620,     0,       0,       0,         0,       0),--	Nightmare heavy set

-- Friendship Sets 10 day limited period
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (202, 15092,     0, 15093, 15094, 15095, '3006-1;3535-1;',              0,     0,    3622,     0,       0,       0,         0,       0),--	Dark Crystal robe set
  (203, 15097,     0, 15098, 15099, 15100, '3006-1;3523-1;',              0,     0,    3619,     0,       0,       0,         0,       0),--	Avadon robe set
  (204, 15102, 15103,     0, 15104,     0, '3006-1;3510-1;',              0,     0,    3616,     0,       0,       0,         0,       0),--	Karmian robe set
  (205, 15106, 15107,     0, 15108,     0, '3006-1;3507-1;',              0,     0,    3613,     0,       0,       0,         0,       0),--	Mithril Tunic
  (206, 15110, 15111, 15093, 15112, 15113, '3006-1;3532-1;',              0,     0,    3621,     0,       0,       0,         0,       0),--	Dark Crystal leather set
  (207, 15114,     0, 15115, 15117, 15119, '3006-1;3527-1;',              0,     0,    3618,     0,       0,       0,         0,       0),--	Doom leather set
  (208, 15120,     0, 15115, 15116, 15118, '3006-1;3525-1;',              15121, 3549, 3617,     0,       0,       0,         0,       0),--	Doom plate heavy set
  (209, 15122, 15123,     0,     0, 15124, '3006-1;3511-1;',              0,     0,    3615,     0,       0,       0,         0,       0),--	Plated leather set
  (210, 15127,     0, 15126,     0,     0, '3006-1;3516-1;',              15130, 3547, 3614,     0,       0,       0,         0,       0),--	Full Plate Armor set
  (211, 15131, 15132,     0,     0, 15133, '3006-1;3505-1;',              0,     0,    3612,     0,       0,       0,         0,       0),--	Manticore skin set
  (212, 15135, 15136, 15137,     0,     0, '3006-1;3506-1;',              15140, 3544, 3611,     0,       0,       0,         0,       0),--	Brigandine Armor set
  (213, 15141,     0, 15142, 15143, 15144, '3006-1;3536-1;',              15145, 3551, 3620,     0,       0,       0,         0,       0),--	Nightmare heavy set

-- Friendship Sets 90 day limited period
-- id   chest  legs  head  glov  feet  skill                           shld  shsk  enchant6  mw_legs  mw_head  mw_gloves  mw_feet  mw_shield
  (214, 16866,     0, 16867, 16868, 16860, '3006-1;3535-1;',              0,     0,    3622,     0,       0,       0,         0,       0),--	Dark Crystal robe set
  (215, 16871,     0, 16872, 16873, 16874, '3006-1;3523-1;',              0,     0,    3619,     0,       0,       0,         0,       0),--	Avadon robe set
  (216, 16876, 16877,     0, 16878,     0, '3006-1;3510-1;',              0,     0,    3616,     0,       0,       0,         0,       0),--	Karmian robe set
  (217, 16880, 16881,     0, 16882,     0, '3006-1;3507-1;',              0,     0,    3613,     0,       0,       0,         0,       0),--	Mithril Tunic
  (218, 16884, 16885, 16867, 16886, 16887, '3006-1;3532-1;',              0,     0,    3621,     0,       0,       0,         0,       0),--	Dark Crystal leather set
  (219, 16888,     0, 16889, 16891, 16893, '3006-1;3527-1;',              0,     0,    3618,     0,       0,       0,         0,       0),--	Doom leather set
  (220, 16894,     0, 16889, 16890, 16892, '3006-1;3525-1;',              16895, 3549, 3617,     0,       0,       0,         0,       0),--	Doom plate heavy set
  (221, 16896, 16897,     0,     0, 16898, '3006-1;3511-1;',              0,     0,    3615,     0,       0,       0,         0,       0),--	Plated leather set
  (222, 16901,     0, 16900,     0,     0, '3006-1;3516-1;',              16904, 3547, 3614,     0,       0,       0,         0,       0),--	Full Plate Armor set
  (223, 16905, 16906,     0,     0, 16907, '3006-1;3505-1;',              0,     0,    3612,     0,       0,       0,         0,       0),--	Manticore skin set
  (224, 16909, 16910, 16911,     0,     0, '3006-1;3506-1;',              16914, 3544, 3611,     0,       0,       0,         0,       0),--	Brigandine Armor set
  (225, 16915,     0, 16916, 16917, 16918, '3006-1;3536-1;',              16919, 3551, 3620,     0,       0,       0,         0,       0);--	Nightmare heavy set

