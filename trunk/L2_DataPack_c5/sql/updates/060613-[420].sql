UPDATE characters SET base_class=classid WHERE obj_Id NOT IN (SELECT char_obj_id FROM character_subclasses) and classid!= base_class