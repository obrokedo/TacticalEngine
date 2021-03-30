package tactical.game.persist;

import java.awt.Point;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EgressLocation implements Serializable {
	// This value is the map that was last loaded from, the priest was saved at or
	// an egress location was set
	private String lastSaveMapData;
	private Point inTownPoint;
	private String inTownLocation;
}
