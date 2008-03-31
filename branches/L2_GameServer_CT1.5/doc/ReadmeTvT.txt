///////////////////////////////////////////////////////////////////////////
Description:
This engine is for Team vs. Team pvp. You can create unlimited
amount of teams. All teams vs. all teams at the same time. The team
with the most kills win and each player of it get the setted reward.
///////////////////////////////////////////////////////////////////////////
Features:
Configuration for even teams inn extensions.properties
Participation via NPC
Players do not drop item if they die
Players do not loose expirience if they die
Admin/GM can handle if player can stand up
Players can attack/cast without pressign CTRL
If player die he is revived and teleported to team spot in 20 seconds
Players do not get karma or pvp flag
Players do not get PvP/PK points
Configure all settings via TvT panel(only one time needed)
On teleport players karma is set to 0 and original karma is saved
On teleport players color is set to team color and original color is saved
On finish saved karma is restored
On finish saved name color is restored
///////////////////////////////////////////////////////////////////////////
Usage:
Set you even teams option in extensions,properties:
TvTEvenTeams=NO|BALANCE|SHUFFLE

NO means: not even teams.
BALANCE means: Players can only join team with lowest player count.
SHUFFLE means: Players can only participate to tzhe event and not direct to a team. Teams will be schuffeled in teleporting teams.


In TvT Panel:
Set name(input1), description(input1), join location(input1)

npc(input1[id]), npc pos(no input)
reward(input1[id]), reward amount(input1)

team add(input1), team remove(input1)
team pos(no input), team color(input1,input2[color,teamname])

join(no input), start(no input)
finish(no input), abort(no input)

teleport(no input), sit force(no input), dump(no input)

1. Set all infos(first 4 button blocks)
2. Join
3. Teleport
4. Start
5. Finish

Now if you want to start the next TvT match you just have to start with
step 2! (After each restart you have to set the infos again)
///////////////////////////////////////////////////////////////////////////
