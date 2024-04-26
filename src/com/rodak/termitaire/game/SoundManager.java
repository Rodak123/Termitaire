package com.rodak.termitaire.game;

import com.rodak.termitaire.game.settings.GameSettings;
import com.rodak.termitaire.ui.ColoredString;

import javax.sound.sampled.*;
import java.io.*;

public class SoundManager {

    private enum SoundType {
        SFX("audio/sfx"),
        Music("audio/music"),
        ;

        private final String settingKey;

        SoundType(String settingKey) {
            this.settingKey = settingKey;
        }
    }

    public enum Sound {
        CardUp(SoundType.SFX),
        CardDown(SoundType.SFX),
        ShuffleStock(SoundType.SFX),
        Victory(SoundType.Music),
        Startup(SoundType.Music),
        ;

        private final SoundType type;

        Sound(SoundType type) {
            this.type = type;
        }
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
            float masterVolume = (float) (GameSettings.getInstance().getSetting("audio/volume").getDoubleVal());
            float soundVolume = (float) (GameSettings.getInstance().getSetting(sound.type.settingKey).getDoubleVal());

            float volume = masterVolume * soundVolume;
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
