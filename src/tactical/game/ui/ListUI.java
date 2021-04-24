package tactical.game.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.gui.AbstractComponent;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.TextField;

import tactical.game.Timer;
import tactical.utils.StringUtils;

public class ListUI implements ComponentListener {
	protected List<Button> resourceFileButtons = new ArrayList<Button>();
	protected List<String> values;
	protected int longestNameWidth = 0;
	protected Button selectedItem = null;
	protected int drawX, drawY = 0;
	protected int menuIndex = 0;
	protected boolean minimized = false;
	protected ResourceSelectorListener listener;
	protected String title;
	protected int listLength = 15;
	protected int itemSpacing = 5;
	protected int buttonHeight = 20;
	private Timer clickCooldown = new Timer(200);
	private Button upButton, downButton;
	private Font font;
	private boolean ignoreClicksInUpdate = false;
	private TextField textField;
	private String lastFilter = "";
	
	protected ListUI() {}
	
	public ListUI(GameContainer container, String title)
	{
		this(container, title, 0, new ArrayList<String>());
	}
	
	public ListUI(GameContainer container, String title, boolean setupTextField)
	{
		this(container, title, 0, new ArrayList<String>(), 15, setupTextField);
	}
	
	public ListUI(GameContainer container, String title, int drawX, List<String> values)
	{
		this(container, title, drawX, 0, values, 15, true);
	}
	
	public ListUI(GameContainer container, String title, int drawX, int drawY, List<String> values)
	{
		this(container, title, drawX, drawY, values, 15, true);
	}
	
	public ListUI(GameContainer container, String title, int drawX, List<String> values, int listLength, boolean setupTextField) {
		this(container, title, drawX, 0, values, listLength, setupTextField);
	}

	public ListUI(GameContainer container, String title, int drawX, int drawY, List<String> values, int listLength, boolean setupTextField)
	{
		longestNameWidth = 150;
		this.font = StringUtils.loadFont("Times New Roman", 14, false, false);
		for (String s : values) {
			int width = this.font.getWidth(s);
			if (width > longestNameWidth)
				longestNameWidth = width;
		}
		longestNameWidth += 10;
		this.listLength = listLength;
		this.drawX = drawX;
		this.drawY = drawY;
		
		this.title = title;
		this.values = values;
		for (String value : values)
			this.resourceFileButtons.add(new Button(drawX, 0, longestNameWidth, buttonHeight, value));
		this.layoutItems();
		this.setupDirectionalButtons();
		
		if (setupTextField)
			initTextField(container);
	}
	
	protected void initTextField(GameContainer container) {		
		this.textField = new TextField(container, container.getDefaultFont(), drawX + 50,  Math.min(listLength, values.size()) * buttonHeight + drawY + 25 +  
					Math.min(listLength, values.size()) * itemSpacing, 100, 20);
		this.textField.setBackgroundColor(Color.white);
		this.textField.setTextColor(Color.black);
		this.textField.setBorderColor(Color.blue);
		this.textField.addListener(this);
	}
	
	protected void setupDirectionalButtons() {
		this.upButton = new Button(drawX + longestNameWidth, drawY + 25, buttonHeight, buttonHeight, "^");
		this.downButton = new Button(drawX + longestNameWidth, 
				(listLength - 1) * buttonHeight + drawY + 25 + (listLength - 1) * itemSpacing, 
				buttonHeight, buttonHeight, "v");
		
		upButton.setVisible(menuIndex > 0);
		downButton.setVisible(menuIndex < resourceFileButtons.size() - listLength);
	}


	public void render(GameContainer container, Graphics g)
	{
		g.setFont(font);
		if (!minimized)
		{
			g.drawString(title, drawX + 5, drawY);
			for (int i = 0; i < Math.min(listLength, resourceFileButtons.size()); i++)
			{
				Button button = resourceFileButtons.get(i + menuIndex);
				button.render(g);
			}

			
			if (resourceFileButtons.size() > listLength)
			{
				upButton.render(g);
				downButton.render(g);
			}
			
			textField.render(container, g);
			g.drawString("Search", textField.getX()- 50, textField.getY());
		}
		else
		{
			g.setColor(Color.blue);
			g.fillRect(drawX + 0, 30, longestNameWidth, 25);

			g.setColor(Color.white);

			g.drawString(title, drawX + 5, 35);
		}

		g.setColor(Color.white);
	}
	
	public void update(GameContainer container, int delta) {		
		int x = container.getInput().getMouseX();
		int y = container.getInput().getMouseY();
		this.clickCooldown.update(delta);
		
		boolean clicked = container.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON) && 
				clickCooldown.perform() && !ignoreClicksInUpdate;
		
		handleInput(x, y, clicked);
		
		String fil = textField.getText();
		if (!fil.equalsIgnoreCase(lastFilter)) {
			this.filter(textField.getText());
			lastFilter = fil;
		}
	}

	public void handleInput(int x, int y, boolean clicked) {
		for (Button button : resourceFileButtons)
		{
			if (button.handleUserInput(x, y, clicked))
			{
				if (listener != null)
				{
					if (!listener.resourceSelected(button.getText(), this))
						this.selectedItem = null;
					else
						this.selectedItem = button;
				}
				else
					this.selectedItem = button;
				
				
					
				break;
			}
			
			if (selectedItem == button)
				button.setForegroundColor(Color.red);
			else
			{
				button.setForegroundColor(Color.white);
			}
		}			
		
		if (upButton.handleUserInput(x, y, clicked))
		{
			menuIndex--;
			
			upButton.setVisible(menuIndex > 0);
			downButton.setVisible(menuIndex < resourceFileButtons.size() - listLength);
			this.layoutItems();
		}
		else if (downButton.handleUserInput(x, y, clicked))
		{
			menuIndex++;
			
			upButton.setVisible(menuIndex > 0);
			downButton.setVisible(menuIndex < resourceFileButtons.size() - listLength);
			this.layoutItems();
		}		
	}

	public String getSelectedResource()
	{
		if (selectedItem == null)
			return null;
		return selectedItem.getText();
	}

	public interface ResourceSelectorListener
	{
		public boolean resourceSelected(String selectedItem, ListUI parentSelector);
	}

	public void setListener(ResourceSelectorListener listener) {
		this.listener = listener;
	}

	public void setSelectedIndex(int selectedIndex) {
		if (selectedIndex < 0 || selectedIndex > resourceFileButtons.size())
			this.selectedItem = null;
		else
			this.selectedItem = resourceFileButtons.get(selectedIndex);
	}
	
	protected void layoutItems()
	{		
		for (int buttonIndex = 0; buttonIndex < resourceFileButtons.size(); buttonIndex++) {
			Button button = this.resourceFileButtons.get(buttonIndex);
			button.setX(drawX);
			if (buttonIndex < menuIndex)
			{
				button.setVisible(false);
			}
			else if (buttonIndex >= menuIndex && buttonIndex  < menuIndex + listLength)
			{
				button.setY((buttonIndex - menuIndex) * buttonHeight + drawY + 25 + (buttonIndex - menuIndex) * itemSpacing);
				button.setVisible(true);
			}
			else
			{
				button.setVisible(false);
			}
		}
	}
	
	public void filter(String filter) {
		this.resourceFileButtons.clear();
		this.menuIndex = 0;
		for (String value : values.stream().filter(v -> v.toUpperCase().startsWith(filter.toUpperCase())).collect(Collectors.toList()))
			this.resourceFileButtons.add(new Button(drawX, 0, longestNameWidth, buttonHeight, value));
		layoutItems();
	}

	public void setListLength(int listLength) {
		this.listLength = listLength;
	}

	public boolean isIgnoreClicksInUpdate() {
		return ignoreClicksInUpdate;
	}

	public void setIgnoreClicksInUpdate(boolean ignoreClicksInUpdate) {
		this.ignoreClicksInUpdate = ignoreClicksInUpdate;
	}	
	
	public void registerListeners(GameContainer container) {
		container.getInput().addKeyListener(textField);
		container.getInput().addMouseListener(textField);
		container.getInput().addControllerListener(textField);
	}
	
	public void unregisterListeners(GameContainer container) {
		container.getInput().removeKeyListener(textField);
		container.getInput().removeMouseListener(textField);
		container.getInput().removeControllerListener(textField);
	}

	@Override
	public void componentActivated(AbstractComponent source) {
		
	}
}
