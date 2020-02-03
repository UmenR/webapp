package ai.elimu.web.content.word;

import ai.elimu.dao.WordDao;
import ai.elimu.model.content.Allophone;
import ai.elimu.model.content.Word;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/content/word/list")
public class WordCsvExportController {
    
    private final Logger logger = Logger.getLogger(getClass());
    
    @Autowired
    private WordDao wordDao;
    
    @RequestMapping(value="/words.csv", method = RequestMethod.GET)
    public void handleRequest(
            HttpServletResponse response,
            OutputStream outputStream) {
        logger.info("handleRequest");
        
        // Generate CSV file
        String csvFileContent = "id,text,phonetics,allophone_ids,usage_count,wordType,spellingConsistency" + "\n";
        List<Word> words = wordDao.readAll();
        logger.info("words.size(): " + words.size());
        for (Word word : words) {
            long[] allophoneIdsArray = new long[word.getAllophones().size()];
            int index = 0;
            for (Allophone allophone : word.getAllophones()) {
                allophoneIdsArray[index] = allophone.getId();
                index++;
            }
            csvFileContent += word.getId() + ","
                    + "\"" + word.getText() + "\","
                    + "/" + word.getPhonetics() + "/,"
                    + Arrays.toString(allophoneIdsArray) + ","
                    + word.getUsageCount() + ","
                    + word.getWordType() + ","
                    + word.getSpellingConsistency() + "\n";
        }
        
        response.setContentType("text/csv");
        byte[] bytes = csvFileContent.getBytes();
        response.setContentLength(bytes.length);
        try {
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            logger.error(null, ex);
        }
    }
}
