package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2DefaultTransformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class InquisitorShilienElder extends L2DefaultTransformation
{
	public InquisitorShilienElder()
	{
		// id, colRadius, colHeight
		super(318, 8.0, 25.0);
	}

	public void onTransform(L2PcInstance player)
	{
		if (player.getTransformationId() != getId() || player.isCursedWeaponEquipped())
			return;
		
		// Update transformation ID into database and player instance variables.
		player.transformInsertInfo();

		// Switch Stance
		addSkill(player, 838, 1);
		// Decrease Bow/Crossbow Attack Speed
		addSkill(player, 5491, 1);		
		
		// give transformation skills
		transformedSkills(player);
	}
	
	public void onUntransform(L2PcInstance player)
	{
		// Switch Stance
		removeSkill(player, 838);
		// Decrease Bow/Crossbow Attack Speed
		removeSkill(player, 5491);
		
		// remove transformation skills
		removeSkills(player);
	}	

	public void transformedSkills(L2PcInstance player)
	{
		if (player.getLevel() > 43)
		{
			int level = player.getLevel() - 43;
			addSkill(player, 1523, level); // Divine Punishment
			addSkill(player, 1524, level); // Surrender to the Holy
			addSkill(player, 1525, level); // Divine Curse
			addSkill(player, 1528, level); // Divine Flash
			addSkill(player, 1043, 1); // Holy Weapon
		}
		player.addTransformAllowedSkill(new int[]{1430,1303,1059});
	}

	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 1523); // Divine Punishment
		removeSkill(player, 1524); // Surrender to the Holy
		removeSkill(player, 1525); // Divine Curse
		removeSkill(player, 1528); // Divine Flash
		removeSkill(player, 1043); // Holy Weapon
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new InquisitorShilienElder());
	}
}