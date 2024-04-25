package com.rodak.termitaire;

import javax.sound.sampled.*;
import java.io.*;

public class SoundManager {

    public enum Sound {
        CardUp,
        CardDown,
        ShuffleStock,
        Victory
    }

    private final Clip[] clips;

    public SoundManager(String soundsFolder, String soundsFormat) {
        clips = new Clip[Sound.values().length];

        for (Sound sound : Sound.values()) {
            String clipPath = soundsFolder + "/" + sound.name() + soundsFormat;
            try (final InputStream in = getClass().getResourceAsStream(clipPath);
                 final BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
                 final AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedInputStream)) {
                final Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clips[sound.ordinal()] = clip;
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                System.out.println("Did not find '" + ColoredString.colorizeString(sound.name() + soundsFormat, ColoredString.Color.RED) + "'");
            }
        }
    }

    public void play(Sound sound) {
        if (GameSettings.getInstance().getSetting("audio/mute").getBoolVal()) return;
        Clip clip = clips[sound.ordinal()];
        if (clip != null && !clip.isRunning()) {
            clip.setFramePosition(0);
            float volume = (float) (GameSettings.getInstance().getSetting("audio/volume").getDoubleVal());
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(20f * (float) Math.log10(volume));
            clip.start();
        }
    }

    public void dispose() {
        for (Clip clip : clips) {
            if (clip != null) {
                clip.close();
            }
        }
    }
}
