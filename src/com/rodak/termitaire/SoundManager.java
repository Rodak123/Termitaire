package com.rodak.termitaire;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SoundManager {

    public enum Sound {
        CardUp,
        CardDown,
        ShuffleStock,
        Victory
    }

    private final Clip[] clips;

    public SoundManager(Path soundsFolder, String soundsFormat) {
        clips = new Clip[Sound.values().length];
        for (Sound sound : Sound.values()) {
            Path clipPath = soundsFolder.resolve(sound.name() + soundsFormat);
            if (!Files.exists(clipPath)) {
                System.out.println("Sound '" + clipPath.getFileName().toString() + "' does not exist.");
                continue;
            }

            try (final InputStream inputStream = Files.newInputStream(clipPath);
                 final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                 final AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedInputStream)) {
                final Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clips[sound.ordinal()] = clip;
            } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                e.printStackTrace();
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
