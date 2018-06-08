package tactical.game.ui;

import java.io.File;

import org.newdawn.slick.GameContainer;

import tactical.utils.DirectoryLister;

public class ResourceSelector extends ListUI {
	public ResourceSelector(String title, int drawX,
			boolean fromLeft, String rootFolder, 
			String suffix, GameContainer gc)
	{
		super(title);
		resourceFileButtons.clear();

		for (File file : DirectoryLister.listFilesInDir(rootFolder))
		{
			if (file.isDirectory() || file.getName().startsWith("."))
				continue;

			if (file.getName().endsWith(suffix))
			{
				resourceFileButtons.add(new Button(0, 0, 0, buttonHeight, file.getName()));
				int width = gc.getDefaultFont().getWidth(file.getName());

				if (width > longestNameWidth)
					longestNameWidth = width;
			}
		}
		longestNameWidth += 15;

		for (Button button : resourceFileButtons)
			button.setWidth(longestNameWidth);
		
		if (fromLeft)
			this.drawX = drawX;
		else
			this.drawX = gc.getWidth() - longestNameWidth - drawX;
		this.drawY = 0;

		this.title = title;
		this.layoutItems();
		this.setupDirectionalButtons();
	}
}
