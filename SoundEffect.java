import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;

public enum SoundEffect {
    EAT_FOOD("Audio/womp.wav"),
    EXPLODE("Audio/cine 2.wav"),
    DIE("Audio/patrick.wav"),
    BACKGROUND("Audio/Background.wav");

    /** Volume control (0.0f = mute, 1.0f = full volume) */
    private static float volume = 0.7f;

    public static void setGlobalVolume(float value) {
        volume = Math.max(0f, Math.min(value, 1f)); // Clamp between 0.0 and 1.0
        for (SoundEffect sfx : values()) {
            sfx.updateVolume();
        }
    }

    /** Each sound effect has its own clip */
    public Clip clip;

    private final String soundFileName;

    SoundEffect(String soundFileName) {
        this.soundFileName = soundFileName;
        loadClip();
    }

    private void loadClip() {
        try {
            URL url = getClass().getClassLoader().getResource(soundFileName);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            updateVolume(); // Apply initial volume
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound: " + soundFileName);
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip == null) return;

        if (clip.isRunning())
            clip.stop();
        clip.setFramePosition(0);

        if (this == BACKGROUND) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            clip.start();
        }
    }

    private void updateVolume() {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            float gain = min + (max - min) * volume;
            gainControl.setValue(gain);
        }
    }

    public static void stopAll() {
        for (SoundEffect sfx : values()) {
            if (sfx.clip != null && sfx.clip.isRunning()) {
                sfx.clip.stop();
            }
        }
    }

    public static void initGame() {
        values(); // Load all clips
    }
}
