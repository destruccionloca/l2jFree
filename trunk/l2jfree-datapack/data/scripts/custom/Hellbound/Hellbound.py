# Psycho(killer1888) / L2jFree
# This script is under developpement and may not return complete official gameplay
# Note also that it needs serious rework.

import sys
from com.l2jfree.gameserver.model.quest.jython    import QuestJython as JQuest
from com.l2jfree.gameserver.datatables            import DoorTable
from com.l2jfree.gameserver.datatables            import SpawnTable
from com.l2jfree.gameserver.model                 import L2Spawn
from com.l2jfree.gameserver.datatables            import NpcTable
from com.l2jfree.tools.random                     import Rnd
from com.l2jfree.gameserver.model.itemcontainer   import PcInventory
from com.l2jfree.gameserver.model                 import L2ItemInstance
from com.l2jfree.gameserver.network.serverpackets import InventoryUpdate
from com.l2jfree.gameserver.network.serverpackets import SystemMessage
from com.l2jfree.gameserver.network               import SystemMessageId


qn = "Hellbound"

QUEST_RATE = 3 #!! Needs levels adjustement and mob points!!

#BELETH_CLAN
JUNIOR_WATCHMAN = 22320
JUNIOR_SUMMONER = 22321
BLIND_HUNTSMAN  = 22324
BLIND_WATCHMAN  = 22325
ARCANE_SCOUT    = 22327
ARCANE_GUARDIAN = 22328
ARCANE_WATCHMAN = 22329
REMNANT_DIABOLIST = 18463
REMNANT_DIVINER = 18464
DARION_EXECUTIONER = 22343
DARION_ENFORCER = 22342
KELTAS = 22341
DEREK = 18465
HELLINARK = 22326
OUTPOST_CAPTAIN = 22354
QUARRY_FOREMAN = 22346
QUARRY_SUPERVISOR = 22344
QUARRY_PATROL = 22347

MOB_LVL1 = [JUNIOR_WATCHMAN,JUNIOR_SUMMONER,BLIND_HUNTSMAN,BLIND_WATCHMAN,ARCANE_SCOUT,ARCANE_GUARDIAN,ARCANE_WATCHMAN]
MOB_LVL2 = [REMNANT_DIABOLIST,REMNANT_DIVINER]
MOB_LVL3 = [DARION_EXECUTIONER,DARION_ENFORCER,KELTAS]
MOB_LVL4 = [DEREK]
MOB_LVL5 = [QUARRY_FOREMAN, QUARRY_SUPERVISOR, QUARRY_PATROL]
MOB_LVL6 = [HELLINARK]
MOB_LVL8 = [OUTPOST_CAPTAIN]

#NATIVES_CLAN
SUBJUGATED_NATIVE = 22322
CHARMED_NATIVE    = 22323
NATIVE_SLAVE      = 32357
NATIVE_PRISONER   = 32358

#NPC
KIEF = 32354
HUDE = 32298
BERNARDE = 32300

#Items
BADGE = 9674
BASIC_CERTIFICATE = 9850
STANDART_CERTIFICATE = 9851
PREMIUM_CERTIFICATE = 9852
NATIVE_TREASURE = 9684

HUDE_ITEMS = [9628,9629,9630]

# Transformation
NATIVE_TRANSFORMATION = 101

#Levels
LEVEL1 = 0
LEVEL2 = 300000
LEVEL3 = 600000
LEVEL4 = 1000000
LEVEL5 = 1030000
LEVEL6 = 1060000
LEVEL7 = 1090000
LEVEL8 = 1110000
LEVEL9 = 1140000
LEVEL10 = 20000000

class PyObject:
	pass

def newSpawn(npcId,x,y,z,heading,respawnTime,respawnMinDelay,respawnMaxDelay):
	template = NpcTable.getInstance().getTemplate(npcId)
	spawn = L2Spawn(template)
	spawn.setLocx(x)
	spawn.setLocy(y)
	spawn.setLocz(z)
	spawn.setHeading(heading)
	spawn.setAmount(1)
	spawn.setRespawnDelay(respawnTime)
	spawn.setRespawnMinDelay(respawnMinDelay);
	spawn.setRespawnMaxDelay(respawnMaxDelay);
	objectId = spawn.spawnOne(False)
	spawn.init();
	return objectId

def spawnBadNatives(self):
	self.badNativeSpawns = PyObject()
	self.badNativeSpawns.npclist = []
	newNpc = newSpawn(22323, -11789, 237702, -3160, 3326, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -10577, 237356, -3149, 65055, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -10135, 237547, -3140, 3599, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -10661, 237586, -3131, 30769, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -12013, 238409, -3238, 24810, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -12356, 238609, -3282, 29650, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -12292, 238438, -3273, 52887, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -12636, 237769, -3230, 43213, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -12707, 237458, -3227, 44352, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -14083, 236811, -3293, 35434, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -14496, 236664, -3313, 41845, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -14639, 236636, -3318, 28059, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -14609, 236468, -3323, 50973, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -13364, 236368, -3273, 1631, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -12971, 236486, -3296, 3816, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -12820, 236699, -3294, 9951, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -14583, 238345, -3255, 25133, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -15021, 238531, -3292, 29420, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -16535, 238373, -3330, 32821, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -16959, 238296, -3345, 36123, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -17413, 238605, -3333, 25672, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -17737, 239430, -3346, 19952, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -18001, 239769, -3365, 19704, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -17932, 239864, -3366, 9831, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -18049, 240131, -3371, 19365, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -18166, 240661, -3376, 17798, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -18234, 241303, -3352, 15904, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -18361, 241412, -3344, 25370, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -19519, 242628, -3372, 24425, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -19656, 242519, -3372, 39777, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -20207, 242776, -3320, 28240, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -21160, 240982, -2857, 51672, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -21010, 240506, -2826, 52975, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -20663, 240445, -2853, 2590, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -20251, 240755, -2863, 7569, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -20092, 240668, -2863, 3751, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -19731, 240540, -2863, 56116, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -19764, 239165, -2858, 48081, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -19675, 238917, -2853, 52575, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -19956, 238359, -2782, 44076, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -20065, 238088, -2733, 44347, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -20307, 237889, -2611, 44366, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -20312, 239074, -2809, 20021, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -23048, 244030, -3121, 23552, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -23413, 244327, -3142, 30937, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -23516, 244214, -3142, 41727, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -24043, 245009, -3139, 26509, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -24456, 245273, -3142, 25605, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -24400, 245342, -3139, 9272, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -24289, 245875, -3142, 15881, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -23771, 246239, -3142, 2938, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -24184, 246829, -3140, 21354, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -24131, 246895, -3136, 9326, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -24505, 246200, -3142, 41105, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -24714, 245802, -3142, 44337, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -23633, 251135, -3317, 15517, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -23827, 251508, -3374, 36744, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -24813, 251592, -3308, 32068, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -25028, 252160, -3303, 17292, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -25199, 252162, -3278, 37604, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -24432, 251648, -3358, 60699, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -23089, 251919, -3370, 2435, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -22686, 251747, -3375, 61704, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -27193, 253489, -2189, 16530, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -27251, 254398, -2100, 17713, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -27448, 254772, -2083, 21474, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -27193, 255304, -2023, 10269, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -26802, 255580, -1961, 4395, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -27421, 255938, -1974, 26847, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -27944, 256663, -1934, 23050, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -28210, 257060, -1931, 21060, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -28041, 257288, -1939, 9346, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -26399, 257076, -1935, 65162, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -26156, 257041, -1924, 64043, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -25425, 257018, -2132, 280, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -25510, 256393, -2155, 45089, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -25196, 255451, -2147, 52706, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -25582, 254862, -2147, 48633, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -25732, 254391, -2151, 45744, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -26459, 254151, -2147, 39584, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -26308, 253936, -2144, 55421, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -28023, 253782, -2149, 31123, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22323, -28535, 253708, -2149, 33574, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22322, -28741, 254135, -2169, 18991, 60, 60, 60)
	self.badNativeSpawns.npclist.append(newNpc)


def spawnOutpostCaptain(self):
	self.OutpostCaptainSpawn = PyObject()
	self.OutpostCaptainSpawn.npclist = []
	newNpc = newSpawn(18466, -4489, 246914, -1916, 45973, 36000, 36000, 36000)
	self.OutpostCaptainSpawn.npclist.append(newNpc)

def spawnQuarryGuards(self):
	self.quarryGuardsSpawn = PyObject()
	self.quarryGuardsSpawn.npclist = []
	newNpc = newSpawn(QUARRY_FOREMAN, -8494, 242371, -1886, 16380, 900, 900, 1100)
	self.quarryGuardsSpawn.npclist.append(newNpc)
	newNpc = newSpawn(QUARRY_SUPERVISOR, -6730, 243501, -2105, 12892, 900, 900, 1100)
	self.quarryGuardsSpawn.npclist.append(newNpc)
	newNpc = newSpawn(QUARRY_PATROL, -4391, 243431, -2084, 22405, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -5467, 243695, -2035, 32977, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -5253, 242252, -2072, 54337, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -4807, 241362, -1891, 53667, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -6620, 242155, -2079, 23330, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -7006, 243942, -2084, 18776, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -8158, 244221, -2089, 2104, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -7937, 242615, -2035, 58824, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -4954, 244750, -2120, 19304, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -5301, 245729, -2057, 14506, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -5352, 246972, -1892, 4050, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -4063, 247354, -1984, 65151, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -2911, 248287, -2379, 12501, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -3874, 248492, -2451, 55063, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -5001, 248211, -2465, 29686, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -4347, 248986, -2710, 8049, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -3259, 249531, -2976, 6408, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -5319, 247866, -2332, 48457, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -3822, 246389, -1890, 44943, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)										
	newNpc = newSpawn(QUARRY_PATROL, -5380, 246383, -2002, 33887, 180, 180, 180)
	self.quarryGuardsSpawn.npclist.append(newNpc)

def spawnQuarrySlaves(self):
	self.quarrySlaves = PyObject()
	self.quarrySlaves.npclist = []
	newNpc = newSpawn(32299, -4400, 245425, -2027, 46070, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -4066, 244468, -2043, 60748, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -3714, 244117, -2044, 33672, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -4216, 243814, -2020, 13700, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -5755, 243046, -2032, 38842, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -5613, 242378, -2055, 46121, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -6812, 242386, -2043, 25159, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -6419, 242875, -2067, 9528, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -6645, 244074, -2017, 14162, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -6487, 244581, -2044, 45796, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -4807, 244946, -2070, 5925, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -5649, 245574, -2047, 36959, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -6190, 245253, -2073, 58475, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -6253, 245529, -2072, 61671, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -6290, 244398, -1975, 37604, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -7566, 244970, -2046, 53860, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -8042, 243926, -2055, 59978, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -7758, 243482, -2044, 12684, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -7254, 242713, -2031, 1358, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -7123, 242154, -2023, 17094, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -6100, 241945, -2038, 15479, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -5912, 241750, -2018, 14733, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -5100, 242540, -2064, 18278, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -4710, 242741, -2066, 16383, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -5123, 243955, -2009, 24091, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -5470, 244622, -2034, 39276, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)
	newNpc = newSpawn(32299, -7389, 244290, -2050, 18480, 60, 60, 60)
	self.quarrySlaves.npclist.append(newNpc)

def spawnDerek(self):
	self.derekSpawn = PyObject()
	self.derekSpawn.npclist = []
	newNpc = newSpawn(DEREK, -25675, 254686, -2144, 16380, 36000, 36000, 36000)
	self.derekSpawn.npclist.append(newNpc)

def spawnKiefBuronInHarbor(self):
	self.KiefBuronHarborSpawn = PyObject()
	self.KiefBuronHarborSpawn.npclist = []
	newNpc = newSpawn(32345, -11954, 236171, -3272, 16380, 60, 60, 60)
	self.KiefBuronHarborSpawn.npclist.append(newNpc)
	newNpc = newSpawn(32354, -20684, 250275, -3277, 12892, 60, 60, 60)
	self.KiefBuronHarborSpawn.npclist.append(newNpc)

def spawnKiefBuronInVillage(self):
	self.KiefBuronVillageSpawn = PyObject()
	self.KiefBuronVillageSpawn.npclist = []
	newNpc = newSpawn(32345, -28504, 250132, -3480, 15879, 60, 60, 60)
	self.KiefBuronVillageSpawn.npclist.append(newNpc)
	newNpc = newSpawn(32354, -29062, 250938, -3527, 64692, 60, 60, 60)
	self.KiefBuronVillageSpawn.npclist.append(newNpc)
	for npc in self.KiefBuronHarborSpawn.npclist:
		npc.deleteMe()

def spawnHellinark(self):
	self.hellinarkSpawns = PyObject()
	self.hellinarkSpawns.npclist = []
	newNpc = newSpawn(HELLINARK, -23909, 245910, -3136, 16380, 36000, 36000, 36000)
	self.hellinarkSpawns.npclist.append(newNpc)

def spawnNatives(self):
	self.nativeSpawns = PyObject()
	self.nativeSpawns.npclist = []
	newNpc = newSpawn(32362, -26979, 251120, -3520, 42440, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -26955, 250976, -3525, 23938, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -28491, 250709, -3525, 47047, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -28830, 250205, -3480, 8615, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -28773, 250709, -3525, 10472, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -28904, 251163, -3525, 52054, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -29088, 250219, -3480, 10083, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -27234, 251187, -3525, 39999, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -27874, 251724, -3525, 15577, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -27799, 251209, -3520, 1534, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -27169, 250819, -3520, 17748, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -27041, 251883, -3520, 35048, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -27405, 252043, -3520, 30046, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -27849, 251917, -3520, 34835, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32362, -27886, 251777, -3525, 49895, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32363, -29374, 253083, -3520, 43134, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32363, -28531, 251087, -3525, 61999, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32363, -28371, 250215, -3480, 21462, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32363, -28704, 251654, -3520, 43528, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32363, -28767, 252690, -3525, 17716, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32363, -29646, 252915, -3520, 61217, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32363, -29457, 252695, -3525, 6802, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32363, -28559, 252050, -3525, 49895, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32363, -25420, 252198, -3256, 0, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)
	newNpc = newSpawn(32363, -25420, 252382, -3256, 64496, 60, 60, 60)
	self.nativeSpawns.npclist.append(newNpc)

def spawnKeltasAndUnderlings(self):
	self.keltasSpawns = PyObject()
	self.keltasSpawns.npclist = []
	newNpc = newSpawn(KELTAS, -28711, 250953, -3525, 16380, 7200, 7200, 14400)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -24296, 252703, -3048, 7497, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -24207, 253277, -3040, 21487, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -24243, 253626, -3040, 8559, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -24312, 253050, -3046, 46233, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -24999, 252677, -3048, 29794, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -25054, 251866, -3288, 55437, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -24543, 251660, -3352, 422, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -24401, 251412, -3344, 46754, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -24063, 251225, -3328, 60265, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -23693, 251219, -3312, 3335, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -23090, 252202, -3368, 16475, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -23454, 252145, -3360, 34388, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -23690, 251817, -3368, 42646, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -22651, 252472, -3280, 24730, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -23067, 252803, -3312, 25757, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -22706, 252921, -3272, 13213, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -22456, 252950, -3274, 1204, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -23513, 250247, -3256, 51759, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -23602, 250581, -3268, 31848, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -24022, 252235, -3208, 27584, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -22133, 251747, -3352, 15634, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -22235, 252066, -3368, 19611, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -24355, 252335, -3120, 30135, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -23980, 252893, -3040, 17260, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -24424, 253550, -3046, 24542, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -24651, 252575, -3056, 40569, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -25226, 252116, -3264, 45141, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -25133, 251768, -3264, 42075, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -25012, 251641, -3268, 57091, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -23240, 251280, -3288, 177, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -23087, 251860, -3367, 13084, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -22237, 251493, -3352, 63309, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -22514, 252339, -3312, 21164, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -22770, 252717, -3273, 64719, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -23517, 251302, -3320, 43779, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -23658, 250880, -3264, 45788, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -23421, 250565, -3264, 13446, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -23103, 251150, -3264, 9541, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -24409, 252249, -3088, 32390, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -22256, 251467, -3352, 59532, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -22668, 251670, -3368, 41299, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -25803, 252082, -3256, 44037, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -25615, 252253, -3248, 13258, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -27308, 252489, -3525, 28246, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -27869, 251844, -3520, 17098, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -28504, 251962, -3520, 30963, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -27207, 250895, -3520, 23310, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -28903, 250759, -3520, 49978, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -28601, 250352, -3512, 55810, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -28472, 250469, -3520, 10516, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -28981, 250541, -3520, 32161, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -27860, 251184, -3520, 49313, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -27043, 251256, -3525, 2751, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -26955, 251098, -3525, 51478, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -25725, 251897, -3248, 4257, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -26940, 252385, -3525, 63610, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -28340, 251895, -3520, 63581, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -28766, 252769, -3520, 18500, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -29684, 253063, -3525, 21419, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22342, -28848, 252819, -3520, 52985, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -27931, 251980, -3525, 20845, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -28968, 251137, -3525, 33756, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -28646, 250193, -3472, 46275, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -28687, 251753, -3525, 15584, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -27864, 251442, -3525, 51456, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -27691, 251095, -3525, 60480, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -26732, 251918, -3472, 3000, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -26958, 252092, -3520, 14817, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -28784, 252555, -3525, 49673, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -27355, 251136, -3525, 56906, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -28688, 251961, -3525, 49476, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -28538, 251183, -3525, 6437, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -29565, 252790, -3525, 23296, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -29430, 253123, -3520, 2419, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)
	newNpc = newSpawn(22343, -25604, 252085, -3248, 5309, 60, 60, 60)
	self.keltasSpawns.npclist.append(newNpc)

def spawnRemnant(self):
	self.remnantSpawns = PyObject()
	self.remnantSpawns.npclist = []
	newNpc = newSpawn(18463, -27061, 254941, -2042, 46320, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18463, -27455, 255499, -2012, 25521, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18463, -27918, 256493, -1932, 27597, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18463, -28073, 257065, -1933, 7677, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18463, -26991, 257600, -1932, 63910, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18463, -26661, 257615, -1926, 473, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18463, -25730, 254439, -2153, 54360, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18463, -25099, 255567, -2147, 16358, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18463, -29151, 255839, -2147, 42134, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18463, -29068, 254817, -2167, 50499, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18463, -27982, 254332, -2153, 58681, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18463, -27263, 254089, -2129, 48379, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -27281, 255183, -2039, 49515, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -27286, 255929, -1960, 12297, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -27583, 256373, -1924, 24848, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -27949, 256665, -1932, 18243, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -28234, 256868, -1941, 26312, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -27462, 257674, -1928, 4984, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -26343, 256435, -1933, 53950, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -26166, 255304, -2145, 53579, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -26008, 254988, -2147, 52882, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -25102, 254910, -2144, 19611, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -26666, 256235, -1911, 62890, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -28666, 254838, -2167, 63197, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -27693, 254611, -2110, 7116, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)
	newNpc = newSpawn(18464, -27180, 253936, -2141, 54336, 60, 60, 60)
	self.remnantSpawns.npclist.append(newNpc)

def increaseHellboundPoints(self, npcId, actualPoints, addSpecific):
	if addSpecific == 0:
		points = self.BelethClan[npcId]*QUEST_RATE
	else:
		points = addSpecific
	actualPoints += points
	self.saveGlobalQuestVar("HellboundPoints",""+str(actualPoints)+"")
	if actualPoints >= LEVEL2 and self.remnant == 0:
		self.remnant = 1
		spawnRemnant(self)
		print "Hellbound went up to stage 2"
	elif actualPoints >= LEVEL3 and self.keltas == 0:
		self.keltas = 1
		spawnKeltasAndUnderlings(self)
		print "Hellbound went up to stage 3"
	elif actualPoints >= LEVEL4 and self.derek == 0:
		self.derek = 1
		spawnDerek(self)
		print "Hellbound went up to stage 4"
	elif actualPoints >= LEVEL5 and self.quarry == 0:
		self.quarry = 1
		spawnNatives(self)
		spawnKiefBuronInVillage(self)
		spawnQuarryGuards(self)
		for mob in self.remnantSpawns.npclist:
			mob.getSpawn().stopRespawn()
			mob.deleteMe()
		for mob in self.badNativeSpawns.npclist:
			mob.getSpawn().stopRespawn()
			mob.deleteMe()
		if self.keltas == 1:
			for mob in self.keltasSpawns.npclist:
				mob.getSpawn().stopRespawn()
				mob.deleteMe()
		if self.derek == 1:
			for mob in self.derekSpawn.npclist:
				mob.getSpawn().stopRespawn()
				mob.deleteMe()
		print "Hellbound went up to stage 5"
	elif actualPoints >= LEVEL6 and self.hellinark == 0:
		self.hellinark = 1
		spawnHellinark(self)
		if self.quarry == 1:
			for mob in self.quarryGuardsSpawn.npclist:
				mob.getSpawn().stopRespawn()
				mob.deleteMe()
			for mob in self.quarrySlaves.npclist:
				mob.getSpawn().stopRespawn()
				mob.deleteMe()
		print "Hellbound went up to stage 6"
	elif actualPoints >= LEVEL7 and self.WoundedPassage == 0:
		self.WoundedPassage = 1
		DoorTable.getInstance().getDoor(20250002).openMe()
		print "Hellbound went up to stage 7"
	elif actualPoints >= LEVEL8 and self.outpost_captain == 0:
		self.outpost_captain = 1
		spawnOutpostCaptain(self)
		print "Hellbound went up to stage 8"
	elif actualPoints >= LEVEL9 and self.IronGate == 0:
		self.IronGate = 1
		if self.outpost_captain == 1:
			for mob in self.OutpostCaptainSpawn.npclist:
				mob.getSpawn().stopRespawn()
				mob.deleteMe()
		DoorTable.getInstance().getDoor(20250001).openMe()
		print "Hellbound went up to stage 9"
	return

class Hellbound (JQuest):
	def __init__(self,id,name,descr):
		self.remnant = 0
		self.keltas = 0
		self.WoundedPassage = 0
		self.IronGate = 0
		self.derek = 0
		self.hellinark = 0
		self.outpost_captain = 0
		self.quarry = 0
		#npcid,trustpoints+
		self.BelethClan ={
			JUNIOR_WATCHMAN:1,
			JUNIOR_SUMMONER:1,
			BLIND_HUNTSMAN:1,
			BLIND_WATCHMAN:1,
			ARCANE_SCOUT:3,
			ARCANE_GUARDIAN:3,
			ARCANE_WATCHMAN:3,
			REMNANT_DIABOLIST:5,
			REMNANT_DIVINER:5,
			DARION_EXECUTIONER:3,
			DARION_ENFORCER:3,
			KELTAS:100,
			DEREK:10000,
			HELLINARK:10000,
			OUTPOST_CAPTAIN:10000
			}
		#npcid,trustpoints-
		self.NativeClan ={
			SUBJUGATED_NATIVE:10,
			CHARMED_NATIVE:10,
			NATIVE_SLAVE:10,
			NATIVE_PRISONER:10
			}
		JQuest.__init__(self,id,name,descr)
		actualPoints = self.loadGlobalQuestVar("HellboundPoints")
		if actualPoints == "":
			self.saveGlobalQuestVar("HellboundPoints","0")
			actualPoints = 0
		print "Hellbound loaded with: "+str(actualPoints)+" points"
		spawnKiefBuronInHarbor(self)
		spawnQuarrySlaves(self)
		if int(actualPoints) < LEVEL5:
			spawnBadNatives(self)
		if int(actualPoints) >= LEVEL2 and int(actualPoints) < LEVEL5:
			self.remnant = 1
			spawnRemnant(self)
			print "Hellbound: Remnant spawned"
		if int(actualPoints) >= LEVEL3 and int(actualPoints) < LEVEL5:
			self.keltas = 1
			spawnKeltasAndUnderlings(self)
			print "Hellbound: Keltas spawned"
		if int(actualPoints) >= LEVEL4 and int(actualPoints) < LEVEL5:
			self.derek = 1
			spawnDerek(self)
			print "Hellbound: Derek spawned"
		if int(actualPoints) >= LEVEL5:
			self.quarry = 1
			spawnNatives(self)
			spawnKiefBuronInVillage(self)
			spawnQuarryGuards(self)
			print "Hellbound: Native spawned"
		if int(actualPoints) >= LEVEL6 and int(actualPoints) < LEVEL7:
			self.hellinark = 1
			spawnHellinark(self)
			print "Hellbound: Hellinark spawned"
		if int(actualPoints) >= LEVEL7:
			self.WoundedPassage = 1
			DoorTable.getInstance().getDoor(20250002).openMe()
			print "Hellbound: Wounded passage opened"
		if int(actualPoints) >= LEVEL8 and int(actualPoints) < LEVEL9:
			self.outpost_catpain = 1
			spawnOutpostCaptain(self)
			print "Hellbound: Outpost Captain spawned"
		if int(actualPoints) >= LEVEL9:
			self.IronGate = 1
			DoorTable.getInstance().getDoor(20250001).openMe()
			print "Hellbound: Iron Gate opened"

	def onFirstTalk (self,npc,player):
		st = player.getQuestState(qn)
		if not st : st = self.newQuestState(player)
		npcId = npc.getNpcId()
		actualPoints = self.loadGlobalQuestVar("HellboundPoints")
		htmltext = ""
		if npcId == KIEF:
			if int(actualPoints) < 999000:
				htmltext = "kief_exchange.htm"
			else:
				htmltext = "kief_trade.htm"
		elif npcId == HUDE:
			basicCertif = player.getInventory().getItemByItemId(BASIC_CERTIFICATE)
			standartCertif = player.getInventory().getItemByItemId(STANDART_CERTIFICATE)
			premiumCertif = player.getInventory().getItemByItemId(PREMIUM_CERTIFICATE)
			if int(actualPoints) > LEVEL2 and int(actualPoints) < LEVEL4:
				if not basicCertif:
					htmltext = "hude_no.htm"
				elif basicCertif:
					htmltext = "hude.htm"
			elif int(actualPoints) > LEVEL4 and int(actualPoints) < LEVEL7:
				if not basicCertif:
					htmltext = "hude_no.htm"
				elif basicCertif and not standartCertif:
					htmltext = "hude_certificate.htm"
				elif basicCertif and standartCertif:
					htmltext = "hude_basic.htm"
				else:
					htmltext = "hude_no.htm"
			elif int(actualPoints) > LEVEL7:
				if not basicCertif:
					htmltext = "hude_no.htm"
				if basicCertif and not standartCertif:
					htmltext = "hude_certificate.htm"
				elif basicCertif and standartCertif and not premiumCertif:
					htmltext = "hude_premium_certificate.htm"
				elif basicCertif and standartCertif and  premiumCertif:
					htmltext = "hude_advanced.htm"
				else:
					htmltext = "hude_no.htm"
			else:
				htmltext = "hude_no.htm"
		elif npcId == BERNARDE:
			if player.isTransformed() and player.getTransformationId() == NATIVE_TRANSFORMATION:
				if int(actualPoints) < 999000:
					htmltext = "bernarde_trade.htm"
				elif int(actualPoints) >= 999000 and int(actualPoints) < LEVEL4:
					htmltext = "bernarde_advanced.htm"
				else:
					htmltext = "bernarde.htm"
			else:
				htmltxt = "bernarde_no.htm"
		return htmltext

	def onAdvEvent (self,event,npc,player):
		htmltext = event
		st = player.getQuestState(qn)
		if not st: return
		npcId = npc.getNpcId()
		if npcId == KIEF:
			if event == "kief_exchangeBadges":
				if player.getInventory().getItemByItemId(BADGE) >= 1:
					player.destroyItemByItemId("Kief Exchange", BADGE, 1, player, True)
					actualPoints = self.loadGlobalQuestVar("HellboundPoints")
					actualPoints = int(actualPoints)
					if actualPoints + 10 > 999000:
						points = 999000 - actualPoints
					else:
						points = 10
					increaseHellboundPoints(self, 0, actualPoints, points)
					htmltext = "kief_thanks.htm"
				else:
					htmltext = "<html><body>You don't have any Darion's Badge...</body></html>"
			if event == "kief_exchangeAllBadges":
				if player.getInventory().getItemByItemId(BADGE) >= 1:
					count = st.getQuestItemsCount(BADGE)
					player.destroyItemByItemId("Kief Exchange", BADGE, count, player, True)
					actualPoints = self.loadGlobalQuestVar("HellboundPoints")
					actualPoints = int(actualPoints)
					if actualPoints + (10 * count) > 999000:
						points = 999000 - actualPoints
					else:
						increaseHellboundPoints(self, 0, actualPoints, 10 * count)
					htmltext = "kief_thanks.htm"
				else:
					htmltext = "<html><body>You don't have any Darion's Badge...</body></html>"
		elif npcId == HUDE:
			if event == "hude_tradeall":
				item = player.getInventory().getItemByItemId(BADGE)
				if item.getCount()>=10:
					for step in range(10,item.getCount(),10):
						player.destroyItemByItemId("Quest", BADGE, 10, player, True)
						i = Rnd.get(len(HUDE_ITEMS))
						item = player.getInventory().addItem("Quest", HUDE_ITEMS[i], 1, player, None)
						iu = InventoryUpdate()
						iu.addItem(item)
						player.sendPacket(iu);
						sm = SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2)
						sm.addItemName(item)
						sm.addNumber(1)
						player.sendPacket(sm)
					return
			if event == "hude_trade":
				item = player.getInventory().getItemByItemId(BADGE)
				if not item:
					player.sendPacket(SystemMessage.sendString("You must have 10 Darion Badges in your Inventory."))	
					return
				elif item.getCount()<10:
					player.sendPacket(SystemMessage.sendString("You must have 10 Darion Badges in your Inventory."))	
					return
				else:
					player.destroyItemByItemId("Quest", BADGE, 10, player, True)
					i = Rnd.get(len(HUDE_ITEMS))
					item = player.getInventory().addItem("Quest", HUDE_ITEMS[i], 1, player, None)
					iu = InventoryUpdate()
					iu.addItem(item)
					player.sendPacket(iu);
					sm = SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2)
					sm.addItemName(item)
					sm.addNumber(1)
					player.sendPacket(sm)
				return
		elif npcId == BERNARDE:
			if event == "bernarde_treasure":
				if player.getInventory().getItemByItemId(NATIVE_TREASURE) >= 1:
					player.destroyItemByItemId("Bernarde Exchange", NATIVE_TREASURE, 1, player, True)
					actualPoints = self.loadGlobalQuestVar("HellboundPoints")
					actualPoints = int(actualPoints)
					pointsToAdd = LEVEL4 - actualPoints
					increaseHellboundPoints(self, 0, actualPoints, pointsToAdd)
					htmltext = "bernarde_thanks.htm"
		return htmltext

	def onKill (self,npc,player,isPet):
		npcId = npc.getNpcId()
		if self.BelethClan.has_key(npcId) :
			actualPoints = self.loadGlobalQuestVar("HellboundPoints")
			actualPoints = int(actualPoints)
			if npcId in MOB_LVL1 and actualPoints < LEVEL2:
				increaseHellboundPoints(self, npcId, actualPoints, 0)
			elif npcId in MOB_LVL2 and actualPoints >= LEVEL2 and actualPoints < LEVEL4:
				increaseHellboundPoints(self, npcId, actualPoints, 0)
			#elif npcId in MOB_LVL3 and actualPoints >= LEVEL3 and actualPoints < LEVEL4:
			elif npcId in MOB_LVL3 and actualPoints >= LEVEL3 and actualPoints < 999000:
				increaseHellboundPoints(self, npcId, actualPoints, 0)
			elif npcId in MOB_LVL4 and actualPoints >= LEVEL4 and actualPoints < LEVEL5:
				increaseHellboundPoints(self, npcId, actualPoints, 0)
			elif npcId in MOB_LVL6 and actualPoints >= LEVEL6 and actualPoints < LEVEL7:
				increaseHellboundPoints(self, npcId, actualPoints, 0)
			elif npcId in MOB_LVL8 and actualPoints >= LEVEL8 and actualPoints < LEVEL9:
				increaseHellboundPoints(self, npcId, actualPoints, 0)
		elif self.NativeClan.has_key(npcId) :
			actualPoints = self.loadGlobalQuestVar("HellboundPoints")
			actualPoints = int(actualPoints)
			if actualPoints >= (10 * QUEST_RATE):
				points = actualPoints - (self.NativeClan[npcId]*QUEST_RATE)
				self.saveGlobalQuestVar("HellboundPoints",""+str(points)+"")
				if points < LEVEL9 and self.IronGate == 1:
					self.IronGate = 0
					self.outpost_captain = 1
					DoorTable.getInstance().getDoor(20250001).closeMe()
					spawnOutpostCaptain(self)
					print "Hellbound went down to stage 8 => Iron Gate closed"
				if points < LEVEL8 and self.outpost_captain == 1:
					self.outpost_captain = 0
					self.WoundedPassage = 1
					for mob in self.OutpostCaptainSpawn.npclist:
						mob.getSpawn().stopRespawn()
						mob.deleteMe()
					print "Hellbound went down to stage 7 => Outpost Captain despawned"
				if points < LEVEL7 and self.WoundedPassage == 1:
					self.WoundedPassage = 0
					self.hellinark = 1
					DoorTable.getInstance().getDoor(20250002).closeMe()
					spawnHellinark(self)
					print "Hellbound went down to stage 6 => Wounded Passage closed"
				if points < LEVEL6 and self.hellinark == 1:
					self.hellinark = 0
					self.quarry = 1
					spawnQuarryGuards(self)
					for mob in self.hellinarkSpawns.npclist:
						mob.getSpawn().stopRespawn()
						mob.deleteMe()
					print "Hellbound went down to stage 5 => Hellinark despawned"
				if points < LEVEL5 and self.quarry == 1:
					self.quarry = 0
					self.derek = 1
					spawnDerek(self)
					for npc in self.nativeSpawns.npclist:
						npc.deleteMe()
					for npc in self.KiefBuronInVillage.npclist:
						npc.deleteMe()
					spawnBadNatives(self)
					spawnKiefBuronInHarbor(self)
					print "Hellbound went down to stage 4 => Natives flew"
				if points < LEVEL4 and self.derek == 1:
					self.derek = 0
					for mob in self.derekSpawn.npclist:
						mob.getSpawn().stopRespawn()
						mob.deleteMe()
					print "Hellbound went down to stage 3 => Derek despawned"
				if points < LEVEL3 and self.keltas == 1:
					self.keltas = 0
					for mob in self.keltasSpawns.npclist:
						mob.getSpawn().stopRespawn()
						mob.deleteMe()
					print "Hellbound went down to stage 2 => Keltas and underlings despawned"
				if points < LEVEL2 and self.remnant == 1:
					self.remnant = 0
					for mob in self.remnantSpawns.npclist:
						mob.getSpawn().stopRespawn()
						mob.deleteMe()
					print "Hellbound went down to stage 1 => Remnant despawned"
		return

QUEST = Hellbound(-1, qn, "custom")
for i in QUEST.BelethClan.keys():
	QUEST.addKillId(i)
for i in QUEST.NativeClan.keys():
	QUEST.addKillId(i)

QUEST.addTalkId(KIEF)
QUEST.addFirstTalkId(KIEF)
QUEST.addTalkId(HUDE)
QUEST.addFirstTalkId(HUDE)
QUEST.addTalkId(BERNARDE)
QUEST.addFirstTalkId(BERNARDE)