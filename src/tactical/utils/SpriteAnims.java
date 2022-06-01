package tactical.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import javax.imageio.ImageIO;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;

import tactical.engine.config.AnimationConfiguration;
import tactical.game.constants.Direction;
import tactical.game.exception.BadAnimationException;
import tactical.game.exception.BadResourceException;

public class SpriteAnims
{
	protected Hashtable<String, Animation> animations;
	private String spriteSheet;
	public ArrayList<Rectangle> imageLocs;
	public transient ArrayList<Image> images;

	public SpriteAnims(String spriteSheet, ArrayList<Rectangle> imageLocs)
	{
		animations = new Hashtable<String, Animation>();
		this.imageLocs = imageLocs;
		this.spriteSheet = spriteSheet;
	}

	public void addAnimation(String name, Animation anim)
	{
		animations.put(name, anim);
	}

	public void initialize(Image image)
	{
		images = new ArrayList<Image>();

		for (int i = 0; i < imageLocs.size(); i++)
		{
			Rectangle r = imageLocs.get(i);
			Image subImage = image.getSubImage((int) r.getX(), (int) r.getY(), (int) r.getWidth(),
					(int) r.getHeight());
			subImage.setFilter(Image.FILTER_NEAREST);
			images.add(subImage);
		}
	}

	public boolean hasAnimation(String name)
	{
		return animations.get(name) != null;
	}

	public boolean hasCharacterAnimation(String name, boolean isPromoted)
	{
		return animations.get((isPromoted ? AnimationConfiguration.getPromotedPrefix() : AnimationConfiguration.getUnpromotedPrefix()) + name) != null;
	}

	public Animation getCharacterAnimation(String name, boolean isPromoted)
	{	
		Animation a = animations.get((isPromoted ? AnimationConfiguration.getPromotedPrefix() : AnimationConfiguration.getUnpromotedPrefix()) + name);
		if (a == null)
			throw new BadResourceException("Unable to find animation: " +
					(isPromoted ? AnimationConfiguration.getPromotedPrefix() : AnimationConfiguration.getUnpromotedPrefix()) + name + " for animation using spritesheet: " + spriteSheet);
		return a;
	}
	
	public Animation getDirectionAnimation(Direction dir) {
		String animString = null;
		if (hasAnimation(animString = AnimationConfiguration.getUnpromotedDirection(dir)))
			return getAnimation(animString);
		else if (hasAnimation(animString = AnimationConfiguration.getPromotedDirection(dir)))
			return getAnimation(animString);
		else 
			throw new BadAnimationException("Unable to get character direction animation for dir " + dir + " on spritesheet " + 
					spriteSheet +". Neither a unpromoted or promoted animation has been defined");
	}
	
	public Animation getCharacterDirectionAnimation(Direction dir, boolean isHeroPromoted) {
		switch (dir)
		{
			case UP:
				return getCharacterAnimation(AnimationConfiguration.getUpAnimationName(), isHeroPromoted);
			case DOWN:
				return getCharacterAnimation(AnimationConfiguration.getDownAnimationName(), isHeroPromoted);
			case LEFT:
				return getCharacterAnimation(AnimationConfiguration.getLeftAnimationName(), isHeroPromoted);
			case RIGHT:
				return getCharacterAnimation(AnimationConfiguration.getRightAnimationName(), isHeroPromoted);
		}
		throw new BadAnimationException("Unable to get character direction animation for dir " + dir + " on spritesheet " + spriteSheet);
	}

	public Animation getAnimation(String name)
	{
		Animation a = animations.get(name);
		if (a == null)
			throw new BadResourceException("Unable to find animation: " +
					name + " for animation using spritesheet: " + spriteSheet);
		return a;
	}

	public Image getImageAtIndex(int idx)
	{
		return images.get(idx);
	}

	public String getSpriteSheet() {
		return spriteSheet;
	}

	public void printAnimations()
	{
		System.out.println("-- Print Animations --");
		for (String a : animations.keySet())
			System.out.println(a);
	}

	public Set<String> getAnimationKeys()
	{
		return animations.keySet();
	}
	
	public void dumpToFolders() throws IOException {				
		for (Animation a : animations.values()) {
			Hashtable<Integer, Integer> oldImIndexToNew = new Hashtable<>();
			int indexCnt = 0;	
			
			StringBuffer spriteSB = new StringBuffer();		
			StringBuffer animSB = new StringBuffer();
			StringBuffer weaponSB = new StringBuffer();
			String path = "animations/animationsheets/" + spriteSheet;
			if (a.name.startsWith(AnimationConfiguration.getUnpromotedPrefix())) {
				path += "/unpromoted";
				a.name = a.name.replaceFirst(AnimationConfiguration.getUnpromotedPrefix(), "");
			}
			else if (a.name.startsWith(AnimationConfiguration.getPromotedPrefix())) {
				path += "/promoted";
				a.name = a.name.replaceFirst(AnimationConfiguration.getPromotedPrefix(), "");
			}		
			
			path += "/" + a.name + "/";
			
			animSB.append(String.format("<animations spriteSheet=\"%s.sprites\" ver=\"1.2\">\n", a.name));
			animSB.append(String.format("<anim name=\"%s\" loops=\"0\">\n", a.name));
			weaponSB.append(String.format("<animations spriteSheet=\"%s.sprites\" ver=\"1.2\">\n", a.name));
			weaponSB.append(String.format("<anim name=\"%s\" loops=\"0\">\n", a.name));
			
			spriteSB.append("<definitions>\n");
			spriteSB.append("<dir name=\"/\">\n");
			int nextX = 0;
			
			int cellIdx = 0;
			
			int maxHeight = 0;
			boolean hasWeapon = false;
			boolean hasSprites = false;
			
			// Dump animation here:
			for (AnimFrame af : a.frames) {			
				animSB.append(String.format("\t<cell index=\"%d\" delay=\"%d\">\n", cellIdx, af.delay));
				weaponSB.append(String.format("\t<cell index=\"%d\" delay=\"%d\">\n", cellIdx, af.delay));
				for (AnimSprite as : af.sprites) {
					hasSprites = true;
					// Remap old indexes
					if (as.imageIndex != -1 && !oldImIndexToNew.containsKey(as.imageIndex)) {
						spriteSB.append(String.format("<spr name=\"Frame%d\" x=\"%d\" y=\"0\" w=\"%d\" h=\"%d\"/>\n", 
								indexCnt, 
								nextX, 
								(int) imageLocs.get(as.imageIndex).getWidth(),
								(int) imageLocs.get(as.imageIndex).getHeight()));
							nextX += (int) imageLocs.get(as.imageIndex).getWidth();
						
						oldImIndexToNew.put(as.imageIndex, indexCnt++);		
						
						maxHeight = (int) Math.max(maxHeight, imageLocs.get(as.imageIndex).getHeight());
					}
					
					if (as.imageIndex != -1) {
						animSB.append(String.format("\t\t<spr name=\"/%s\" x=\"%d\" y=\"%d\" z=\"1\" angle=\"%d\" flipV=\"%d\" flipH=\"%d\"/>\n",
							"Frame" + oldImIndexToNew.get(as.imageIndex),
							(int) (as.x + imageLocs.get(as.imageIndex).getWidth() / 2),
							(int) (as.y + imageLocs.get(as.imageIndex).getHeight() / 2),
							as.angle,
							as.flipV ? 1 : 0,
							as.flipH ? 1 : 0));
					} else {
						weaponSB.append(String.format("\t\t<spr name=\"/%s\" x=\"%d\" y=\"%d\" z=\"1\" angle=\"%d\" flipV=\"%d\" flipH=\"%d\"/>\n",
								"Weapon",
								(int) (as.x + 60),
								(int) (as.y + 15),
								as.angle,
								as.flipV ? 1 : 0,
								as.flipH ? 1 : 0));
						hasWeapon = true;
					}
					
				}	
				animSB.append("\t</cell>\n");
				weaponSB.append("\t</cell>\n");
				
				cellIdx++;
			}
			
			if (!hasSprites)
				continue;
			
			animSB.append("</anim>\n");
			animSB.append("</animations>\n");
			
			weaponSB.append("</anim>\n");
			weaponSB.append("</animations>\n");
			
			spriteSB.append("</dir>\n");
			spriteSB.append("</definitions>\n");
			spriteSB.append("</img>\n");
			
			Files.createDirectories(Paths.get(path));
			
			BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(path + a.name + ".anim")));
			bwr.write(animSB.toString());
			bwr.close();
			
			if (hasWeapon) {
				bwr = new BufferedWriter(new FileWriter(new File(path + a.name + "-weapon.anim")));
				bwr.write(weaponSB.toString());
				bwr.close();
			}
			
			bwr = new BufferedWriter(new FileWriter(new File(path + a.name + ".sprites")));
			bwr.write(String.format("<img name=\"%s.png\" w=\"%d\" h=\"%d\">\n", a.name, nextX, maxHeight) + spriteSB.toString());
			bwr.close();
			
			// Dump image to anim folder
			// Dump animation as only entry
			
			java.awt.image.BufferedImage fullImage = ImageIO.read(new File("animations/animationsheets/" + this.getSpriteSheet() + ".png"));
			java.awt.image.BufferedImage newImage = new BufferedImage(Math.max(1, nextX), Math.max(1, maxHeight), BufferedImage.TYPE_INT_ARGB);
			
			// Iterate back through the sprites to copy them over to the new image now that we know its size
			Graphics2D newGraphics = newImage.createGraphics();
			nextX = 0;
			for (AnimFrame af : a.frames) {				
				for (AnimSprite as : af.sprites) {								
					if (as.imageIndex != -1) {
						
						java.awt.image.BufferedImage subImage = fullImage.getSubimage((int) imageLocs.get(as.imageIndex).getX(), 
								(int) imageLocs.get(as.imageIndex).getY(), 
								(int) imageLocs.get(as.imageIndex).getWidth(),
								(int) imageLocs.get(as.imageIndex).getHeight());
						newGraphics.drawImage(subImage, nextX, 0, null);
						
						
						nextX += (int) imageLocs.get(as.imageIndex).getWidth();											
					}
				}
			}
			
			ImageIO.write(newImage, "png", new File(path + a.name + ".png"));
			
			
			newGraphics.dispose();			
		}
	}
}
