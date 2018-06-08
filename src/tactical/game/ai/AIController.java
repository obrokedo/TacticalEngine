package tactical.game.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import tactical.game.sprite.CombatSprite;

public class AIController
{
	private ArrayList<AIGroup> aiGroups;
	private Hashtable<CombatSprite, ArrayList<HealRequest>> healRequestByHealer;

	public void initialize(Iterable<CombatSprite> combatSprites)
	{

		ArrayList<CombatSprite> copyCombatSprites = new ArrayList<CombatSprite>();
		for (CombatSprite c : combatSprites)
			if (!c.isHero())
				copyCombatSprites.add(c);

		aiGroups = new ArrayList<>();

		while (copyCombatSprites.size() > 0)
		{
			HashSet<CombatSprite> groupMembers = new HashSet<CombatSprite>();
			findGroupMembers(copyCombatSprites, groupMembers, copyCombatSprites.get(0));
			aiGroups.add(new AIGroup(groupMembers));
		}
	}

	private void findGroupMembers(ArrayList<CombatSprite> css, HashSet<CombatSprite> groupMembers, CombatSprite member)
	{
		css.remove(member);
		groupMembers.add(member);

		for (int i = 0; i < css.size(); i++)
		{
			if (isWithinManhattan(member, css.get(i), 4))
			{
				findGroupMembers(css, groupMembers, css.get(i));
				i--;
			}
		}
	}

	private boolean isWithinManhattan(CombatSprite cs1, CombatSprite cs2, int maxDistance)
	{
		return Math.abs(cs1.getTileX() - cs2.getTileX()) + Math.abs(cs1.getTileY() - cs2.getTileY()) <= maxDistance;
	}

	public void performAI(CombatSprite cs)
	{

	}

	private class HealRequest
	{
		CombatSprite toHeal;
		int healValue;
	}
}
