package tactical.engine.config.intr;

import java.io.IOException;

import tactical.utils.SpriteAnims;

public interface AnimationParser {
	public SpriteAnims parseAnimations(String animsFile) throws IOException;
}
