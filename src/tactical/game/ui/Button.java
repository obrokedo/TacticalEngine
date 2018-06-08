package tactical.game.ui;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import tactical.game.hudmenu.Panel;
import tactical.utils.StringUtils;

public class Button 
{
	private Rectangle rect;
	private boolean mouseOver;
	private String text;
	private boolean displayBorder = true;
	private boolean enabled = true;
	private boolean visible = true;
	private Color foreground = Color.white;
	private boolean scaleText = false;
	
	public Button(int x, int y, int width, int height, String text) {
		this(x, y, width, height, text, false);
	}
	
	public Button(int x, int y, int width, int height, String text, boolean scaleText) {
		super();
		this.rect = new Rectangle(x, y, width, height);
		this.mouseOver = false;
		this.text = text;
		this.scaleText = scaleText;
	}

	public boolean handleUserInput(int mouseX, int mouseY, boolean leftClick)
	{
		if (!visible)
			return false;
		
		if (enabled && rect.contains(mouseX, mouseY))
		{
			mouseOver = true;
			if (leftClick)
				return true;
		}
		else
			mouseOver = false;
		return false;
	}
	
	public void render(Graphics graphics)
	{
		if (!visible)
			return;
		
		if (mouseOver)
		{
			graphics.setColor(Panel.COLOR_MOUSE_OVER);				
			Panel.fillRect(rect, graphics);
		}
		else {
			graphics.setColor(new Color(45, 45, 45));
			Panel.fillRect(rect, graphics);
		}
		
		if (enabled)
			graphics.setColor(foreground);
		else
			graphics.setColor(Color.lightGray);
		if (displayBorder)
			Panel.drawRect(rect, graphics);
		if (scaleText)
			StringUtils.drawString(text, (int) rect.getX() + 5, (int) rect.getY() + 2, graphics);
		else
			graphics.drawString(text, (int) rect.getX() + 5, (int) rect.getY() + 2);
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public void displayBorder(boolean displayBorder)
	{
		this.displayBorder = displayBorder;
	}
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getText() {
		return text;
	}
	
	public void setWidth(int width) {
		this.rect.setWidth(width);
	}
	
	public void setX(int x) {
		this.rect.setX(x);
	}
	
	public void setY(int y) {
		this.rect.setY(y);
	}
	
	public void setForegroundColor(Color color) {
		this.foreground = color;
	}
}
