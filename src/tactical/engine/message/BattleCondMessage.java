package tactical.engine.message;

/**
 * A message that indicates that the current battle has custom battle conditions
 *
 * @author Broked
 *
 */
public class BattleCondMessage extends Message
{
	private static final long serialVersionUID = 1L;

	private int[] leaderIds;
	private int[] enemyLeaderIds;
	private boolean killAllLeaders;

	public BattleCondMessage(int[] leaderIds, int[] enemyLeaderIds, boolean killAllLeaders) {
		super(MessageType.BATTLE_COND);
		this.leaderIds = leaderIds;
		this.enemyLeaderIds = enemyLeaderIds;
		this.killAllLeaders = killAllLeaders;
	}

	public int[] getLeaderIds() {
		return leaderIds;
	}

	public int[] getEnemyLeaderIds() {
		return enemyLeaderIds;
	}

	public boolean isKillAllLeaders() {
		return killAllLeaders;
	}
}
