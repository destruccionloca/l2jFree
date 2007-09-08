-- Note for L2JFree users : Luxury Gatekeeper is already in table `custom_npc`
DELETE FROM `npc` WHERE `id`='7077';

INSERT INTO `npc` VALUES
(7077, 31275, 'Tinkerbell', 1, 'Luxury Gatekeeper', 1, 'NPC.a_teleporter_FHuman', 8.00, 25.00, 70, 'female', 'L2Teleporter', 40, 3862, 1494, NULL, NULL, 40, 43, 30, 21, 20, 10, 5879, 590, 1444, 514, 760, 381, 253, 0, 253, 0, 0, 0, 80, 120, NULL, NULL, 0, 0,'LAST_HIT');