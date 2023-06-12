package tactical.engine.config.intr;

import java.io.IOException;
import java.util.Hashtable;

import org.newdawn.slick.SlickException;

import tactical.utils.SpriteAnims;

public interface AnimationParser {
	public SpriteAnims parseAnimations(String animsFile) throws IOException;
	public void parseAnimationsDirectory(Hashtable<String, SpriteAnims> spriteAnims, String animsFile) throws IOException, SlickException;
}
