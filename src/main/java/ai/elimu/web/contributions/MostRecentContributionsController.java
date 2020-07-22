package ai.elimu.web.contributions;

import ai.elimu.dao.ContributorDao;
import ai.elimu.dao.StoryBookContributionEventDao;
import ai.elimu.dao.WordContributionEventDao;
import ai.elimu.model.contributor.Contributor;
import ai.elimu.model.contributor.StoryBookContributionEvent;
import java.util.List;
import org.apache.log4j.Logger;
import ai.elimu.model.contributor.WordContributionEvent;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/contributions/most-recent")
public class MostRecentContributionsController {
    
    private final Logger logger = Logger.getLogger(getClass());
    
    @Autowired
    private StoryBookContributionEventDao storyBookContributionEventDao;
    
    @Autowired
    private WordContributionEventDao wordContributionEventDao;
    
    @Autowired
    private ContributorDao contributorDao;

    @RequestMapping(method = RequestMethod.GET)
    public String handleRequest(Model model) {
    	logger.info("handleRequest");
        
        List<StoryBookContributionEvent> storyBookContributionEvents = storyBookContributionEventDao.readMostRecent(9);
        logger.info("storyBookContributionEvents.size(): " + storyBookContributionEvents.size());
        model.addAttribute("storyBookContributionEvents", storyBookContributionEvents);
        
        List<WordContributionEvent> wordContributionEvents = wordContributionEventDao.readMostRecent(10);
        logger.info("wordContributionEvents.size(): " + wordContributionEvents.size());
        model.addAttribute("wordContributionEvents", wordContributionEvents);
        
        
        List<Contributor> contributorsWithStoryBookContributions = contributorDao.readAllWithStoryBookContributions();
        logger.info("contributorsWithStoryBookContributions.size(): " + contributorsWithStoryBookContributions.size());
        model.addAttribute("contributorsWithStoryBookContributions", contributorsWithStoryBookContributions);
        
        // <Contributor ID, Count>
        Map<Long, Long> storyBookContributionsCountMap = new HashMap<>();
        for (Contributor contributor : contributorsWithStoryBookContributions) {
            storyBookContributionsCountMap.put(contributor.getId(), storyBookContributionEventDao.readCount(contributor));
        }
        model.addAttribute("storyBookContributionsCountMap", storyBookContributionsCountMap);
        
        
        List<Contributor> contributorsWithWordContributions = contributorDao.readAllWithWordContributions();
        logger.info("contributorsWithWordContributions.size(): " + contributorsWithWordContributions.size());
        model.addAttribute("contributorsWithWordContributions", contributorsWithWordContributions);
        
        // <Contributor ID, Count>
        Map<Long, Long> wordContributionsCountMap = new HashMap<>();
        for (Contributor contributor : contributorsWithWordContributions) {
            wordContributionsCountMap.put(contributor.getId(), wordContributionEventDao.readCount(contributor));
        }
        model.addAttribute("wordContributionsCountMap", wordContributionsCountMap);

        return "contributions/most-recent";
    }
}
