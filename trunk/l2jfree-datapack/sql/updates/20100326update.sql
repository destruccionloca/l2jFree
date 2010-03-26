UPDATE `hellbounds` SET `value` = 
(SELECT `value` FROM `quest_global_data` WHERE `quest_name` = 'Hellbound' AND `var` = 'HellboundPoints') 
WHERE `variable` = 'trust_points';

UPDATE `hellbounds` SET `value` = 
(SELECT `value` FROM `quest_global_data` WHERE `quest_name` = '1108_Hellbound_WarpGate' AND `var` = 'WarpGateEnergy') 
WHERE `variable` = 'warpgates_energy';