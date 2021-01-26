package tactical.game.ui;

import java.io.File;

import org.newdawn.slick.GameContainer;

import tactical.utils.DirectoryLister;

public class ResourceSelector extends ListUI {
	public ResourceSelector(String title, int drawX,
			boolean fromLeft, String rootFolder, 
			String suffix, GameContainer gc)
	{
		super(gc, title, false);
		if (!fromLeft)
			this.drawX = gc.getWidth() - longestNameWidth - drawX;
		else
			this.drawX = drawX;
		this.drawY = 0;

		addResourceFromDir(rootFolder, suffix, gc);
		this.initTextField(gc);
	}

	public void addResourceFromDir(String rootFolder, String suffix, GameContainer gc) {
		for (File file : DirectoryLister.listFilesInDir(rootFolder))
		{
			if (file.isDirectory() || file.getName().startsWith("."))
				continue;

			if (file.getName().endsWith(suffix))
			{
				resourceFileButtons.add(new Button(0, 0, 0, buttonHeight, file.getName()));
				values.add(file.getName());
				int width = gc.getDefaultFont().getWidth(file.getName());

				if (width + 15 > longestNameWidth)
					longestNameWidth = width + 15 ;
			}
		}

		for (Button button : resourceFileButtons)
			button.setWidth(longestNameWidth);
		
		this.layoutItems();
		this.setupDirectionalButtons();
		
	}
}
