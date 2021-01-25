package tactical.game.dev;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.game.sprite.CombatSprite;

public class BattleOptimizer 
{
	private static int plusAll = 0;
	private static int wonAtPlus;
	private static int lostAtPlus;
	private static float threshold = .5f;
	private static int checkPermutation = 0;
	private final static int BATTLES_PER = 5;
	private static int bestPermutation = 0;
	private static float bestPermutationResult = 2f;
	private static int bestPermPlusAll = -1;
	private static boolean battleDone = false;
	
	private int[][] permutations = {
			{0, 0, 0, 0},
			{1, 0, 0, 0},
			{0, 1, 0, 0},
			{0, 0, 1, 0},
			{0, 0, 0, 1},
			{1, 1, 0, 0},
			{1, 0, 1, 0},
			{1, 0, 0, 1},
			{0, 1, 1, 0},
			{0, 1, 0, 1},
			{0, 0, 1, 1},
			{1, 1, 1, 0},
			{1, 0, 1, 1},
			{1, 1, 0, 1},
			{0, 1, 1, 1}}; 
	
	public void startBattle() {
		battleDone = false;
	}
	
	public void modifyStats(CombatSprite cs)
	{
		cs.setMaxHP(cs.getMaxHP() + plusAll);
		cs.setMaxAttack(cs.getMaxAttack() + plusAll);
		cs.setMaxDefense(cs.getMaxDefense() + plusAll);
		cs.setMaxSpeed(cs.getMaxSpeed() + plusAll);
		
		if (checkPermutation != -1)
		{
			cs.setMaxHP(cs.getMaxHP() + permutations[checkPermutation][0]);
			cs.setMaxAttack(cs.getMaxAttack() + permutations[checkPermutation][1]);
			cs.setMaxDefense(cs.getMaxDefense() + permutations[checkPermutation][2]);
			cs.setMaxSpeed(cs.getMaxSpeed() + permutations[checkPermutation][3]);
		}
	}
	
	public static void render(Graphics g) {
		g.setColor(Color.red);
		g.drawString("Permutation: " + checkPermutation , 100, 50);
		g.drawString("W " + wonAtPlus + " L " + lostAtPlus , 100, 70);
		g.drawString("Plus All: " + plusAll, 100, 90);
		g.drawString("Best " + bestPermPlusAll + " " + bestPermutationResult, 100, 110);
	}
	public void lostBattle()
	{
		if (battleDone)
			return;
		battleDone = true;
		this.lostAtPlus++;
		this.endBattle();
	}
	
	public void wonBattle()
	{
		if (battleDone)
			return;
		battleDone = true;
		this.wonAtPlus++;
		this.endBattle();
	}
	
	private void endBattle()
	{
		if (wonAtPlus + lostAtPlus == BATTLES_PER)
		{
			try
			{
				PrintWriter pw;
				float winRate = (1.0f * wonAtPlus / BATTLES_PER);
				pw = new PrintWriter(new FileWriter("Results", true));
				
				
				
				pw.write("Result was plus all: " + plusAll + " permutation " + checkPermutation + " with win percent " + winRate + "\n");
				
				if (winRate >= threshold)
				{
					if (winRate < bestPermutationResult)
					{
						bestPermutation = checkPermutation;
						bestPermutationResult = winRate;
						bestPermPlusAll = plusAll;
					}
				}
				
				checkPermutation++;		
				wonAtPlus = 0;
				lostAtPlus = 0;
				
				if (checkPermutation == permutations.length) {
					if (bestPermPlusAll == plusAll) {
						pw.write("A better permutation was found at this plus level, increasing plus\n");
						plusAll++;						
						checkPermutation = 0;
					} else {
						pw.write("Best result was plus all: " + bestPermPlusAll + " permutation " + bestPermutation + " with win percent " + bestPermutationResult + "\n");
						pw.flush();
						pw.close();
						System.exit(0);
					}
				}
				pw.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
}
