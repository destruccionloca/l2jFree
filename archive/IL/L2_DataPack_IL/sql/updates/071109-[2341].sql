ALTER TABLE armor ADD COLUMN item_display_id int(11) NOT NULL DEFAULT 0 AFTER item_id;
update armor set item_display_id = item_id;

ALTER TABLE weapon ADD COLUMN item_display_id int(11) NOT NULL DEFAULT 0 AFTER item_id;
update weapon set item_display_id = item_id;

ALTER TABLE etcitem ADD COLUMN item_display_id int(11) NOT NULL DEFAULT 0 AFTER item_id;
update etcitem set item_display_id = item_id;