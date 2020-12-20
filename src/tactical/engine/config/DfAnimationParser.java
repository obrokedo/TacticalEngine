package tactical.engine.config;

import java.io.IOException;
import java.util.ArrayList;

import org.newdawn.slick.geom.Rectangle;

import tactical.engine.config.intr.AnimationParser;
import tactical.game.exception.BadResourceException;
import tactical.utils.AnimFrame;
import tactical.utils.AnimSprite;
import tactical.utils.Animation;
import tactical.utils.SpriteAnims;
import tactical.utils.XMLParser;
import tactical.utils.XMLParser.TagArea;

public class DfAnimationParser implements AnimationParser {
	public SpriteAnims parseAnimations(String animsFile) throws IOException
	{
		// Parse Animations
		 ArrayList<TagArea> rootTags = XMLParser.process(animsFile, false);
		 ArrayList<String> imageNames = new ArrayList<>();
		 ArrayList<Rectangle> imageLocs = new ArrayList<>();
		 SpriteAnims sa = null;

		 for (TagArea ta : rootTags)
		 {
			 if (ta.getTagType().equalsIgnoreCase("animations"))
			 {
				 String imageLocation = null;
				 try
				 {
					 imageLocation =
						 parseSprites("/animations/animationsheets/" + ta.getAttribute("spriteSheet"), imageNames, imageLocs);
				 }
				 catch (Exception e)
				 {
					 try {
						 imageLocation =
								 parseSprites("/animations/weaponanim/" + ta.getAttribute("spriteSheet"), imageNames, imageLocs);
					 } catch (Exception e2) {
						 throw new BadResourceException("An error occurred while attempting to load the animation file " + animsFile + "\n"
					 		+ "The specified sprite sheet " + "/animations/animationsheets/" + ta.getAttribute("spriteSheet") + " does not exist.\n"
					 		+ "Check the animationsheets folder to verify that the file exists.\n"
					 		+ "Keep in mind that the names ARE case-sensitive");
					 }
				 }

				 sa = new SpriteAnims(imageLocation, imageLocs);

				 for (TagArea animTag : ta.getChildren())
				 {
					 String name = animTag.getAttribute("name");
					 Animation animation = new Animation(name);

					 for (TagArea frameTag : animTag.getChildren())
					 {
						 AnimFrame animFrame = new AnimFrame(Integer.parseInt(frameTag.getAttribute("delay")));

						 for (TagArea spriteTag : frameTag.getChildren())
						 {
							 int x = Integer.parseInt(spriteTag.getAttribute("x"));
							 int y = Integer.parseInt(spriteTag.getAttribute("y"));
							 int index = -1;
							 Rectangle imageSize;
							 if (!spriteTag.getAttribute("name").equalsIgnoreCase("/weapon"))
							 {
								 index = imageNames.indexOf(spriteTag.getAttribute("name"));
								 if (index == -1)
									 throw new BadResourceException("Unable to parse the animation " + name +
										 ". One of the frames had a bad sprite index " + spriteTag.getAttribute("name"));
								 imageSize = imageLocs.get(index);
							 }
							 else
								 imageSize = new Rectangle(0, 0, 112, 24);

							 x -= imageSize.getWidth() / 2;
							 y -= imageSize.getHeight() / 2;

							 int angle = 0;
							 if (spriteTag.getAttribute("angle") != null)
								 angle = (int) Float.parseFloat(spriteTag.getAttribute("angle"));

							 int flipH = 0;
							 if (spriteTag.getAttribute("flipH") != null)
								 flipH = Integer.parseInt(spriteTag.getAttribute("flipH"));

							 int flipV = 0;
							 if (spriteTag.getAttribute("flipV") != null)
								 flipV = Integer.parseInt(spriteTag.getAttribute("flipV"));

							 if (animFrame.sprites.size() > 0)
								 animFrame.sprites.add(0, new AnimSprite(x, y, index, angle, flipH == 1, flipV == 1));
							 else
								 animFrame.sprites.add(new AnimSprite(x, y, index, angle, flipH == 1, flipV == 1));
						 }

						 animation.frames.add(animFrame);
					 }

					 sa.addAnimation(name, animation);
				 }
			 }
		 }

		 if (sa == null)
			 throw new BadResourceException("Unable to parse the animation file " + animsFile);

		 return sa;
	}

	private static String parseSprites(String spritesFile, ArrayList<String> imageNames, ArrayList<Rectangle> imageLocs) throws IOException
	{
		ArrayList<TagArea> rootTags = XMLParser.process(spritesFile, false);

		// Parse Sprites
		for (TagArea ta : rootTags)
		{
			if (ta.getTagType().equalsIgnoreCase("img"))
			{
				String spriteSheetName = ta.getAttribute("name");
				spriteSheetName = spriteSheetName.replace("../animationsheets/", "");
				String spriteSheet = spriteSheetName.split("\\.")[0];
				
				// Find the first sprite entry in the directory structure
				while (true)
				{
					if (ta.getTagType().equalsIgnoreCase("dir")) {
						getSpritesRecursive(ta, "", imageNames, imageLocs);
						break;
					}
					if (ta.getChildren() != null && ta.getChildren().size() > 0) {
						ta = ta.getChildren().get(0);
					} else {
						break;
					}
				}

				
				if (imageNames.size() == 0)
					throw new BadResourceException("Unable to parse .sprites file " + spritesFile + ". Could not find any defined sprites");

				return spriteSheet;
			}
		}

		return null;
	}
	
	private static void getSpritesRecursive(TagArea ta, String dirName, ArrayList<String> imageNames, ArrayList<Rectangle> imageLocs) {		
		if (ta.getTagType().equalsIgnoreCase("dir"))
		{
			dirName += ta.getAttribute("name");
			if (ta.getChildren() != null && ta.getChildren().size() > 0)
				for (TagArea childTagArea : ta.getChildren())
					getSpritesRecursive(childTagArea, dirName, imageNames, imageLocs);
			return;
		}
		
		// Once we've found a sprite in a directory, get all of the sprites from that dir
		if (ta.getTagType().equalsIgnoreCase("spr"))
		{
			if (!dirName.endsWith("/"))
				dirName += "/";
			imageNames.add(dirName + ta.getAttribute("name"));
			imageLocs.add(new Rectangle(Integer.parseInt(ta.getAttribute("x")),
									Integer.parseInt(ta.getAttribute("y")),
									Integer.parseInt(ta.getAttribute("w")),
									Integer.parseInt(ta.getAttribute("h"))));

		}
	}
}
