-------------------------------------------------- VIP ENGINE - by CubicVirtuoso and the iGO Team -----------------------------------------------------

------------------
--- LEGALITIES ---
------------------

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

http://www.gnu.org/copyleft/gpl.html


------------------
--- THANKS -------
------------------

Very special thanks goes out the L2J first and for most, also thanks goes out to FBIAgent and his TVT Engine. A lot of this code stems from his, it helpd me greatly in programming the VIP Engine. Thanks so much FBIAgent and keep up the good work!!!


------------------
--- ABOUT --------
------------------

The VIP engine is an engine that I wrote to enable a new level of events in L2J, mainly PVP servers. With the help testing help of iGO I was able to complete the core within a day. 

Basically the idea is this. The admins/gms setup all the parameters of the engine (for instructions see the Instructions part) and they either randomly choose a race or choose one themselves. The event is setup like this. Everyone who wishes to join can participate, lets say for example the race choosen was Dark Elf. All Dark Elfs will join the dark elf team and everyone else will be automatically assigned to the opposing team. 

When the event starts the Dark Elves are teleported to an area outside their starter city defined by the SQL table. Everybody else is teleported in the starter town next to a certain NPC. Then at start time one player from the Dark Elf team is randomly choosen to be the VIP. The VIP must make it to the NPC without dying before the time runs out. The team must protect their VIP, because if the VIP dies the other team wins. If a non-vip player dies he will be teleported back to their initial spawn location.

------------------
--- INSTALLATION -
------------------

- Place VIP.java in the net.sf.l2j.gameserver.model.entity package
- Place the AdminVIPEngine.java in the net.sf.l2j.gameserver.handler.admincommandhandlers package
- Run the patch on your entire L2J checkout
- Run the SQL script on your SQL database to import the tables
- IF you are using command privilages insert the following at the end of your command-privileges.properties file:

admin_vip = 100
admin_vip_setteam = 100
admin_vip_randomteam = 100
admin_vip_settime = 100
admin_vip_endnpc = 100
admin_vip_setdelay = 100
admin_vip_joininit = 100
admin_vip_joinnpc = 100
admin_vip_joinlocxyz = 100
admin_vip_setarea = 100
admin_vip_vipreward = 100
admin_vip_viprewardamount = 100
admin_vip_notvipreward = 100
admin_vip_notviprewardamount = 100
admin_vip_thevipreward = 100
admin_vip_theviprewardamount = 100

--------------------
--- INSTRUCTIONS ---
--------------------

- After installing the engine go ahead and enter game on a GM character with 100 access.
- Type //vip and fill in the following settings in the proper order. (Some settings are dependent on other settings to be filled in so they must be done in order; this is to save computation time)

--SET TEAM--
- INPUT1: You can do this one of two ways; the easiest way is to select a random team by hitting the random team button. It will cycle through random numbers and choose a team for you. Alternativly you can choose a team yourself by putting a team name in Input 1 and hitting Set Team. Valid teams are: human/dark/elf/orc/dwarf. This is not case sensitive
- After choosing a team the Code will go through the SQL databases and plot the X,Y,Z of the start and finish locations as per which team you have choosen.

--SET TIME--
- INPUT1: The time variable defines how long the event will last after it starts. This is in mins.

--SET DELAY--
- INPUT1: The delay variable defines how long the join period lasts. This is in mins.

--SET AREA--
- INPUT1: This is a formality, you can really put any string. It is used to inform the players where the join NPC is located. EX. Giran Square etc.

--JOIN LOC--
- This is a x,y,z location X -> Input1, Y -> Input2, Z -> Input3. This is where the join NPC spawns.

--END NPC--
- INPUT1: This is a valid NPC id for which the End NPC will be choosen from. You can pick any preexisting NPC or create your own.

--END NPC--
- INPUT1: This is a valid NPC id for which the Join NPC will be choosen from. You can pick any preexisting NPC or create your own.

--REWARDS--
- INPUT1: The next six variables define the rewards and their respective ammounts or each of the two teams and finally the actual VIP Player. It would be smart to give the Not VIP team less valuable rewards than that of the VIP team; since its harder for the VIP team to win.

- After filling in the variables above you are now able to hit the Start Join button. The event will take over from there.
- Players will locate the Join NPC and hit join, the NPC will automatically check their race and assign them respectivly
- After the join period is finish they will be teleported after 20 seconds.
- After they are teleported they will be forced to sit for another 20 seconds
- The event will start and end if the VIP makes it to the NPC, VIP dies or the time runs out
- At the end of the event all values will clear and you are ready for another go!

--------------------
--- CUSTOMIZATION --
--------------------

If you are smart with code I'm sure you can customize anything you wanted in the event, but I'll talk more about the SQL customizations. The values in the SQL table have been carefully choosen to give each race the same advantage, but you can change them if you wish. 

Each team is assigned a value (1: Human, 2: Elf, 3: Dark, 4: Orc, 5: Dwarf), this differs from L2j where 0: Human because I used 0 as a null value. In the SQL tables you will see an entry for each race. You can edit the start and end x,y,z respectivly to move around the posistions to your liking. The engine will do the rest.

If you do decide to do some code changes feel free to share your code on the L2J Forums. I would be more than willing to look it over and if its good apply it! L2J is all about the SHARE!!!!

--------------------
--- CONTACT --------
--------------------

It would be best to post questions about the engine in the L2J forums in the respective topic, however feel free to email me at cubicvirtuoso@gmail.com.

-------------------------------------------------- VIP ENGINE - by CubicVirtuoso and the iGO Team -----------------------------------------------------

- Thanks and have fun... remember being greedy is not the way. Keep software free!!
- l2j.clanigo.com

-------------------------------------------------- VIP ENGINE - by CubicVirtuoso and the iGO Team -----------------------------------------------------