ALTER TABLE characters DROP apprentice;
ALTER TABLE characters DROP varka;
ALTER TABLE characters drop ketra;
ALTER TABLE characters ADD COLUMN last_recom_date decimal(20,0) NOT NULL DEFAULT 0 AFTER pledge_type;
ALTER TABLE characters ADD COLUMN apprentice int(1) NOT NULL DEFAULT 0 AFTER pledge_rank;
ALTER TABLE characters ADD COLUMN sponsor int(1) NOT NULL DEFAULT 0 AFTER apprentice;
ALTER TABLE characters ADD COLUMN varka_ketra_ally int(1) NOT NULL DEFAULT 0 AFTER sponsor;
delete from clan_privs where rank=0;