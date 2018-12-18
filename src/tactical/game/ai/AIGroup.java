package tactical.game.ai;

import java.util.ArrayList;
import java.util.Collection;

import tactical.engine.state.StateInfo;
import tactical.game.manager.TurnManager;
import tactical.game.move.MoveableSpace;
import tactical.game.sprite.CombatSprite;

public class AIGroup {
	enum Phase
	{
		JOIN,
		SKIRMISH,
		INDIVIDUAL,
		RETREAT
	}

	private Phase phase;
	private ArrayList<CombatSprite> members;
	private CombatSprite target;

	// HONOR HEAL REQUESTS ALWAYS
	// KILL ALWAYS
	public AIGroup(Collection<CombatSprite> members)
	{
		phase = Phase.JOIN;
		this.members = new ArrayList<CombatSprite>(members);
	}

	public void addMember(CombatSprite member)
	{
		members.add(member);
	}

	public void performAI(StateInfo stateInfo, MoveableSpace ms, CombatSprite currentSprite)
	{
		// Check to see if we can kill someone
		// Check to see if we need to heal people
		AIConfidence aiConf = currentSprite.getAi().getBestConfidence(stateInfo, ms, currentSprite, null);
		if (aiConf.willKill || aiConf.willHeal)
		{

		}


		switch (phase)
		{
			case JOIN:
				// Move to an agreed upon central point.
				// If everyone is already there then swap to skirmish mode
				break;
			case SKIRMISH:
				// Communicate with other groups to figure out our ultimate group location?
				// Attempt to move towards that location while also staying obnoxiously close to the heroes
				// If we're in our correct "sector" then it's time to engage in cases where we will all be able to attack first
				// If we've found a target we can all reach then set it as our target and then switch to invidual mode
				break;
			case INDIVIDUAL:
				// Check if we need to be healed (THIS MAY NEED TO BE IN GENERAL AI)
				// Determine who is the best target for your group
				// Perform you're individual AI with a bias with attacking the target
				// If we're becoming outnumbered or getting low then have a chance to retreat to regroup
				break;
			case RETREAT:
				// While we're outnumbered/hurt attempt to get away which may case the heroes to seperate
				// If the hero's have separated then engage
				// If not then heal and try to get back into the correct position
		}
	}
}
