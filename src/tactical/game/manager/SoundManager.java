package tactical.game.manager;

import org.newdawn.slick.Music;
import org.newdawn.slick.MusicListener;
import org.newdawn.slick.Sound;

import tactical.engine.TacticalGame;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.IntMessage;
import tactical.engine.message.Message;

public class SoundManager extends Manager implements MusicListener
{
	private Music playingMusic;
	private Music introMusic;
	private float playingVolume;
	private String playingMusicName;
	private float playingMusicPosition;
	private float musicPosition = 0f;
	
	public static float GLOBAL_VOLUME = 1.0f; 

	public void update(int delta)
	{
		/*
		if (playingMusic != null && playingMusic.playing())
		{
			musicPosition = musicPosition + delta / 1000.0f;
		}
		*/
	}

	@Override
	public void initialize() {

	}

	public void playSoundByName(String name, float volume)
	{
		if (name == null)
			return;
		Sound sound = stateInfo.getResourceManager().getSoundByName(name);
		if (sound != null)
			sound.play(1f, volume * GLOBAL_VOLUME);
	}

	public void playMusicByName(String name, float volume, float position)
	{
		if (name.equalsIgnoreCase(playingMusicName) && playingMusic.playing()) {
			return;
		}
		
		Music playingMusic = stateInfo.getResourceManager().getMusicByName(name);
		playingMusic.stop();
		playingMusic.setPosition(position);
		playingMusic.loop(1, 0);
		playingMusic.fade(2000, volume * GLOBAL_VOLUME, false);
		this.playingMusicName = name;
		this.playingVolume = volume;
		this.playingMusic = playingMusic;
	}

	public void pauseMusic()
	{
		if (playingMusic != null)
		{
			this.playingMusicPosition = this.playingMusic.getPosition();
			playingMusic.stop();
		}
	}

	public void resumeMusic()
	{
		if (playingMusic != null)
		{
			playMusicByName(playingMusicName, playingVolume, playingMusicPosition);
		}
	}

	public void stopMusic()
	{
		if (playingMusic != null)
		{
			playingMusic.stop();
			playingMusic = null;
		}
	}

	public void fadeMusic(int duration)
	{
		if (playingMusic != null)
		{
			playingMusic.fade(duration, 0f, true);
		}
	}

	@Override
	public void recieveMessage(Message message)
	{
		if (TacticalGame.MUTE_MUSIC)
			return;
		
		switch (message.getMessageType())
		{
			case SOUND_EFFECT:
				AudioMessage am = (AudioMessage) message;
				playSoundByName(am.getAudio(), am.getVolume());
				break;
			case PAUSE_MUSIC:
				pauseMusic();
				break;
			case RESUME_MUSIC:
				resumeMusic();
				break;
			case PLAY_MUSIC:
				am = (AudioMessage) message;
				playMusicByName(am.getAudio(), am.getVolume(), am.getPosition());
				break;
			case FADE_MUSIC:
				IntMessage im = (IntMessage) message;
				fadeMusic(im.getValue());
				break;
			default:
				break;
		}
	}

	@Override
	public void musicEnded(Music music) {

	}

	@Override
	public void musicSwapped(Music music, Music newMusic) {

	}
}
