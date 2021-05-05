package ai.elimu.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tritonus.share.sampled.file.TAudioFileFormat;

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
            AudioFile af = AudioFileIO.read(target);
            AudioHeader ah = af.getAudioHeader();
            duration = (long)ah.getTrackLength() * 1000;
        } catch (IOException ex) {
            logger.error(ex);
        } catch (CannotReadException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (TagException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (ReadOnlyFileException e) {
            logger.error(e);
            e.printStackTrace();
        }

        return  duration;
    }
}
