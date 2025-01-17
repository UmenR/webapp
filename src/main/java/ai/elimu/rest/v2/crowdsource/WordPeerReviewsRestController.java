package ai.elimu.rest.v2.crowdsource;

import ai.elimu.dao.WordContributionEventDao;
import ai.elimu.model.contributor.WordContributionEvent;
import ai.elimu.model.v2.gson.crowdsource.WordContributionEventGson;
import ai.elimu.rest.v2.JpaToGsonConverter;
import com.google.gson.Gson;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for the Crowdsource application: https://github.com/elimu-ai/crowdsource
 */
@RestController
@RequestMapping(value = "/rest/v2/crowdsource/word-peer-reviews", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class WordPeerReviewsRestController {
    
    private Logger logger = LogManager.getLogger();
    
    @Autowired
    private WordContributionEventDao wordContributionEventDao;
    
    @RequestMapping(method = RequestMethod.GET)
    public String handleGetRequest() {
        logger.info("handleGetRequest");
        
        JSONArray wordsJsonArray = new JSONArray();
        
        List<WordContributionEvent> wordContributionEvents = wordContributionEventDao.readAll();
        logger.info("wordContributionEvents.size(): " + wordContributionEvents.size());
        for (WordContributionEvent wordContributionEvent : wordContributionEvents) {
            WordContributionEventGson wordContributionEventGson = JpaToGsonConverter.getWordContributionEventGson(wordContributionEvent);
            String json = new Gson().toJson(wordContributionEventGson);
            wordsJsonArray.put(new JSONObject(json));
        }
        
        String jsonResponse = wordsJsonArray.toString();
        logger.info("jsonResponse: " + jsonResponse);
        return jsonResponse;
    }
}
