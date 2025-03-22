package tactical.engine.state.devel;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

import lombok.Getter;
import tactical.engine.state.StateInfo;
import tactical.game.persist.ClientProfile;
import tactical.game.persist.ClientProgress;

public class SaveState implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Getter
	private ClientProgress clientProgress;
	
	@Getter
	private ClientProfile clientProfile;
	
	public void loadToState(StateInfo stateInfo) {
		stateInfo.getPersistentStateInfo().setClientProfile(clientProfile);
		stateInfo.getPersistentStateInfo().setClientProgress(clientProgress);
		stateInfo.initState();
	}
	
	public static SaveState createSaveState(StateInfo stateInfo) {
		SaveState ss = new SaveState();
		// On the off-chance this is a "flashing" enemy we want to make sure they
		// aren't saved while invisible
		if (stateInfo.getCurrentSprite() != null)
			stateInfo.getCurrentSprite().setVisible(true);
		try {
			
			stateInfo.getClientProfile().convertJythonToSerialized();
			
			ss.clientProfile = deepCopy(stateInfo.getClientProfile());
			ss.clientProgress = deepCopy(stateInfo.getClientProgress());
			// Deep copy the save location so that combat sprites are decoupled
			// Create the save location from the stateinfos client progress so that
			// transient data isn't lost (mapData field)
			ss.clientProgress.setLastSaveLocation(deepCopy(
					stateInfo.getClientProgress().createBattleSaveLocation(stateInfo.getCombatSprites(), stateInfo.getCurrentSprite())));
			
			
			
		// Hope nothing bad ever happens
		} catch (Exception e) {
			e.printStackTrace();
			try {
				stateInfo.getClientProfile().serializeToFile("BLAP", true);
				ClientProfile cp = ClientProfile.deserializeFromFile("BLAP");
				System.out.println("ASDASD");
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			return null;
		}
		
		return ss;
	}
	
	private static <T> T deepCopy(T toCopy) throws Exception {
		//Serialization of object
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    ObjectOutputStream out = new ObjectOutputStream(bos);
	    out.writeObject(toCopy);

	    //De-serialization of object
	    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
	    ObjectInputStream in = new ObjectInputStream(bis);
	    @SuppressWarnings("unchecked")
		T copied = (T) in.readObject();
		
	    return copied;
	}
	
	public static LinkedList<SaveState> loadSaveStates(File selectedFile) throws Exception {									
		InputStream file = new FileInputStream(selectedFile);
		InputStream buffer = new BufferedInputStream(file);
		ObjectInput input = new ObjectInputStream (buffer);
		Object saveStates = input.readObject();
		file.close();
		return (LinkedList<SaveState>) saveStates;
	}
}	
