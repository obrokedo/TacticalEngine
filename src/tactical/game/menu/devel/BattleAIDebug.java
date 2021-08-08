package tactical.game.menu.devel;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import tactical.engine.message.MessageType;
import tactical.engine.message.SpriteContextMessage;
import tactical.engine.message.TurnActionsMessage;
import tactical.engine.state.StateInfo;
import tactical.game.ai.AI;
import tactical.game.ai.AIConfidence;
import tactical.game.ai.AIController;
import tactical.game.ai.AIGroup;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.manager.TurnManager;
import tactical.game.sprite.CombatSprite;
import tactical.game.ui.PaddedGameContainer;
import tactical.utils.StringUtils;

public class BattleAIDebug {	
	private List<byte[]> layouts = new ArrayList<>();
	 
	private List<AIConfidence> debugConfidences;
	private TurnManager turnManager;
	private StateInfo stateInfo;
	
	public BattleAIDebug(TurnManager tm, StateInfo si) {
		this.turnManager = tm;
		this.stateInfo = si;
	}
	
	public void renderDebugConfidences(Graphics graphics) {
		if (debugConfidences != null && turnManager.getCurrentSprite() != null) {
			AIController aiController = turnManager.getAiController();
			Optional<AIGroup> optAIGroup = aiController.getCombatantGroup(turnManager.getCurrentSprite());
			
			graphics.setColor(Color.white);
			
			String approach = null;
			switch (turnManager.getCurrentSprite().getAi().getApproachType()) {
			case 0:
				approach = "Reactive";
				break;
			case 1:
				approach = "Kamikazee";
				break;
			case 2:
				approach = "Hesitant";
				break;
			case 3:
				approach = "Follow";
				break;
			case 4:
				approach = "Move to point";
				break;
			case 5:
				approach = "Approach target";
				break;
			case AI.APPROACH_WANDER:
				approach = "Wander";
			}
			
			if (approach != null)
				StringUtils.drawString(approach, 3, PaddedGameContainer.GAME_SCREEN_SIZE.height - 8, graphics);			
			StringUtils.drawString("Vision: " + turnManager.getCurrentSprite().getAi().getVision(), 3, PaddedGameContainer.GAME_SCREEN_SIZE.height - 16, graphics);
			
			if (optAIGroup.isPresent()) {
				AIGroup aiGroup = optAIGroup.get();
				for (CombatSprite cs : aiGroup.getMembers()) { 
					StringUtils.drawString("G", (int) (cs.getLocX() - stateInfo.getCamera().getLocationX() + stateInfo.getCurrentMap().getTileEffectiveWidth() * .75f),
							(int) (cs.getLocY() - stateInfo.getCamera().getLocationY() + stateInfo.getCurrentMap().getTileEffectiveHeight() * .75f), graphics);
				}
			}
			
			Hashtable<java.awt.Point, Integer> amtPerSpace = new Hashtable<>();
			for (AIConfidence aic : debugConfidences) {
				int amt = 0;
				
				if (aic.attackPoint != null) {
					graphics.setColor(Color.green);
					float x = aic.attackPoint.x * stateInfo.getCurrentMap().getTileEffectiveWidth() - 
							stateInfo.getCamera().getLocationX();
					float y = aic.attackPoint.y * stateInfo.getCurrentMap().getTileEffectiveHeight() - 
							stateInfo.getCamera().getLocationY();
					graphics.drawRect(x, y, stateInfo.getCurrentMap().getTileEffectiveWidth(), stateInfo.getCurrentMap().getTileEffectiveHeight());
					graphics.setColor(Color.white);
										
					if (amtPerSpace.containsKey(aic.attackPoint)) {
						amt = amtPerSpace.get(aic.attackPoint);
					}
					
					if (aic.confidence != Integer.MIN_VALUE)
						StringUtils.drawString("" + aic.confidence, (int) x + (10 * (amt / 4)), (int) y + (amt % 4) * 6, graphics);
					else
						StringUtils.drawString("L", (int) x + (10 * (amt / 4)), (int) y + (amt % 4) * 6, graphics);
					if (aic.target != null) {
						graphics.setColor(Color.orange);
						float tx = aic.target.getLocX() -  stateInfo.getCamera().getLocationX();
						float ty = aic.target.getLocY() -  stateInfo.getCamera().getLocationY();
						graphics.drawRect(tx, ty, 
								stateInfo.getCurrentMap().getTileEffectiveWidth(), stateInfo.getCurrentMap().getTileEffectiveHeight());
						graphics.drawRect(tx + 1, ty + 1, 
								stateInfo.getCurrentMap().getTileEffectiveWidth() - 2, stateInfo.getCurrentMap().getTileEffectiveHeight() - 2);
					}
					
					amtPerSpace.put(aic.attackPoint, (amt + 1));
				}
			}
		}	
	}
	

	
	public boolean handleDebugKeyboardInput(UserInput input) {
		if (input.isKeyDown(KeyMapping.BUTTON_3))
		{
			stateInfo.removeKeyboardListeners();
			stateInfo.sendMessage(new TurnActionsMessage(false, 
					turnManager.getCurrentSprite().getAi().performAI(stateInfo, turnManager.getMoveableSpace(), 
							turnManager.getCurrentSprite())), true);
			return true;
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_2))
		{
			debugConfidences = new ArrayList<>();
			turnManager.getCurrentSprite().getAi().performAI(stateInfo, turnManager.getMoveableSpace(), 
					turnManager.getCurrentSprite(), debugConfidences);
			return true;
		}
		return false;
	}
	
	public boolean handleDebugInput(Input input, boolean debugIsShown) {
		if (debugIsShown) {
			if (debugConfidences != null && input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
				int mx = (int)(input.getMouseX() / PaddedGameContainer.GAME_SCREEN_SCALE + stateInfo.getCamera().getLocationX()) / stateInfo.getCurrentMap().getTileEffectiveWidth();
				int my = (int)(input.getMouseY()  / PaddedGameContainer.GAME_SCREEN_SCALE + stateInfo.getCamera().getLocationY()) / stateInfo.getCurrentMap().getTileEffectiveWidth();
				JPanel panel = null;
				for (AIConfidence aic : debugConfidences) {
					if (aic.attackPoint != null && aic.attackPoint.x == mx && aic.attackPoint.y == my) {
						if (panel == null) {
							panel = new JPanel();
							panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
							
							String approach = null;
							/*
							 *  public final static int APPROACH_REACTIVE = 0;
								public final static int APPROACH_KAMIKAZEE = 1;
								public final static int APPROACH_HESITANT = 2;
								public final static int APPROACH_FOLLOW = 3;
								public final static int APPROACH_MOVE_TO_POINT = 4;
								public final static int APPROACH_TARGET = 5;
							 */
							switch (turnManager.getCurrentSprite().getAi().getApproachType()) {
								case 0:
									approach = "Reactive";
									break;
								case 1:
									approach = "Kamikazee";
									break;
								case 2:
									approach = "Hesitant";
									break;
								case 3:
									approach = "Follow";
									break;
								case 4:
									approach = "Move to point";
									break;
								case 5:
									approach = "Approach target";
									break;
							}
							panel.add(new JLabel("Approach: " + approach));
						}
						JLabel label = new JLabel("<html><body style='width: 600px'>" + aic.toString().replaceAll(" ", "<br>") + "</body></html>");
						panel.add(label);					
					}
				}
				
				if (panel != null) {
					JFrame debugFrame = new JFrame("AI Debug");
					debugFrame.setContentPane(panel);
					debugFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					debugFrame.setMinimumSize(new Dimension(600, 400));
					debugFrame.pack();
					debugFrame.setVisible(true);
					debugFrame.toFront();
				}
			}
		}
		
		if (input.isKeyPressed(Input.KEY_COMMA)) {
			this.rewind();
			return true;
		}
		return false;
	}
	
	public void determineConfidences() {
		debugConfidences = new ArrayList<>();
		turnManager.getCurrentSprite().getAi().performAI(stateInfo, turnManager.getMoveableSpace(), 
				turnManager.getCurrentSprite(), debugConfidences);
	}
	
	public void clearDebugConfidences() {
		debugConfidences = null;
	}
	
	public boolean acceptingInput() {
		return debugConfidences != null;
	}
	
	public void turnStart() {
		try {
			BattleLayout layout = new BattleLayout(new ArrayList<CombatSprite>(stateInfo.getCombatSprites()),
					stateInfo.getCurrentSprite());
			layouts.add(0, layout.serialize(layout));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (layouts.size() > 5)
			layouts.remove(5);
	}
	
	public void rewind() {
		if (layouts.size() > 1) {
			// First one is the one we just added
			layouts.remove(0);
			byte[] bytes = layouts.remove(0);
			BattleLayout layout;
			try {
				layout = BattleLayout.deserializeBattleLayout(bytes);
				stateInfo.getSprites().removeAll(stateInfo.getCombatSprites());
				stateInfo.getCombatSprites().clear();
				layout.combatSprites.forEach(cs -> cs.initializeSprite(stateInfo.getResourceManager()));
				stateInfo.addAllCombatSprites(layout.combatSprites);
				stateInfo.sendMessage(new SpriteContextMessage(MessageType.COMBATANT_TURN, layout.combatSprites.get(layout.currentTurn)), true);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
