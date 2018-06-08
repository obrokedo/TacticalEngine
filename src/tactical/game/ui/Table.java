package tactical.game.ui;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Rectangle;

import tactical.game.hudmenu.Panel;

public class Table<T> 
{
	private ArrayList<T> items;
	private Rectangle itemSelectRect;
	private Rectangle tablePerimiter;
	private Rectangle scrollBarRect;
	private Rectangle itemUpRect;
	private Rectangle itemDownRect;
	private ArrayList<Line> columnLines;
	private int mouseOverItems = -1;
	private int itemOffset = 0;
	private T selectedItem = null;
	private String[] columnHeaders;
	private int itemsPerScreen;
	private CellRenderer<T> renderer;
	
	public Table(int x, int y, int[] columnWidths, String[] columnHeaders, int itemsPerScreen, Iterable<T> items, CellRenderer<T> renderer)
	{
		int tableWidth = 0;
		int tableHeight = (itemsPerScreen + 1) * 30;
		columnLines = new ArrayList<Line>();
		for (int i = 0; i < columnWidths.length; i++)
		{
			tableWidth += columnWidths[i];
			if (i + 1 < columnWidths.length)
			{
				columnLines.add(new Line(tableWidth + x, y, tableWidth + x, y + tableHeight));
			}
		}
		columnLines.add(new Line(x, y + 30, x + tableWidth + 15, y + 30));
		itemSelectRect = new Rectangle(x, y + 30, tableWidth, tableHeight - 30);
		tablePerimiter = new Rectangle(x, y, tableWidth + 15, tableHeight);
		scrollBarRect = new Rectangle(x + tableWidth, y + 31, 15, tableHeight - 30);
		itemUpRect = new Rectangle(x + tableWidth, y + 31, 15, 15);
		itemDownRect = new Rectangle(x + tableWidth, y + tableHeight - 15, 15, 15);
		this.itemsPerScreen = itemsPerScreen;
		
		this.columnHeaders = columnHeaders;
		this.items = new ArrayList<>();
		for (T i : items)
			this.items.add(i);
		this.renderer = renderer;
	}
	
	public T handleUserInput(int mouseX, int mouseY, boolean leftClick)
	{
		if (leftClick && itemUpRect.contains(mouseX, mouseY))
			itemOffset = Math.max(itemOffset - 1, 0);
		else if (leftClick && items.size() > itemsPerScreen && itemDownRect.contains(mouseX, mouseY))
			itemOffset = Math.min(itemOffset + 1, items.size() - itemsPerScreen);
		
		// Select Heroes
		if (itemSelectRect.contains(mouseX, mouseY))
		{
			int over =  ((int)(mouseY - itemSelectRect.getY()) / 30);
			
			if (over < items.size())
			{
				mouseOverItems= over;
				if (leftClick)
				{
					selectedItem = items.get(mouseOverItems + itemOffset);
					return selectedItem;
				}
			}
		}
		
		else
			mouseOverItems = -1;
		return null;
	}
	
	public void render(GameContainer gc, Graphics graphics)
	{
		// Draw Mouse Over
		if (mouseOverItems != -1)
		{
			graphics.setColor(Panel.COLOR_MOUSE_OVER);
			graphics.fillRect(itemSelectRect.getX(), itemSelectRect.getY() + (mouseOverItems * 30), itemSelectRect.getWidth(), 30);			
		}
		
		// Draw the items
		graphics.setColor(Panel.COLOR_FOREFRONT);
		for (int i = 0; i <  Math.min(items.size(), itemsPerScreen); i++)
		{
			if (items.get(i + itemOffset) == selectedItem)
			{
				graphics.setColor(Panel.COLOR_MOUSE_OVER);
				graphics.fillRect(itemSelectRect.getX(), itemSelectRect.getY() + i * 30, itemSelectRect.getWidth(), 30);
				graphics.setColor(Panel.COLOR_FOREFRONT);
			}
			
			for (int j = 0; j < columnLines.size() + 1; j++)
			{
				if (j == 0)
					graphics.drawString(renderer.getColumnValue(items.get(i + itemOffset), j, graphics), 
							tablePerimiter.getX() + 7, tablePerimiter.getY() + 35 + i * 30);
				else
					graphics.drawString(renderer.getColumnValue(items.get(i + itemOffset), j, graphics),  
							columnLines.get(j - 1).getX1() + 7, tablePerimiter.getY() + 35 + i * 30);
			}	
		}
		
		graphics.setColor(Panel.COLOR_FOREFRONT);
		Panel.drawRect(tablePerimiter, graphics);
		for (Line l : columnLines)
			graphics.draw(l);
		graphics.setColor(Color.lightGray);
		Panel.fillRect(scrollBarRect, graphics);
		graphics.setColor(Color.darkGray);
		Panel.fillRect(itemUpRect, graphics);
		Panel.fillRect(itemDownRect, graphics);
		graphics.setColor(Panel.COLOR_FOREFRONT);	
		graphics.drawString("^", itemUpRect.getX() + 1, itemUpRect.getY() + 1);
		graphics.drawString("v", itemDownRect.getX() + 2, itemDownRect.getY() - 2);
		
		// Draw Column Headers
		graphics.drawString(columnHeaders[0], tablePerimiter.getX() + 7, tablePerimiter.getY() + 5);
		for (int i = 1; i < columnHeaders.length; i++)
		{
			graphics.drawString(columnHeaders[i], columnLines.get(i - 1).getX1() + 7, tablePerimiter.getY() + 5);
		}				
		
		graphics.setColor(Color.red);		
	}
	
	public void setItems(ArrayList<T> items)
	{
		this.items = items;
		this.itemOffset = 0;
		this.mouseOverItems = -1;
		this.selectedItem = null;
	}
}
