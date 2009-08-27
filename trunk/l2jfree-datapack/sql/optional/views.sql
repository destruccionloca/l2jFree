-- This VIEWs could be very slow, so think before using.

CREATE VIEW `online_characters` AS
	SELECT
		`characters`.*
	FROM
		`characters`
	WHERE
		`characters`.`online` > 0
	ORDER BY
		`characters`.`account_name` ASC;

CREATE VIEW `online_characters_with_class_names` AS
	SELECT
		`class_list`.`class_name`,
		`characters`.`classid` AS `class_id`,
		`characters`.*
	FROM
		`characters`
	INNER JOIN
		`class_list` ON `characters`.`classid` = `class_list`.`id`
	WHERE
		`characters`.`online` > 0
	ORDER BY
		`characters`.`account_name` ASC;

CREATE VIEW `character_skills_with_char_names` AS
	SELECT
		`characters`.`account_name`,
		`characters`.`char_name`,
		`characters`.`accesslevel`,
		`character_skills`.*
	FROM
		`character_skills`
	INNER JOIN
		`characters` USING (`charId`)
	ORDER BY
		`characters`.`account_name` ASC,
		`characters`.`char_name` ASC,
		`character_skills`.`class_index` ASC,
		`character_skills`.`skill_id` ASC;

CREATE VIEW `character_skills_save_with_char_names` AS
	SELECT
		`characters`.`account_name`,
		`characters`.`char_name`,
		`characters`.`accesslevel`,
		`character_skills_save`.*
	FROM
		`character_skills_save`
	INNER JOIN
		`characters` USING (`charId`)
	ORDER BY
		`characters`.`account_name` ASC,
		`characters`.`char_name` ASC,
		`character_skills_save`.`class_index` ASC,
		`character_skills_save`.`skill_id` ASC;

CREATE VIEW `heroes_with_char_names` AS
	SELECT
		`characters`.`account_name`,
		`characters`.`char_name`,
		`characters`.`accesslevel`,
		`heroes`.*
	FROM
		`heroes`
	INNER JOIN
		`characters` USING (`charId`)
	ORDER BY
		`characters`.`account_name` ASC,
		`characters`.`char_name` ASC;

CREATE VIEW `olympiad_nobles_with_char_names` AS
	SELECT
		`characters`.`account_name`,
		`characters`.`char_name`,
		`characters`.`accesslevel`,
		`olympiad_nobles`.*
	FROM
		`olympiad_nobles`
	INNER JOIN
		`characters` USING (`charId`)
	ORDER BY
		`characters`.`account_name` ASC,
		`characters`.`char_name` ASC;

CREATE VIEW `olympiad_nobles_eom_with_char_names` AS
	SELECT
		`characters`.`account_name`,
		`characters`.`char_name`,
		`characters`.`accesslevel`,
		`olympiad_nobles_eom`.*
	FROM
		`olympiad_nobles_eom`
	INNER JOIN
		`characters` USING (`charId`)
	ORDER BY
		`characters`.`account_name` ASC,
		`characters`.`char_name` ASC;

CREATE VIEW `item_names` AS
		SELECT
			`armor`.`item_id` AS `item_id`,
			`armor`.`name` AS `item_name`,
			"armor" AS `item_type`
		FROM
			`armor`
	UNION
		SELECT
			`etcitem`.`item_id` AS `item_id`,
			`etcitem`.`name` AS `item_name`,
			"etcitem" AS `item_type`
		FROM
			`etcitem`
	UNION
		SELECT
			`weapon`.`item_id` AS `item_id`,
			`weapon`.`name` AS `item_name`,
			"weapon" AS `item_type`
		FROM
			`weapon`
	ORDER BY
		`item_id` ASC;

CREATE VIEW `items_with_char_and_item_names` AS
	SELECT
		`characters`.`account_name`,
		`characters`.`char_name`,
		`characters`.`accesslevel`,
		`item_names`.`item_name`,
		`item_names`.`item_type`,
		`items`.*
	FROM
		`items`
	INNER JOIN
		`characters` ON `characters`.`charId` = `items`.`owner_id`
	INNER JOIN
		`item_names` ON item_names.`item_id` = `items`.`item_id`
	ORDER BY
		`characters`.`account_name` ASC,
		`characters`.`char_name` ASC,
		`items`.`item_id` ASC;