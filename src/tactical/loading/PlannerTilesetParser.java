package tactical.loading;

import java.awt.image.BufferedImage;
import java.util.Hashtable;

import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;

import tactical.map.Map;
import tactical.utils.ImageUtility;

public class PlannerTilesetParser extends TilesetParser {
	@Override
	public void parseTileset(String image, Color trans, int tileWidth,
			int tileHeight, int startIndex, Map map,
			Hashtable<Integer, Integer> landEffectByTileId, float resize)
			throws SlickException {
		PlannerMap pm = (PlannerMap) map;
		BufferedImage bim = ImageUtility.loadBufferedImage(image);
		if (resize != 1)
			bim = ImageUtility.toBufferedImage(bim.getScaledInstance((int) (bim.getWidth() * resize), (int) (bim.getHeight() * resize), BufferedImage.SCALE_SMOOTH));
		ImageUtility.makeColorTransparent(bim, new java.awt.Color(trans.getRed(), trans.getGreen(), trans.getBlue()));
		BufferedImage[] bims = ImageUtility.splitImage(bim, bim.getWidth() / tileWidth, bim.getHeight() / tileHeight);
		pm.addTileset(bims, startIndex, tileWidth, tileHeight, landEffectByTileId);
	}
}
