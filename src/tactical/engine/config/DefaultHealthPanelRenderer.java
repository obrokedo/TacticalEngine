package tactical.engine.config;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.UnicodeFont;

import tactical.engine.TacticalGame;
import tactical.game.hudmenu.Panel;
import tactical.game.sprite.CombatSprite;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.ResourceManager;
import tactical.utils.StringUtils;

public class DefaultHealthPanelRenderer implements HealthPanelRenderer {
	public void displayHealthPanel(ResourceManager fcrm, CombatSprite sprite, UnicodeFont panelFont, 
			PaddedGameContainer gc, Graphics graphics, PanelLocation position)
	{
		// Determine panel width by max hp of entity
		int width = 75;
		int healthWidth = (int) (Math.min(100, sprite.getMaxHP()) * 1.48);
		int mpWidth = (int) (Math.min(100, sprite.getMaxMP()) * 1.48);
		width = Math.max(width, 17 + healthWidth + StringUtils.getStringWidth(sprite.getMaxHP() + " / " + sprite.getMaxHP(), panelFont));
		width = Math.max(width, 17 + mpWidth + StringUtils.getStringWidth(sprite.getMaxMP() + " / " + sprite.getMaxMP(), panelFont));
		if (sprite.isHero())
		{
			width = Math.max(width, StringUtils.getStringWidth(sprite.getName() + " Lv " +
					sprite.getLevel(), panelFont) + 15);
		}
		else
			width = Math.max(width, StringUtils.getStringWidth(sprite.getName(), panelFont) + 15);
		
		int height = 25 + (sprite.getMaxMP() != 0 ? 10 : 0);
		int x = 0;
		int y = 0;

		switch (position) {
			case HERO_HEALTH:
				x = PaddedGameContainer.GAME_SCREEN_SIZE.width - width - 4;
				y = 5;
			break;
			case ENEMY_HEALTH:
				x = 5;
				y = PaddedGameContainer.GAME_SCREEN_SIZE.height - height - 4;
			break;
			case TARGET_HEALTH:
				x = 5;
				y = 5;
			break;
		}

		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(x, y, width, height, graphics, null);
		graphics.setColor(Panel.COLOR_FOREFRONT);
		if (sprite.isHero()) {
			StringUtils.drawString(sprite.getName() + " Lv " +
				sprite.getLevel(), x + 7, y - 5, graphics);
		}
		else
			StringUtils.drawString(sprite.getName(), x + 7, y - 5, graphics);
		
		// Draw health bars
		int largestBarWidth = Math.max(healthWidth, mpWidth) + x + 23;
		
		int maxStatDigits = Math.max(("" + sprite.getMaxHP()).length(), ("" + sprite.getMaxMP()).length());
		
		defaultStatBar("HP", sprite.getCurrentHP(), sprite.getMaxHP(), maxStatDigits, healthWidth, largestBarWidth, x + 7, y + 3, graphics, sprite.isHero());
		if (sprite.getMaxMP() != 0)
			defaultStatBar("MP", sprite.getCurrentMP(), sprite.getMaxMP(), maxStatDigits, mpWidth, largestBarWidth, x + 7, y + 12, graphics, sprite.isHero());
	}
	
	private void defaultStatBar(String statName, int currStat, int maxStat, int maxDigits,int barWidth, 
			int xValueCoord, int xCoord, int yCoord, Graphics graphics, boolean isHero)
	{
		graphics.setColor(Color.white);
		StringUtils.drawString(statName, xCoord, yCoord, graphics);
		String currStatStr = "" + currStat;
		String maxStatStr = "" + maxStat;
		while (currStatStr.length() < maxDigits)
			currStatStr = " " + currStatStr;
		while (maxStatStr.length() < maxDigits)
			maxStatStr = " " + maxStatStr;
		
		if (maxStat > 100 && !isHero)
			maxStatStr = "???";
		if (currStat > 100 && !isHero)
			currStatStr = "???";
		
		StringUtils.drawString(currStatStr + "/" + maxStatStr,
				xValueCoord + 1, yCoord, graphics);
		
		graphics.setColor(Color.red);
		graphics.fillRoundRect(xCoord + 15, yCoord + 10, barWidth, 7, 2);
		Color[] colors = new Color[] {Color.yellow, Color.blue, Color.green, Color.black};
		int barIndex = 0;
		do
		{
			graphics.setColor(colors[Math.min(3, barIndex)]);
			graphics.fillRoundRect(xCoord + 15, yCoord + 10, (int) (Math.min(100, Math.max(0, (currStat - barIndex * 100))) * 1.48), 7, 2);
			barIndex++;
		}
		while (currStat - (barIndex * 100) > 0);
	}
}
