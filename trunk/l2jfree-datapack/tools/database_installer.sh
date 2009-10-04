#!/bin/bash

############################################
##           WARNING!  WARNING!           ##
##                                        ##
## Don't edit this script on Windows OS   ##
## Or use a software which allows you to  ##
## write in UNIX type                     ##
############################################
## Written by Respawner                   ##
## License: GNU GPL                       ##
## Based on L2JDP script                  ##
############################################

# Catch kill signals
trap finish 1 2 15

# Configure the database access
configure()
{
	echo "#################################################"
	echo "#               Configuration area              #"
	echo "#         Please answer to the questions        #"
	echo "#################################################"
	MYSQLDUMPPATH=`which mysqldump 2>/dev/null`
	MYSQLPATH=`which mysql 2>/dev/null`
	if [ $? -ne 0 ]; then
		echo "Unable to find MySQL binaries on your PATH"
		while :
		do
			echo -ne "\nPlease enter MySQL binaries directory (no trailing slash): "
			read MYSQLBINPATH
			if [ -e "$MYSQLBINPATH" ] && [ -d "$MYSQLBINPATH" ] && [ -e "$MYSQLBINPATH/mysqldump" ] && [ -e "$MYSQLBINPATH/mysql" ]; then
				MYSQLDUMPPATH="$MYSQLBINPATH/mysqldump"
				MYSQLPATH="$MYSQLBINPATH/mysql"
				break
			else
				echo "Invalid data. Please verify and try again."
				exit 1
			fi
		done
	fi

	# LoginServer
	echo -ne "\nPlease enter MySQL LoginServer hostname (default localhost): "
	read LSDBHOST
	if [ -z "$LSDBHOST" ]; then
		LSDBHOST="localhost"
	fi
	echo -ne "\nPlease enter MySQL Login Server database name (default l2jdb): "
	read LSDB
	if [ -z "$LSDB" ]; then
		LSDB="l2jdb"
	fi
	echo -ne "\nPlease enter MySQL Login Server user (default root): "
	read LSUSER
	if [ -z "$LSUSER" ]; then
		LSUSER="root"
	fi
	echo -ne "\nPlease enter MySQL Login Server $LSUSER's password (won't be displayed) :"
	stty -echo
	read LSPASS
	stty echo
	echo ""
	if [ -z "$LSPASS" ]; then
		echo "Please avoid empty password else you will have a security problem."
	fi

	# GameServer
	echo -ne "\nPlease enter MySQL Game Server hostname (default $LSDBHOST): "
	read GSDBHOST
	if [ -z "$GSDBHOST" ]; then
		GSDBHOST="$LSDBHOST"
	fi
	echo -ne "\nPlease enter MySQL Game Server database name (default $LSDB): "
	read GSDB
	if [ -z "$GSDB" ]; then
		GSDB="$LSDB"
	fi
	echo -ne "\nPlease enter MySQL Game Server user (default $LSUSER): "
	read GSUSER
	if [ -z "$GSUSER" ]; then
		GSUSER="$LSUSER"
	fi
	echo -ne "\nPlease enter MySQL Game Server $GSUSER's password (won't be displayed): "
	stty -echo
	read GSPASS
	stty echo
	echo ""
	if [ -z "$GSPASS" ]; then
		echo "Please avoid empty password else you will have a security problem."
	fi
}

# Actions which can be performed
action_type()
{
	echo "#################################################"
	echo "#           Database Installer Script           #"
	echo "#################################################"
	echo ""
	echo "What do you want to do?"
	echo "Database backup           [b] (make a backup of the existing tables)"
	echo "Insert backups            [r] (Erase all the tables and insert the backups)"
	echo "Full installation         [f] (for first installation, this will erase all the existing tables)"
	echo "Update non critical data  [u] (Only erase and reinsert tables without players' data)"
	echo "Database update           [d] (Update tables which contain player's data using update files)"
	echo "Complete update           [c] (Update your database without data loss)"
	echo "Insert one table          [t] (Only insert one table in your database)"
	echo "Quit this script          [q]"
	echo -ne "Choice: "
	read ACTION_CHOICE
	case "$ACTION_CHOICE" in
		"b"|"B") backup_db; finish;;
		"r"|"R") insert_backup; finish;;
		"f"|"F") full_install; finish;;
		"u"|"U") update_db noncritical; finish;;
		"d"|"D") update_db critical; finish;;
		"c"|"C") update_db noncritical; update_db critical; finish;;
		"t"|"T") table_insert;;
		"q"|"Q") finish;;
		*)       action_type;;
	esac
}

# Make a backup of the LS and GS database
backup_db()
{
	echo "#################################################"
	echo "#                Database Backup                #"
	echo "#################################################"
	echo ""
	echo "LoginServer backup"
	$MYSQLDUMPPATH --add-drop-table -h $LSDBHOST -u $LSUSER --password=$LSPASS $LSDB > loginserver_backup.sql
	echo "GameServer backup"
	$MYSQLDUMPPATH --add-drop-table -h $GSDBHOST -u $GSUSER --password=$GSPASS $GSDB > gameserver_backup.sql
}

# Insert backups
insert_backup()
{
	echo "#################################################"
	echo "#                Database Backup                #"
	echo "#################################################"
	echo ""
	echo "What backups do you want to insert?"
	echo "Enter the full path of your backups."
	echo "LoginServer backup: "
	read LS_BACKUP
	echo "GameServer backup: "
	read GS_BACKUP
	echo "Deleting old tables"
	$MYL < login_install.sql &> /dev/null
	$MYG < full_install.sql &> /dev/null
	echo "Inserting Backups"
	$MYL < ../sql/$LS_BACKUP &> /dev/null
	$MYG < ../sql/$GS_BACKUP &> /dev/null
	echo "Backup restore completed"
}

# Full installation (erase and insert all tables)
full_install()
{
	echo "#################################################"
	echo "#          Full Database Installation           #"
	echo "#################################################"
	echo ""
	echo "LoginServer database"
	$MYL < login_install.sql &> /dev/null
	$MYL < ../sql/accounts.sql &> /dev/null
	$MYL < ../sql/gameservers.sql &> /dev/null
	echo "GameServer database"
	$MYG < full_install.sql &> /dev/null
	$MYG < ../sql/account_data.sql &> /dev/null
	$MYG < ../sql/armor.sql &> /dev/null
	$MYG < ../sql/armorsets.sql &> /dev/null
	$MYG < ../sql/auction.sql &> /dev/null
	$MYG < ../sql/auction_bid.sql &> /dev/null
	$MYG < ../sql/auction_lots.sql &> /dev/null
	$MYG < ../sql/auction_watch.sql &> /dev/null
	$MYG < ../sql/auto_announcements.sql &> /dev/null
	$MYG < ../sql/auto_chat.sql &> /dev/null
	$MYG < ../sql/auto_chat_text.sql &> /dev/null
	$MYG < ../sql/castle.sql &> /dev/null
	$MYG < ../sql/castle_door.sql &> /dev/null
	$MYG < ../sql/castle_doorupgrade.sql &> /dev/null
	$MYG < ../sql/castle_functions.sql &> /dev/null
	$MYG < ../sql/castle_manor_procure.sql &> /dev/null
	$MYG < ../sql/castle_manor_production.sql &> /dev/null
	$MYG < ../sql/castle_siege_guards.sql &> /dev/null
	$MYG < ../sql/castle_zoneupgrade.sql &> /dev/null
	$MYG < ../sql/certification_skill_trees.sql &> /dev/null
	$MYG < ../sql/char_creation_items.sql &> /dev/null
	$MYG < ../sql/char_templates.sql &> /dev/null
	$MYG < ../sql/character_friends.sql &> /dev/null
	$MYG < ../sql/character_hennas.sql &> /dev/null
	$MYG < ../sql/character_macroses.sql &> /dev/null
	$MYG < ../sql/character_quest_global_data.sql &> /dev/null
	$MYG < ../sql/character_quests.sql &> /dev/null
	$MYG < ../sql/character_recipebook.sql &> /dev/null
	$MYG < ../sql/character_recommend_data.sql &> /dev/null
	$MYG < ../sql/character_recommends.sql &> /dev/null
	$MYG < ../sql/character_shortcuts.sql &> /dev/null
	$MYG < ../sql/character_skills.sql &> /dev/null
	$MYG < ../sql/character_skills_save.sql &> /dev/null
	$MYG < ../sql/character_subclasses.sql &> /dev/null
	$MYG < ../sql/character_subclass_certification.sql &> /dev/null
	$MYG < ../sql/character_tpbookmark.sql &> /dev/null
	$MYG < ../sql/characters.sql &> /dev/null
	$MYG < ../sql/clan_data.sql &> /dev/null
	$MYG < ../sql/clan_notices.sql &> /dev/null
	$MYG < ../sql/clan_privs.sql &> /dev/null
	$MYG < ../sql/clan_skills.sql &> /dev/null
	$MYG < ../sql/clan_subpledges.sql &> /dev/null
	$MYG < ../sql/clan_wars.sql &> /dev/null
	$MYG < ../sql/clanhall.sql &> /dev/null
	$MYG < ../sql/clanhall_functions.sql &> /dev/null
	$MYG < ../sql/clanhall_sieges.sql &> /dev/null
	$MYG < ../sql/clanhall_siege_guards.sql &> /dev/null
	$MYG < ../sql/class_list.sql &> /dev/null
	$MYG < ../sql/cursed_weapons.sql &> /dev/null
	$MYG < ../sql/droplist.sql &> /dev/null
	$MYG < ../sql/enchant_skill_trees.sql &> /dev/null
	$MYG < ../sql/etcitem.sql &> /dev/null
	$MYG < ../sql/fish.sql &> /dev/null
	$MYG < ../sql/fishing_skill_trees.sql &> /dev/null
	$MYG < ../sql/fort.sql &> /dev/null
	$MYG < ../sql/fort_doorupgrade.sql &> /dev/null
	$MYG < ../sql/fort_functions.sql &> /dev/null
	$MYG < ../sql/fort_siege_guards.sql &> /dev/null
	$MYG < ../sql/fort_spawnlist.sql &> /dev/null
	$MYG < ../sql/fort_staticobjects.sql &> /dev/null
	$MYG < ../sql/fortsiege_clans.sql &> /dev/null
	$MYG < ../sql/forums.sql &> /dev/null
	$MYG < ../sql/games.sql &> /dev/null
	$MYG < ../sql/global_tasks.sql &> /dev/null
	$MYG < ../sql/gm_audit.sql &> /dev/null
	$MYG < ../sql/grandboss_spawnlist.sql &> /dev/null
	$MYG < ../sql/henna.sql &> /dev/null
	$MYG < ../sql/henna_trees.sql &> /dev/null
	$MYG < ../sql/heroes.sql &> /dev/null
	$MYG < ../sql/item_attributes.sql &> /dev/null
	$MYG < ../sql/items.sql &> /dev/null
	$MYG < ../sql/itemsonground.sql &> /dev/null
	$MYG < ../sql/lvlupgain.sql &> /dev/null
	$MYG < ../sql/merchant_buylists.sql &> /dev/null
	$MYG < ../sql/merchant_shopids.sql &> /dev/null
	$MYG < ../sql/merchants.sql &> /dev/null
	$MYG < ../sql/minions.sql &> /dev/null
	$MYG < ../sql/npc.sql &> /dev/null
	$MYG < ../sql/npcskills.sql &> /dev/null
	$MYG < ../sql/olympiad_data.sql &> /dev/null
	$MYG < ../sql/olympiad_nobles.sql &> /dev/null
	$MYG < ../sql/olympiad_nobles_eom.sql &> /dev/null
	$MYG < ../sql/petitions.sql &> /dev/null
	$MYG < ../sql/pets.sql &> /dev/null
	$MYG < ../sql/pets_skills.sql &> /dev/null
	$MYG < ../sql/pets_stats.sql &> /dev/null
	$MYG < ../sql/pledge_skill_trees.sql &> /dev/null
	$MYG < ../sql/posts.sql &> /dev/null
	$MYG < ../sql/quest_global_data.sql &> /dev/null
	$MYG < ../sql/raidboss_spawnlist.sql &> /dev/null
	$MYG < ../sql/random_spawn.sql &> /dev/null
	$MYG < ../sql/random_spawn_loc.sql &> /dev/null
	$MYG < ../sql/record.sql &> /dev/null
	$MYG < ../sql/seven_signs.sql &> /dev/null
	$MYG < ../sql/seven_signs_festival.sql &> /dev/null
	$MYG < ../sql/seven_signs_status.sql &> /dev/null
	$MYG < ../sql/siege_clans.sql &> /dev/null
	$MYG < ../sql/skill_learn.sql &> /dev/null
	$MYG < ../sql/skill_residential.sql &> /dev/null
	$MYG < ../sql/skill_spellbooks.sql &> /dev/null
	$MYG < ../sql/skill_trees.sql &> /dev/null
	$MYG < ../sql/spawnlist.sql &> /dev/null
	$MYG < ../sql/special_skill_trees.sql &> /dev/null
	$MYG < ../sql/teleport.sql &> /dev/null
	$MYG < ../sql/topic.sql &> /dev/null
	$MYG < ../sql/transform_skill_trees.sql &> /dev/null
	$MYG < ../sql/walker_routes.sql &> /dev/null
	$MYG < ../sql/weapon.sql &> /dev/null
	# L2JFree tables
	$MYG < ../sql/buff_templates.sql &> /dev/null
	$MYG < ../sql/changelog.sql &> /dev/null
	$MYG < ../sql/character_blocks.sql &> /dev/null
	$MYG < ../sql/character_mail.sql &> /dev/null
	$MYG < ../sql/character_raid_points.sql &> /dev/null
	$MYG < ../sql/couples.sql &> /dev/null
	$MYG < ../sql/ctf.sql &> /dev/null
	$MYG < ../sql/ctf_teams.sql &> /dev/null
	$MYG < ../sql/custom/custom_armor.sql &> /dev/null
	$MYG < ../sql/custom/custom_droplist.sql &> /dev/null
	$MYG < ../sql/custom/custom_etcitem.sql &> /dev/null
	$MYG < ../sql/custom/custom_merchant_buylists.sql &> /dev/null
	$MYG < ../sql/custom/custom_merchant_shopids.sql &> /dev/null
	$MYG < ../sql/custom/custom_npc.sql &> /dev/null
	$MYG < ../sql/custom/custom_npcskills.sql &> /dev/null
	$MYG < ../sql/custom/custom_spawnlist.sql &> /dev/null
	$MYG < ../sql/custom/custom_weapon.sql &> /dev/null
	$MYG < ../sql/dm.sql &> /dev/null
	$MYG < ../sql/four_sepulchers_spawnlist.sql &> /dev/null 
	$MYG < ../sql/grandboss_intervallist.sql &> /dev/null 
	$MYG < ../sql/lastimperialtomb_spawnlist.sql &> /dev/null 
	$MYG < ../sql/obj_restrictions.sql &> /dev/null
	$MYG < ../sql/tvt.sql &> /dev/null
	$MYG < ../sql/tvt_teams.sql &> /dev/null
	$MYG < ../sql/vanhalter_spawnlist.sql &> /dev/null 
	$MYG < ../sql/version.sql &> /dev/null
	$MYG < ../sql/vip.sql &> /dev/null
}

# Database update
update_db()
{
	echo "#################################################"
	echo "#                Database Update                #"
	echo "#################################################"
	echo ""
	echo "Please don't forget to make a backup before updating your database"
	echo "Do you want to proceed? (y) yes or (n) no: "
	read ANSWER
	if [ "$ANSWER" = "n" ]; then
		echo "Script aborted, make a backup before each update"
		exit 1
	else
		# Update only tables without players' data
		if [ "$1" = "noncritical" ]; then
			echo "Updating the database"
			$MYG < ../sql/armor.sql &> /dev/null
			$MYG < ../sql/armorsets.sql &> /dev/null
			$MYG < ../sql/auto_chat.sql &> /dev/null
			$MYG < ../sql/auto_chat_text.sql &> /dev/null
			$MYG < ../sql/castle_door.sql &> /dev/null
			$MYG < ../sql/castle_doorupgrade.sql &> /dev/null
			$MYG < ../sql/castle_siege_guards.sql &> /dev/null
			$MYG < ../sql/certification_skill_trees.sql &> /dev/null
			$MYG < ../sql/char_creation_items.sql &> /dev/null
			$MYG < ../sql/char_templates.sql &> /dev/null
			$MYG < ../sql/clanhall_siege_guards.sql &> /dev/null
			$MYG < ../sql/class_list.sql &> /dev/null
			$MYG < ../sql/droplist.sql &> /dev/null
			$MYG < ../sql/enchant_skill_trees.sql &> /dev/null
			$MYG < ../sql/etcitem.sql &> /dev/null
			$MYG < ../sql/fish.sql &> /dev/null
			$MYG < ../sql/fishing_skill_trees.sql &> /dev/null
			$MYG < ../sql/fort_door.sql &> /dev/null
			$MYG < ../sql/fort_doorupgrade.sql &> /dev/null
			$MYG < ../sql/fort_siege_guards.sql &> /dev/null
			$MYG < ../sql/henna.sql &> /dev/null
			$MYG < ../sql/henna_trees.sql &> /dev/null
			$MYG < ../sql/lvlupgain.sql &> /dev/null
			$MYG < ../sql/merchant_buylists.sql &> /dev/null
			$MYG < ../sql/merchant_shopids.sql &> /dev/null
			$MYG < ../sql/merchants.sql &> /dev/null
			$MYG < ../sql/minions.sql &> /dev/null
			$MYG < ../sql/npc.sql &> /dev/null
			$MYG < ../sql/npcskills.sql &> /dev/null
			$MYG < ../sql/pets_stats.sql &> /dev/null
			$MYG < ../sql/pledge_skill_trees.sql &> /dev/null
			$MYG < ../sql/raidboss_spawnlist.sql &> /dev/null
			$MYG < ../sql/random_spawn.sql &> /dev/null
			$MYG < ../sql/random_spawn_loc.sql &> /dev/null
			$MYG < ../sql/skill_learn.sql &> /dev/null
			$MYG < ../sql/skill_spellbooks.sql &> /dev/null
			$MYG < ../sql/skill_trees.sql &> /dev/null
			$MYG < ../sql/spawnlist.sql &> /dev/null
			$MYG < ../sql/teleport.sql &> /dev/null
			$MYG < ../sql/topic.sql &> /dev/null
			$MYG < ../sql/transform_skill_trees.sql &> /dev/null
			$MYG < ../sql/walker_routes.sql &> /dev/null
			$MYG < ../sql/weapon.sql &> /dev/null
			# L2JFree tables
			$MYG < ../sql/buff_templates.sql &> /dev/null
			$MYG < ../sql/ctf.sql &> /dev/null
			$MYG < ../sql/ctf_teams.sql &> /dev/null
			$MYG < ../sql/custom/custom_armor.sql &> /dev/null
			$MYG < ../sql/custom/custom_droplist.sql &> /dev/null
			$MYG < ../sql/custom/custom_etcitem.sql &> /dev/null
			$MYG < ../sql/custom/custom_merchant_buylists.sql &> /dev/null
			$MYG < ../sql/custom/custom_merchant_shopids.sql &> /dev/null
			$MYG < ../sql/custom/custom_npc.sql &> /dev/null
			$MYG < ../sql/custom/custom_npcskills.sql &> /dev/null
			$MYG < ../sql/custom/custom_spawnlist.sql &> /dev/null
			$MYG < ../sql/custom/custom_weapon.sql &> /dev/null
			$MYG < ../sql/dm.sql &> /dev/null
			$MYG < ../sql/four_sepulchers_spawnlist.sql &> /dev/null 
			$MYG < ../sql/lastimperialtomb_spawnlist.sql &> /dev/null 
			$MYG < ../sql/grandboss_intervallist.sql &> /dev/null 
			$MYG < ../sql/tvt.sql &> /dev/null
			$MYG < ../sql/tvt_teams.sql &> /dev/null
			$MYG < ../sql/vanhalter_spawnlist.sql &> /dev/null 
			$MYG < ../sql/version.sql &> /dev/null
			$MYG < ../sql/vip.sql &> /dev/null
			echo "Update completed"
		# Insert update files
		elif [ "$1" = "critical" ]; then
			echo "Updating the database"
			$MYG < ../sql/updates/il_to_ct1_update.sql
			$MYG < ../sql/updates/20080107update.sql
			$MYG < ../sql/updates/20080109update.sql
			$MYG < ../sql/updates/20080128update.sql
			$MYG < ../sql/updates/20080218update.sql
			$MYG < ../sql/updates/20080220update.sql
			$MYG < ../sql/updates/20080303update.sql
			$MYG < ../sql/updates/20080404update.sql
			$MYG < ../sql/updates/20080408update.sql
			$MYG < ../sql/updates/20080419update.sql
			$MYG < ../sql/updates/20080430update.sql
			$MYG < ../sql/updates/20080506update.sql
			$MYG < ../sql/updates/20080509update.sql
			$MYG < ../sql/updates/20080510update.sql
			$MYG < ../sql/updates/20080518update.sql
			$MYG < ../sql/updates/20080529update.sql
			$MYG < ../sql/updates/20080607update.sql
			$MYG < ../sql/updates/20080611update.sql
			$MYG < ../sql/updates/20080615update.sql
			$MYG < ../sql/updates/20080626update.sql
			$MYG < ../sql/updates/20080817update.sql
			$MYG < ../sql/updates/20080917update.sql
			$MYG < ../sql/updates/20080920update.sql
			$MYG < ../sql/updates/20080920update_1.sql
			$MYG < ../sql/updates/20081004update.sql
			$MYG < ../sql/updates/20081227update.sql
			$MYG < ../sql/updates/20090107update.sql
			$MYG < ../sql/updates/20090108update.sql
			$MYG < ../sql/updates/20090131update.sql
			$MYG < ../sql/updates/20090208update.sql
			$MYG < ../sql/updates/20090304update.sql
			$MYG < ../sql/updates/20090331update.sql
			$MYG < ../sql/updates/20090403update.sql
			$MYG < ../sql/updates/20090411update.sql
			$MYG < ../sql/updates/20090412update.sql
			$MYG < ../sql/updates/20090421update.sql
			$MYG < ../sql/updates/20090422update.sql
			$MYG < ../sql/updates/20090424update.sql
			$MYG < ../sql/updates/20090424update_1.sql
			$MYG < ../sql/updates/20090425update.sql
			$MYG < ../sql/updates/20090501update.sql
			$MYG < ../sql/updates/20090504update.sql
			$MYG < ../sql/updates/20090507update.sql
			$MYG < ../sql/updates/20090510update.sql
			$MYG < ../sql/updates/20090513update.sql
			$MYG < ../sql/updates/20090514update.sql
			$MYG < ../sql/updates/20090518update.sql
			$MYG < ../sql/updates/20090519update.sql
			$MYG < ../sql/updates/20090531update.sql
			$MYG < ../sql/updates/20090601update.sql
			$MYG < ../sql/updates/20090709update.sql
			$MYG < ../sql/updates/20090803update.sql
			$MYG < ../sql/updates/20090805update.sql
			echo "Update completed"
		# Bad argument O_o
		else
			echo "DEBUG: Wrong parameter in function update_db"
			exit 1
		fi
	fi
}

# Insert only one table the user want
table_insert()
{
	echo "#################################################"
	echo "#                 Table insertion               #"
	echo "#################################################"
	echo ""
	echo -ne "What table do you want to insert? (don't add .sql extension) "
	read TABLE
	echo "Insertion of file $TABLE"
	$MYG < ../sql/$TABLE.sql &> /dev/null
	echo "Insertion completed"
	action_type
}

# End of the script
finish()
{
	echo ""
	echo "Script execution finished."
	exit 0
}

# Clear console
clear

# Call configure function
configure

# Open MySQL connections
MYL="$MYSQLPATH -h $LSDBHOST -u $LSUSER --password=$LSPASS -D $LSDB"
MYG="$MYSQLPATH -h $GSDBHOST -u $GSUSER --password=$GSPASS -D $GSDB"

# Ask action to do
action_type