package ai.elimu.util;

import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

/**
 * Utility class for extraction information from audio files.
 */
public class AudioMetadataExtractionHelper {
    
    private static Logger logger = LogManager.getLogger();
    
    /**
     * Extracts the duration of an audio file.
     */
    public static Long getDurationInMilliseconds(File target) {
        logger.info("getDurationInMilliseconds");

        Long duration = null;
        try {
            AudioFile audioFile = AudioFileIO.read(target);
            AudioHeader audioHeader = audioFile.getAudioHeader();
            // Convert Duration to MilliSeconds.
            duration = (long)audioHeader.getTrackLength() * 1000;
        } catch (IOException | CannotReadException | TagException |
                ReadOnlyFileException | InvalidAudioFrameException ex) {
            ex.printStackTrace();
            logger.error(ex);
        }

        return  duration;
    }
}
