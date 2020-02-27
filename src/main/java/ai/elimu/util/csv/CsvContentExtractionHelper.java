package ai.elimu.util.csv;

import ai.elimu.dao.AllophoneDao;
import ai.elimu.dao.StoryBookDao;
import ai.elimu.dao.WordDao;
import ai.elimu.model.content.Allophone;
import ai.elimu.model.content.Emoji;
import ai.elimu.model.content.Letter;
import ai.elimu.model.content.Number;
import ai.elimu.model.content.StoryBook;
import ai.elimu.model.content.StoryBookChapter;
import ai.elimu.model.content.Word;
import ai.elimu.model.enums.ContentLicense;
import ai.elimu.model.enums.GradeLevel;
import ai.elimu.model.enums.Language;
import ai.elimu.model.enums.content.SpellingConsistency;
import ai.elimu.model.enums.content.WordType;
import ai.elimu.model.enums.content.allophone.SoundType;
import ai.elimu.util.ConfigHelper;
import ai.elimu.web.content.allophone.AllophoneCsvExportController;
import ai.elimu.web.content.emoji.EmojiCsvExportController;
import ai.elimu.web.content.letter.LetterCsvExportController;
import ai.elimu.web.content.number.NumberCsvExportController;
import ai.elimu.web.content.storybook.StoryBookCsvExportController;
import ai.elimu.web.content.word.WordCsvExportController;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class CsvContentExtractionHelper {
    
    private static final Logger logger = Logger.getLogger(CsvContentExtractionHelper.class);
    
    /**
     * For information on how the CSV files were generated, see {@link AllophoneCsvExportController#handleRequest}.
     */
    public static List<Allophone> getAllophonesFromCsvBackup(File csvFile) {
        logger.info("getAllophonesFromCsvBackup");
        
        logger.info("csvFile: " + csvFile);
        
        List<Allophone> allophones = new ArrayList<>();
        
        try {
            Scanner scanner = new Scanner(csvFile);
            int rowNumber = 0;
            while (scanner.hasNextLine()) {
                String row = scanner.nextLine();
                logger.info("row: " + row);
                
                rowNumber++;
                
                if (rowNumber == 1) {
                    // Skip the header row
                    continue;
                }
                
                // Expected header format: id,value_ipa,value_sampa,audio_id,diacritic,sound_type,usage_count
                // Expected row format: 1,"dʒ","dZ",null,false,null,-1
                
                // Prevent "dʒ" from being stored as ""dʒ""
                // TODO: find more robust solution (e.g. by using CSV parser library or JSON array parsing)
                row = row.replace("\"", "");
                logger.info("row (after removing '\"'): " + row);
                
                String[] rowValues = row.split(",");
                logger.info("rowValues: " + Arrays.toString(rowValues));
                
                // "id"
                Long id = Long.valueOf(rowValues[0]);
                logger.info("id: " + id);
                
                // "value_ipa"
                String valueIpa = String.valueOf(rowValues[1]);
                logger.info("valueIpa: \"" + valueIpa + "\"");
                
                // "value_sampa"
                String valueSampa = String.valueOf(rowValues[2]);
                logger.info("valueSampa: \"" + valueSampa + "\"");
                
                // "audio_id"
                Long audioId = null;
                if (!"null".equals(rowValues[3])) {
                    audioId = Long.valueOf(rowValues[3]);
                }
                logger.info("audioId: " + audioId);
                
                // "diacritic"
                boolean diacritic = Boolean.valueOf(rowValues[4]);
                logger.info("diacritic: " + diacritic);
                
                // "sound_type"
                SoundType soundType = null;
                if (!"null".equals(rowValues[5])) {
                    soundType = SoundType.valueOf(rowValues[5]);
                }
                logger.info("soundType: " + soundType);
                
                // "usage_count"
                int usageCount = Integer.valueOf(rowValues[6]);
                logger.info("usageCount: " + usageCount);
                
                Allophone allophone = new Allophone();
                // allophone.setId(id); // TODO: enable lookup of Allophones by ID
                allophone.setValueIpa(valueIpa);
                allophone.setValueSampa(valueSampa);
                // allophone.setAudio(); // TODO: enable lookup of Audios by ID
                allophone.setDiacritic(diacritic);
                allophone.setSoundType(soundType);
                allophone.setUsageCount(usageCount);
                
                allophones.add(allophone);
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
        }
        
        return allophones;
    }
    
    /**
     * For information on how the CSV files were generated, see {@link LetterCsvExportController#handleRequest}.
     */
    public static List<Letter> getLettersFromCsvBackup(File csvFile, AllophoneDao allophoneDao) {
        logger.info("getLettersFromCsvBackup");
        
        logger.info("csvFile: " + csvFile);
        
        List<Letter> letters = new ArrayList<>();
        
        try {
            Scanner scanner = new Scanner(csvFile);
            int rowNumber = 0;
            while (scanner.hasNextLine()) {
                String row = scanner.nextLine();
                logger.info("row: " + row);
                
                rowNumber++;
                
                if (rowNumber == 1) {
                    // Skip the header row
                    continue;
                }
                
                // Expected header format: id,text,allophone_values_ipa,allophone_ids,diacritic,usage_count
                // Expected row format: 1,"অ",[ɔ],[4],false,-1
                
                // Prevent "অ" from being stored as ""অ""
                // TODO: find more robust solution (e.g. by using CSV parser library or JSON array parsing)
                row = row.replace("\"", "");
                logger.info("row (after removing '\"'): " + row);
                
                // Prevent "java.lang.NumberFormatException: For input string: " 6]""
                // TODO: find more robust solution
                row = row.replace(", ", "|");
                logger.info("row (after removing ', '): " + row);
                
                String[] rowValues = row.split(",");
                logger.info("rowValues: " + Arrays.toString(rowValues));
                
                // "id"
                Long id = Long.valueOf(rowValues[0]);
                logger.info("id: " + id);
                
                // "text"
                String text = String.valueOf(rowValues[1]);
                logger.info("text: \"" + text + "\"");
                
                // "allophone_values_ipa"
                String allophoneValuesIpa = String.valueOf(rowValues[2]);
                logger.info("allophoneValuesIpa: \"" + allophoneValuesIpa + "\"");
                allophoneValuesIpa = allophoneValuesIpa.replace("[", "");
                logger.info("allophoneValuesIpa: \"" + allophoneValuesIpa + "\"");
                allophoneValuesIpa = allophoneValuesIpa.replace("]", "");
                logger.info("allophoneValuesIpa: \"" + allophoneValuesIpa + "\"");
                String[] allophoneValuesIpaArray = allophoneValuesIpa.split("\\|");
                logger.info("Arrays.toString(allophoneValuesIpaArray): " + Arrays.toString(allophoneValuesIpaArray));
                
                List<Allophone> allophones = new ArrayList<>();
                Language language = Language.valueOf(ConfigHelper.getProperty("content.language"));
                for (String allophoneValueIpa : allophoneValuesIpaArray) {
                    logger.info("Looking up Allophone with IPA value /" + allophoneValueIpa + "/");
                    Allophone allophone = allophoneDao.readByValueIpa(language, allophoneValueIpa);
                    logger.info("allophone.getId(): \"" + allophone.getId() + "\"");
                    allophones.add(allophone);
                }
                
                // "allophone_ids"
                String allophoneIds = String.valueOf(rowValues[3]);
                logger.info("allophoneIds: \"" + allophoneIds + "\"");
                allophoneIds = allophoneIds.replace("[", "");
                logger.info("allophoneIds: \"" + allophoneIds + "\"");
                allophoneIds = allophoneIds.replace("]", "");
                logger.info("allophoneIds: \"" + allophoneIds + "\"");
                String[] allophoneIdsArray = allophoneIds.split("\\|");
                logger.info("Arrays.toString(allophoneIdsArray): " + Arrays.toString(allophoneIdsArray));
                
                // diacritic
                boolean diacritic = Boolean.valueOf(rowValues[4]);
                logger.info("diacritic: " + diacritic);
                
                // "usage_count"
                int usageCount = Integer.valueOf(rowValues[5]);
                logger.info("usageCount: " + usageCount);
                
                Letter letter = new Letter();
                // letter.setId(id); // TODO: to enable later lookup of the same Letter by its ID
                letter.setText(text);
                letter.setAllophones(allophones);
                letter.setDiacritic(diacritic);
                letter.setUsageCount(usageCount);
                
                letters.add(letter);
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
        }
        
        return letters;
    }
    
    /**
     * For information on how the CSV files were generated, see {@link WordCsvExportController#handleRequest}.
     */
    public static List<Word> getWordsFromCsvBackup(File csvFile, AllophoneDao allophoneDao) {
        logger.info("getWordsFromCsvBackup");
        
        logger.info("csvFile: " + csvFile);
        
        List<Word> words = new ArrayList<>();
        
        try {
            Scanner scanner = new Scanner(csvFile);
            int rowNumber = 0;
            while (scanner.hasNextLine()) {
                String row = scanner.nextLine();
                logger.info("row: " + row);
                
                rowNumber++;
                
                if (rowNumber == 1) {
                    // Skip the header row
                    continue;
                }
                
                // Expected header format: id,text,allophone_values_ipa,allophone_ids,usage_count,word_type,spelling_consistency
                // Expected row format: 7,"anim",[ɑ, n, ɪ, m],[28, 14, 36, 13],0,null,null
                
                // Prevent "anim" from being stored as ""anim""
                // TODO: find more robust solution (e.g. by using CSV parser library or JSON array parsing)
                row = row.replace("\"", "");
                logger.info("row (after removing '\"'): " + row);
                
                // Prevent "java.lang.NumberFormatException: For input string: " 6]""
                // TODO: find more robust solution
                row = row.replace(", ", "|");
                logger.info("row (after removing ', '): " + row);
                
                String[] rowValues = row.split(",");
                logger.info("rowValues: " + Arrays.toString(rowValues));
                
                // "id"
                Long id = Long.valueOf(rowValues[0]);
                logger.info("id: " + id);
                
                // "text"
                String text = String.valueOf(rowValues[1]);
                logger.info("text: \"" + text + "\"");
                
                // "allophone_values_ipa"
                String allophoneValuesIpa = String.valueOf(rowValues[2]);
                logger.info("allophoneValuesIpa: \"" + allophoneValuesIpa + "\"");
                allophoneValuesIpa = allophoneValuesIpa.replace("[", "");
                logger.info("allophoneValuesIpa: \"" + allophoneValuesIpa + "\"");
                allophoneValuesIpa = allophoneValuesIpa.replace("]", "");
                logger.info("allophoneValuesIpa: \"" + allophoneValuesIpa + "\"");
                String[] allophoneValuesIpaArray = allophoneValuesIpa.split("\\|");
                logger.info("Arrays.toString(allophoneValuesIpaArray): " + Arrays.toString(allophoneValuesIpaArray));
                
                List<Allophone> allophones = new ArrayList<>();
                Language language = Language.valueOf(ConfigHelper.getProperty("content.language"));
                for (String allophoneValueIpa : allophoneValuesIpaArray) {
                    logger.info("Looking up Allophone with IPA value /" + allophoneValueIpa + "/");
                    Allophone allophone = allophoneDao.readByValueIpa(language, allophoneValueIpa);
                    logger.info("allophone.getId(): \"" + allophone.getId() + "\"");
                    allophones.add(allophone);
                }
                
                // "allophone_ids"
                String allophoneIds = String.valueOf(rowValues[4]);
                logger.info("allophoneIds: \"" + allophoneIds + "\"");
                allophoneIds = allophoneIds.replace("[", "");
                logger.info("allophoneIds: \"" + allophoneIds + "\"");
                allophoneIds = allophoneIds.replace("]", "");
                logger.info("allophoneIds: \"" + allophoneIds + "\"");
                String[] allophoneIdsArray = allophoneIds.split("\\|");
                logger.info("Arrays.toString(allophoneIdsArray): " + Arrays.toString(allophoneIdsArray));
                
                // "usage_count"
                int usageCount = Integer.valueOf(rowValues[4]);
                logger.info("usageCount: " + usageCount);
                
                // "word_type"
                WordType wordType = null;
                if (!"null".equals(rowValues[5])) {
                    wordType = WordType.valueOf(rowValues[5]);
                }
                logger.info("wordType: " + wordType);
                
                // spelling_consistency
                SpellingConsistency spellingConsistency = null;
                if (!"null".equals(rowValues[6])) {
                    spellingConsistency = SpellingConsistency.valueOf(rowValues[6]);
                }
                logger.info("spellingConsistency: " + spellingConsistency);
                
                Word word = new Word();
                // word.setId(id); // TODO: to enable later lookup of the same Word by its ID
                word.setText(text);
                word.setAllophones(allophones);
                word.setUsageCount(usageCount);
                word.setWordType(wordType);
                word.setSpellingConsistency(spellingConsistency);
                words.add(word);
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
        }
        
        return words;
    }
    
    /**
     * For information on how the CSV files were generated, see {@link NumberCsvExportController#handleRequest}.
     */
    public static List<Number> getNumbersFromCsvBackup(File csvFile, WordDao wordDao) {
        logger.info("getNumbersFromCsvBackup");
        
        logger.info("csvFile: " + csvFile);
        
        List<Number> numbers = new ArrayList<>();
        
        try {
            Scanner scanner = new Scanner(csvFile);
            int rowNumber = 0;
            while (scanner.hasNextLine()) {
                String row = scanner.nextLine();
                logger.info("row: " + row);
                
                rowNumber++;
                
                if (rowNumber == 1) {
                    // Skip the header row
                    continue;
                }
                
                // Expected header format: id,value,symbol,word_texts,word_ids
                // Expected row format: 1,0,"null",["zero"],[1]
                
                // Prevent "null" from being stored as ""null""
                // TODO: find more robust solution (e.g. by using CSV parser library or JSON array parsing)
                row = row.replace("\"", "");
                logger.info("row (after removing '\"'): " + row);
                
                // Prevent "java.lang.NumberFormatException: For input string: " 6]""
                // TODO: find more robust solution
                row = row.replace(", ", "|");
                logger.info("row (after removing ', '): " + row);
                
                String[] rowValues = row.split(",");
                logger.info("rowValues: " + Arrays.toString(rowValues));
                
                // "id"
                Long id = Long.valueOf(rowValues[0]);
                logger.info("id: " + id);
                
                // "value"
                Integer value = Integer.valueOf(rowValues[1]);
                logger.info("value: " + value);
                
                // "symbol"
                String symbol = null;
                if (!"null".equals(rowValues[2])) {
                    symbol = String.valueOf(rowValues[2]);
                }
                logger.info("symbol: \"" + symbol + "\"");
                
                // "word_texts"
                String wordTexts = String.valueOf(rowValues[3]);
                logger.info("wordTexts: \"" + wordTexts + "\"");
                wordTexts = wordTexts.replace("[", "");
                logger.info("wordTexts: \"" + wordTexts + "\"");
                wordTexts = wordTexts.replace("]", "");
                logger.info("wordTexts: \"" + wordTexts + "\"");
                String[] wordTextsArray = wordTexts.split("\\|");
                logger.info("Arrays.toString(wordTextsArray): " + Arrays.toString(wordTextsArray));
                        
                List<Word> words = new ArrayList<>();
                Language language = Language.valueOf(ConfigHelper.getProperty("content.language"));
                for (String wordText : wordTextsArray) {
                    logger.info("Looking up Word with text \"" + wordText + "\"");
                    Word word = wordDao.readByText(language, wordText);
                    logger.info("word.getId(): \"" + word.getId() + "\"");
                    words.add(word);
                }
                
                // "word_ids"
                String wordIds = String.valueOf(rowValues[4]);
                logger.info("wordIds: \"" + wordIds + "\"");
                wordIds = wordIds.replace("[", "");
                logger.info("wordIds: \"" + wordIds + "\"");
                wordIds = wordIds.replace("]", "");
                logger.info("wordIds: \"" + wordIds + "\"");
                String[] wordIdsArray = wordIds.split("\\|");
                logger.info("Arrays.toString(wordIdsArray): " + Arrays.toString(wordIdsArray));
                
                Number number = new Number();
                // number.setId(id); // TODO: to enable later lookup of the same Number by its ID
                number.setValue(value);
                number.setSymbol(symbol);
                number.setWords(words);
                numbers.add(number);
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
        }
        
        return numbers;
    }
    
    /**
     * For information on how the CSV files were generated, see {@link EmojiCsvExportController#handleRequest}.
     */
    public static List<Emoji> getEmojisFromCsvBackup(File csvFile, WordDao wordDao) {
        logger.info("getEmojisFromCsvBackup");
        
        logger.info("csvFile: " + csvFile);
        
        List<Emoji> emojis = new ArrayList<>();
        
        try {
            Scanner scanner = new Scanner(csvFile);
            int rowEmoji = 0;
            while (scanner.hasNextLine()) {
                String row = scanner.nextLine();
                logger.info("row: " + row);
                
                rowEmoji++;
                
                if (rowEmoji == 1) {
                    // Skip the header row
                    continue;
                }
                
                // Expected header format: id,glyph,unicode_version,unicode_emoji_version,word_texts,word_ids
                // Expected row format: 10,✋,6.0,1.0,[],[]
                
                // Prevent "text" from being stored as ""text""
                // TODO: find more robust solution (e.g. by using CSV parser library or JSON array parsing)
                row = row.replace("\"", "");
                logger.info("row (after removing '\"'): " + row);
                
                // Prevent "java.lang.EmojiFormatException: For input string: " 6]""
                // TODO: find more robust solution
                row = row.replace(", ", "|");
                logger.info("row (after removing ', '): " + row);
                
                String[] rowValues = row.split(",");
                logger.info("rowValues: " + Arrays.toString(rowValues));
                
                // "id"
                Long id = Long.valueOf(rowValues[0]);
                logger.info("id: " + id);
                
                // "glyph"
                String glyph = String.valueOf(rowValues[1]);
                logger.info("glyph: " + glyph);
                
                // "unicode_version"
                Double unicodeVersion = Double.valueOf(rowValues[2]);
                logger.info("unicodeVersion: " + unicodeVersion);
                
                // "unicode_emoji_version"
                Double unicodeEmojiVersion = Double.valueOf(rowValues[3]);
                logger.info("unicodeEmojiVersion: " + unicodeEmojiVersion);     
                
                // "word_texts"
                String wordTexts = String.valueOf(rowValues[4]);
                logger.info("wordTexts: \"" + wordTexts + "\"");
                wordTexts = wordTexts.replace("[", "");
                logger.info("wordTexts: \"" + wordTexts + "\"");
                wordTexts = wordTexts.replace("]", "");
                logger.info("wordTexts: \"" + wordTexts + "\"");
                String[] wordTextsArray = wordTexts.split("\\|");
                logger.info("Arrays.toString(wordTextsArray): " + Arrays.toString(wordTextsArray));
                Set<Word> words = new HashSet<>();
                if (StringUtils.isNotBlank(wordTexts)) {
                    Language language = Language.valueOf(ConfigHelper.getProperty("content.language"));
                    for (String wordText : wordTextsArray) {
                        logger.info("Looking up Word with text \"" + wordText + "\"");
                        Word word = wordDao.readByText(language, wordText);
                        logger.info("word.getId(): \"" + word.getId() + "\"");
                        words.add(word);
                    }
                }
                
                // "word_ids"
                String wordIds = String.valueOf(rowValues[5]);
                logger.info("wordIds: \"" + wordIds + "\"");
                wordIds = wordIds.replace("[", "");
                logger.info("wordIds: \"" + wordIds + "\"");
                wordIds = wordIds.replace("]", "");
                logger.info("wordIds: \"" + wordIds + "\"");
                String[] wordIdsArray = wordIds.split("\\|");
                logger.info("Arrays.toString(wordIdsArray): " + Arrays.toString(wordIdsArray));
                
                Emoji emoji = new Emoji();
                // emoji.setId(id); // TODO: to enable later lookup of the same Emoji by its ID
                emoji.setGlyph(glyph);
                emoji.setUnicodeVersion(unicodeVersion);
                emoji.setUnicodeEmojiVersion(unicodeEmojiVersion);
                emoji.setWords(words);
                emojis.add(emoji);
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
        }
        
        return emojis;
    }
    
    /**
     * For information on how the CSV files were generated, see {@link StoryBookCsvExportController#handleRequest}.
     * <p />
     * Also see {@link #getStoryBookChaptersFromCsvBackup}
     */
    public static List<StoryBook> getStoryBooksFromCsvBackup(File csvFile) {
        logger.info("getStoryBooksFromCsvBackup");
        
        logger.info("csvFile: " + csvFile);
        
        List<StoryBook> storyBooks = new ArrayList<>();
        
        try {
            Scanner scanner = new Scanner(csvFile);
            int rowStoryBook = 0;
            while (scanner.hasNextLine()) {
                String row = scanner.nextLine();
                logger.info("row: " + row);
                
                rowStoryBook++;
                
                if (rowStoryBook == 1) {
                    // Skip the header row
                    continue;
                }
                
                // Expected header format: id,description,description,content_license,attribution_url,grade_level,cover_image_id,chapter_ids,chapter_paragraph_texts
                // Expected row format: 1,"যে অটো আকাশে উড়েছিলো","দিল্লীর কঠিন ট্রাফিক জ্যামে পড়ে, একটি অটো জাদুর কাণ্ড করে। চালক ও যাত্রীদের সঙ্গে অটোতে উঠে বসো, এবং আকাশে ওড়ার অভিজ্ঞতা অর্জন করো।",null,"null",null,1,[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23],[["অর্জুন এক তিন চাকাওয়ালা অটো। তার আছে একটি হেডলাইট এবং সবুজ-হলুদ কোট। পুরো দিল্লীর সবচেয়ে বড় পরিবারটি তার মালিক। অর্জুন যেখানেই যায় দেখা পায় ভাই, বোন, চাচা, চাচী ও কাজিনদের।  “সাবধানে যেও,” তারা চেঁচিয়ে বলে।"], ["অর্জুন দিনরাত কঠিন পরিশ্রম করে।  শব্দ করে যে চলে। ফট ফট ভট ভট!  সে কখনো অভিযোগ করে না, কারণ অটোর চালক সিরিশও কঠোর পরিশ্রম করে। সিরিশজির বুড়ো হাড়ে টনটনে ব্যথা। তিনি অর্জুনকে প্লাস্টিক ফুল ও চিত্র তারকাদের ছবি দিয়ে সাজিয়েছেন। তিনি জ্বালানি তেল দিয়ে অর্জুনের তৃষ্ণা মিটান। কখনো কখনো তেল নেওয়ার জন্য লম্বা লাইনে দাঁড়িয়ে থাকতে হয়। যখনই অর্জুনের আচ্ছাদন ছিঁড়ে যায়, সঙ্গে সঙ্গে তিনি তা জোড়াতালি দেওয়ার ব্যবস্থা করে্ন।"], ["অর্জুন পরিবারকে লাজপাত নগর বাজারে নিয়ে যেতে পছন্দ করে। যখন পর্যটকেরা চার চাকার গাড়ির বদলে তিন চাকার অটো ভাড়া করে, তখন তার আনন্দের সীমা থাকে না। কুতুব মিনারের বাইরে একটি গাছের ছায়ায় সিরিশজির সঙ্গে সে বিশ্রাম নিতে ভালোবাসে।"], ["অর্জুন কোন্নট প্লেসের রাতের জাঁকজমক দেখে কখনো ক্লান্ত হয় না। সে রেলওয়ে স্টেশনের বাইরে ব্যস্ততা ক্রিকেট দেখে রোমাঞ্চিত হয়। ক্রিকেট ম্যাচের পরে ফিরোজ শাহ কোটলা থেকে বেরিয়ে আসা জনতা দেখেও তার একই অনুভূতি হয়। জীবন ভালোই চলছে, এবং অর্জুন জানে তার এর চেয়ে বেশি চাওয়ার নেই।"], ["কিন্তু মনে মনে অর্জুন আরো কিছু আশা করে। সে উড়তে চায়। ওহ, এর জন্য তো হেলিকপ্টারের পাখা লাগবে! অর্জুন ভাবে। তার ছাদের ওপরে সেগুলো বাতাস কেটে ঘুরবে। সিরিশজি চাদর দিয়ে তার মাথা ঢাকবে্ন, এবং এর প্রান্তগুলো ফরফর করে বাতাসে উড়বে। তারা আকাশে দিয়ে চলবে, ফট ফট ভট ভট!"], ["কিন্তু অর্জুন জানে এটা কেবলই একটা স্বপ্ন। অটোর হেলিকপ্টারের পাখা হচ্ছে হাতির ডানা থাকার মতো। অথবা পেছনে বগির সারি নিয়ে কোনো ট্রেইনের রকেটের মতো আকাশে ছোটা।"], ["এক গরমের দিনে, অর্জুন ব্যস্ত চার রাস্তার মোড়ে ট্রাফিকের জটলার মধ্যে অপেক্ষা করছে। সিরিশজির পেছনে বিরস বদনে বসে আছেন পরিপাটি শাড়ি পরা এক পাকাচুলো মহিলা।  ফট ফট ভট ভট!"], ["মলিন কাপড় পরা এক ছেলে গাড়ি ও অটোর মধ্যে দিয়ে হেঁটে হেঁটে পানি বিক্রি করছে। তার চোখ উজ্জ্বল পাথরের মতো চকচক করছে। সে এক বোতল ঠাণ্ডা বাড়িয়ে দিলো।  “ম্যাম? খুব ঠাণ্ডা... খুব ঠান্ডা... জাদু।” মহিলা হাসলেন, “জাদু?” ছেলেটি এমন জোরসে মাথা ঝাঁকালো যে মনে হলো তা ছিঁড়ে পড়বে।"], ["“আমাদের সকলেরই জাদু দরকার,” মহিলা বললেন। তিনি ছেলেটির কাছ থেকে কিছু রুপি দিয়ে দুই বোতল পানি কিনলেন। সঙ্গে সঙ্গে তিনি একটি বোতল সিরিশজির হাতে দিলেন।"], ["সিরিশজি তার পানের দাগ পড়া দাঁত বের করে প্রশস্ত হাসি হাসলেন। ্তিনি ঢক ঢক করে পানি পান করলেন, আর তখনি ট্রাফিক নড়ে উঠলেন।  “এই মধ্যেই জাদু কাজ করা শুরু করেছে,” সিরিশজি কৌতুক করলেন।  মহিলাও পানি পান করলেন। গাড়ি হঠাৎ চলতে শুরু করায় একটু পানি ছিটকে অর্জুনের গায়ে পড়লো।  ফট ফট ভট ভট! অর্জুন তার এক ভাইকে পিপ পিপ করে হাই বলে উল্লাস প্রকাশ করে।"], ["যেইমাত্র চলতে শুরু করেছে, অর্জুন অনুভব করে তার চাকা হালকা হয়ে গেছে। সামনের গাড়িগুলো দুই দিকে সরে যায় এবং সে শা-শা করে সামনে এগিয়ে চলে।"], ["খোলা রাস্তা পেয়ে সিরিশজি আশ্চর্য। তিনি রিয়ার-ভিউ মিররে মহিলার চোখের দিকে তাকা্ন।  “হ্যাঁ, দারুণ জাদু, ম্যাম!” মহিলার শাড়ি ঝলমলে, সূক্ষ্ম সোনালি সুতায় নকশা করা। “জাদু...” তিনি নীরবে হাসলেন।"], ["অর্জুনের চাকাগুলো রাস্তা ছেড়ে ওপরে উঠে যায়। কেবলই ওপরে উঠতে থাকে।  ফট ফট ভট ভট... ওপরে, ওপরে, ওপরে! তাকে সাহায্য করার জন্য কোনো হেল্লিকপ্টারের পাখা নেই। জাদুই হবে, অর্জুন ভাবে। এটি অটোর জাদু!"], ["এক ঝাঁক পাখি ছড়িয়ে ছিটিয়ে গেলো। খুশিতে অর্জুনের হেডলাইট জ্বলে।"], ["অর্জুন জওহরলাল নেহরু স্টেডিয়াম ও ইন্ডিয়া গেইটের ওপর দিয়ে উড়ে যায়। সে হুমায়ুনের সমাধি, যমুনা নদী এবং অনিন্দ্যসুন্দর অকশরধাম মন্দিরের দিকে তাকায়।  সে দেখে রাস্তার বিশাল সংযোগ, উন্মাদ মাকড়সার জালের মতো।  চোখ বড় বড় করে সিরিশজি হ্যান্ডেলবার চেপে ধরেন। কোনো গাড়িকে আর পাশ কাটিয়ে যাওয়ার দরকার নেই।"], ["মহিলা আঙুল দিয়ে শাড়ি স্পর্শ করেন।  “জনাব,” রিয়ার-ভিউ মিররের দিকে তাকিয়ে মহিলা বলেন।  “আপনার চেহারা!”  সিরিশজি আয়নায় তার নিজের দিকে তাকিয়ে দেখেন তার চেহারা বলিউড নায়কের মতো হয়ে গেছে। তার দাঁত সাদা ও চামড়া উজ্জ্বল। “আমরা আরেকটু পানি পান করবো,” তিনি উচ্চস্বরে বলেন।"], ["কিন্তু আমরা কী করছি? অর্জুন ভাবে। আমরা কোথায় যেতে পারি? আমি কে, যদি আমি আর চালক না হই? অর্জুন কখনোই এতো মুক্ত ছিল না। কখনোই তিনি এভাবে নিজেকে হারিয়ে ফেলেননি। তিনি জানেন, দুনিয়ার প্রতিটি ভ্রমণের উদ্দেশ্য আছে, এবং প্রতিটি গন্তব্য সাময়িক।"], ["হর্ন বাজানো গাড়িগুলোর ওপরে এক সীমাহীন নীরবতার মধ্যে অর্জুন রাস্তার জীবনের কথা মনে করে। সেখানে গাড়ি, মোটরসাইকেল ও বাসের জটলা। নিচে তাকিয়ে সে তার পরিবারের সদস্যদের দেখে, হলুদ ছাউনির বিন্দুগুলো উজ্জ্বল আলো ছড়াচ্ছে। অর্জুন মানুষের ভিড় ও হুলুস্থূলের কথা মনে করে, সকলেই কোথাও না কোথাও পৌঁছার জন্য মরিয়া। স্থান থেকে স্থানে, ফট ফট ভট ভট..."], ["মহিলা নিচে তাঁকিয়ে সেখানকার আনন্দের কথা ভাবলেন। এখন তার ঝিকিমিকি শাড়ির কথা ভুলে যাওয়া সহজ। আমি যাচ্ছি আমার কন্যা ও নাতিনাতনিদের দেখতে, তিনি ভাবেন। তারা অপেক্ষা করছে। তারাই আমার জীবনের আসল জাদু।"], ["এর মধ্যে, সিরিশজি তার বলিউডি চেহারা নিয়ে ক্লান্ত হয়ে পড়েছেন। এ দিয়ে কী হবে, তিনি ভাবেন। তিনি যদি এটা চাইতেন। এখন সিরিশজি তার নিজের চামড়ার আরাম ফিরে চান।"], ["অর্জুনের হেডলাইট টিমটিম করছে। সে উদ্দেশ্যহীন বোধ করছে। সে সিরিশজির মনের কথা পড়তে পারে। সে মহিলার মনোভাবও আঁচ করতে পারে।  সে ধীরে ধীরে নিচে নামতে থাকে। শহর উষ্ণতা ছড়াচ্ছে। যতই নিচে নামছে, অর্জুন ততই শক্তি পাচ্ছে।"], ["সিরিশজি আবার ব্যস্ত হয়ে পড়েন। তিনি শহরের পরিচিত জাদুতে নিমজ্জিত হন। শহরের প্রতিটি চিহ্ন ও প্রতি মোড় তার সঙ্গে কথা বলছে। শীঘ্রই তিনি নিজেকে আবার চিনতে পারেন। তিনি বুঝতে পারেন কোথায় যেতে হবে। তিনি এর মধ্যে সেখানে পৌঁছে গেছেন। মহিলার শাড়ি ফ্যাকাসে হয়ে এসেছে, কিন্তু তার চেহারা জ্বলজ্বল করছে।  অর্জুনের চাকাগুলো পাকা রাস্তা স্পর্শ করে এবং তার ইঞ্জিন স্বস্তির নিঃশ্বাস ছাড়ে। ফট ফট ভট ভট... “সাবধানে যেও” সড়কের এক কোণা থেকে তার এক ভাই পিপপিপ করে বলে।"], ["মহিলার নাতিনাতনি ওপরের জানালা থেকে তার উদ্দেশ্য হাত নাড়ে।  তিনি অটো থেকে নেমে সিরিশজিকে তার ভাড়া মিটিয়ে দেন।  প্রতিবার চলাতেই নতুন অভিজ্ঞতা। নিরন্তর এই চলা, আশ্চর্য ঘটনায় পূর্ণ।"]]
                
                // Prevent text from being stored as ""text""
                // TODO: find more robust solution (e.g. by using CSV parser library or JSON array parsing)
                row = row.replace("\"", "");
                logger.info("row (after removing '\"'): " + row);
                
                // Prevent "java.lang.StoryBookFormatException: For input string: " 6]""
                // TODO: find more robust solution
                row = row.replace(", ", "|");
                logger.info("row (after removing ', '): " + row);
                
                String[] rowValues = row.split(",");
                logger.info("rowValues: " + Arrays.toString(rowValues));
                
                // "id"
                Long id = Long.valueOf(rowValues[0]);
                logger.info("id: " + id);
                
                // "description"
                String title = String.valueOf(rowValues[1]);
                logger.info("title: " + title);
                
                // "description"
                String description = String.valueOf(rowValues[2]);
                logger.info("description: " + description);
                
                // content_license
                ContentLicense contentLicense = null;
                if (!"null".equals(rowValues[3])) {
                    contentLicense = ContentLicense.valueOf(rowValues[3]);
                }
                logger.info("contentLicense: " + contentLicense);
                
                // attribution_url
                String attributionUrl = null;
                if (!"null".equals(rowValues[4])) {
                    attributionUrl = String.valueOf(rowValues[4]);
                }
                logger.info("attributionUrl: \"" + attributionUrl + "\"");
                
                // grade_level
                GradeLevel gradeLevel = null;
                if (!"null".equals(rowValues[5])) {
                    gradeLevel = GradeLevel.valueOf(rowValues[5]);
                }
                logger.info("gradeLevel: " + gradeLevel);
                
                // cover_image_id
                Long coverImageId = null;
                if (!"null".equals(rowValues[6])) {
                    coverImageId = Long.valueOf(rowValues[6]);
                }
                logger.info("coverImageId: " + coverImageId);
                
                StoryBook storyBook = new StoryBook();
                // storyBook.setId(id); // TODO: to enable later lookup of the same StoryBook by its ID
                storyBook.setTitle(title);
                storyBook.setDescription(description);
                storyBook.setContentLicense(contentLicense);
                storyBook.setAttributionUrl(attributionUrl);
                storyBook.setGradeLevel(gradeLevel);
                // storyBook.setCoverImage(coverImage); // TODO
                storyBooks.add(storyBook);
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
        }
        
        return storyBooks;
    }
    
    /**
     * For information on how the CSV files were generated, see {@link StoryBookCsvExportController#handleRequest}.
     */
    public static List<StoryBookChapter> getStoryBookChaptersFromCsvBackup(File csvFile, StoryBookDao storyBookDao) {
        logger.info("getStoryBookChaptersFromCsvBackup");
        
        logger.info("csvFile: " + csvFile);
        
        List<StoryBookChapter> storyBookChapters = new ArrayList<>();
        
        try {
            Scanner scanner = new Scanner(csvFile);
            int rowStoryBook = 0;
            while (scanner.hasNextLine()) {
                String row = scanner.nextLine();
                logger.info("row: " + row);
                
                rowStoryBook++;
                
                if (rowStoryBook == 1) {
                    // Skip the header row
                    continue;
                }
                
                // Expected header format: id,description,description,content_license,attribution_url,grade_level,cover_image_id,chapter_ids,chapter_paragraph_texts
                // Expected row format: 1,"যে অটো আকাশে উড়েছিলো","দিল্লীর কঠিন ট্রাফিক জ্যামে পড়ে, একটি অটো জাদুর কাণ্ড করে। চালক ও যাত্রীদের সঙ্গে অটোতে উঠে বসো, এবং আকাশে ওড়ার অভিজ্ঞতা অর্জন করো।",null,"null",null,1,[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23],[["অর্জুন এক তিন চাকাওয়ালা অটো। তার আছে একটি হেডলাইট এবং সবুজ-হলুদ কোট। পুরো দিল্লীর সবচেয়ে বড় পরিবারটি তার মালিক। অর্জুন যেখানেই যায় দেখা পায় ভাই, বোন, চাচা, চাচী ও কাজিনদের।  “সাবধানে যেও,” তারা চেঁচিয়ে বলে।"], ["অর্জুন দিনরাত কঠিন পরিশ্রম করে।  শব্দ করে যে চলে। ফট ফট ভট ভট!  সে কখনো অভিযোগ করে না, কারণ অটোর চালক সিরিশও কঠোর পরিশ্রম করে। সিরিশজির বুড়ো হাড়ে টনটনে ব্যথা। তিনি অর্জুনকে প্লাস্টিক ফুল ও চিত্র তারকাদের ছবি দিয়ে সাজিয়েছেন। তিনি জ্বালানি তেল দিয়ে অর্জুনের তৃষ্ণা মিটান। কখনো কখনো তেল নেওয়ার জন্য লম্বা লাইনে দাঁড়িয়ে থাকতে হয়। যখনই অর্জুনের আচ্ছাদন ছিঁড়ে যায়, সঙ্গে সঙ্গে তিনি তা জোড়াতালি দেওয়ার ব্যবস্থা করে্ন।"], ["অর্জুন পরিবারকে লাজপাত নগর বাজারে নিয়ে যেতে পছন্দ করে। যখন পর্যটকেরা চার চাকার গাড়ির বদলে তিন চাকার অটো ভাড়া করে, তখন তার আনন্দের সীমা থাকে না। কুতুব মিনারের বাইরে একটি গাছের ছায়ায় সিরিশজির সঙ্গে সে বিশ্রাম নিতে ভালোবাসে।"], ["অর্জুন কোন্নট প্লেসের রাতের জাঁকজমক দেখে কখনো ক্লান্ত হয় না। সে রেলওয়ে স্টেশনের বাইরে ব্যস্ততা ক্রিকেট দেখে রোমাঞ্চিত হয়। ক্রিকেট ম্যাচের পরে ফিরোজ শাহ কোটলা থেকে বেরিয়ে আসা জনতা দেখেও তার একই অনুভূতি হয়। জীবন ভালোই চলছে, এবং অর্জুন জানে তার এর চেয়ে বেশি চাওয়ার নেই।"], ["কিন্তু মনে মনে অর্জুন আরো কিছু আশা করে। সে উড়তে চায়। ওহ, এর জন্য তো হেলিকপ্টারের পাখা লাগবে! অর্জুন ভাবে। তার ছাদের ওপরে সেগুলো বাতাস কেটে ঘুরবে। সিরিশজি চাদর দিয়ে তার মাথা ঢাকবে্ন, এবং এর প্রান্তগুলো ফরফর করে বাতাসে উড়বে। তারা আকাশে দিয়ে চলবে, ফট ফট ভট ভট!"], ["কিন্তু অর্জুন জানে এটা কেবলই একটা স্বপ্ন। অটোর হেলিকপ্টারের পাখা হচ্ছে হাতির ডানা থাকার মতো। অথবা পেছনে বগির সারি নিয়ে কোনো ট্রেইনের রকেটের মতো আকাশে ছোটা।"], ["এক গরমের দিনে, অর্জুন ব্যস্ত চার রাস্তার মোড়ে ট্রাফিকের জটলার মধ্যে অপেক্ষা করছে। সিরিশজির পেছনে বিরস বদনে বসে আছেন পরিপাটি শাড়ি পরা এক পাকাচুলো মহিলা।  ফট ফট ভট ভট!"], ["মলিন কাপড় পরা এক ছেলে গাড়ি ও অটোর মধ্যে দিয়ে হেঁটে হেঁটে পানি বিক্রি করছে। তার চোখ উজ্জ্বল পাথরের মতো চকচক করছে। সে এক বোতল ঠাণ্ডা বাড়িয়ে দিলো।  “ম্যাম? খুব ঠাণ্ডা... খুব ঠান্ডা... জাদু।” মহিলা হাসলেন, “জাদু?” ছেলেটি এমন জোরসে মাথা ঝাঁকালো যে মনে হলো তা ছিঁড়ে পড়বে।"], ["“আমাদের সকলেরই জাদু দরকার,” মহিলা বললেন। তিনি ছেলেটির কাছ থেকে কিছু রুপি দিয়ে দুই বোতল পানি কিনলেন। সঙ্গে সঙ্গে তিনি একটি বোতল সিরিশজির হাতে দিলেন।"], ["সিরিশজি তার পানের দাগ পড়া দাঁত বের করে প্রশস্ত হাসি হাসলেন। ্তিনি ঢক ঢক করে পানি পান করলেন, আর তখনি ট্রাফিক নড়ে উঠলেন।  “এই মধ্যেই জাদু কাজ করা শুরু করেছে,” সিরিশজি কৌতুক করলেন।  মহিলাও পানি পান করলেন। গাড়ি হঠাৎ চলতে শুরু করায় একটু পানি ছিটকে অর্জুনের গায়ে পড়লো।  ফট ফট ভট ভট! অর্জুন তার এক ভাইকে পিপ পিপ করে হাই বলে উল্লাস প্রকাশ করে।"], ["যেইমাত্র চলতে শুরু করেছে, অর্জুন অনুভব করে তার চাকা হালকা হয়ে গেছে। সামনের গাড়িগুলো দুই দিকে সরে যায় এবং সে শা-শা করে সামনে এগিয়ে চলে।"], ["খোলা রাস্তা পেয়ে সিরিশজি আশ্চর্য। তিনি রিয়ার-ভিউ মিররে মহিলার চোখের দিকে তাকা্ন।  “হ্যাঁ, দারুণ জাদু, ম্যাম!” মহিলার শাড়ি ঝলমলে, সূক্ষ্ম সোনালি সুতায় নকশা করা। “জাদু...” তিনি নীরবে হাসলেন।"], ["অর্জুনের চাকাগুলো রাস্তা ছেড়ে ওপরে উঠে যায়। কেবলই ওপরে উঠতে থাকে।  ফট ফট ভট ভট... ওপরে, ওপরে, ওপরে! তাকে সাহায্য করার জন্য কোনো হেল্লিকপ্টারের পাখা নেই। জাদুই হবে, অর্জুন ভাবে। এটি অটোর জাদু!"], ["এক ঝাঁক পাখি ছড়িয়ে ছিটিয়ে গেলো। খুশিতে অর্জুনের হেডলাইট জ্বলে।"], ["অর্জুন জওহরলাল নেহরু স্টেডিয়াম ও ইন্ডিয়া গেইটের ওপর দিয়ে উড়ে যায়। সে হুমায়ুনের সমাধি, যমুনা নদী এবং অনিন্দ্যসুন্দর অকশরধাম মন্দিরের দিকে তাকায়।  সে দেখে রাস্তার বিশাল সংযোগ, উন্মাদ মাকড়সার জালের মতো।  চোখ বড় বড় করে সিরিশজি হ্যান্ডেলবার চেপে ধরেন। কোনো গাড়িকে আর পাশ কাটিয়ে যাওয়ার দরকার নেই।"], ["মহিলা আঙুল দিয়ে শাড়ি স্পর্শ করেন।  “জনাব,” রিয়ার-ভিউ মিররের দিকে তাকিয়ে মহিলা বলেন।  “আপনার চেহারা!”  সিরিশজি আয়নায় তার নিজের দিকে তাকিয়ে দেখেন তার চেহারা বলিউড নায়কের মতো হয়ে গেছে। তার দাঁত সাদা ও চামড়া উজ্জ্বল। “আমরা আরেকটু পানি পান করবো,” তিনি উচ্চস্বরে বলেন।"], ["কিন্তু আমরা কী করছি? অর্জুন ভাবে। আমরা কোথায় যেতে পারি? আমি কে, যদি আমি আর চালক না হই? অর্জুন কখনোই এতো মুক্ত ছিল না। কখনোই তিনি এভাবে নিজেকে হারিয়ে ফেলেননি। তিনি জানেন, দুনিয়ার প্রতিটি ভ্রমণের উদ্দেশ্য আছে, এবং প্রতিটি গন্তব্য সাময়িক।"], ["হর্ন বাজানো গাড়িগুলোর ওপরে এক সীমাহীন নীরবতার মধ্যে অর্জুন রাস্তার জীবনের কথা মনে করে। সেখানে গাড়ি, মোটরসাইকেল ও বাসের জটলা। নিচে তাকিয়ে সে তার পরিবারের সদস্যদের দেখে, হলুদ ছাউনির বিন্দুগুলো উজ্জ্বল আলো ছড়াচ্ছে। অর্জুন মানুষের ভিড় ও হুলুস্থূলের কথা মনে করে, সকলেই কোথাও না কোথাও পৌঁছার জন্য মরিয়া। স্থান থেকে স্থানে, ফট ফট ভট ভট..."], ["মহিলা নিচে তাঁকিয়ে সেখানকার আনন্দের কথা ভাবলেন। এখন তার ঝিকিমিকি শাড়ির কথা ভুলে যাওয়া সহজ। আমি যাচ্ছি আমার কন্যা ও নাতিনাতনিদের দেখতে, তিনি ভাবেন। তারা অপেক্ষা করছে। তারাই আমার জীবনের আসল জাদু।"], ["এর মধ্যে, সিরিশজি তার বলিউডি চেহারা নিয়ে ক্লান্ত হয়ে পড়েছেন। এ দিয়ে কী হবে, তিনি ভাবেন। তিনি যদি এটা চাইতেন। এখন সিরিশজি তার নিজের চামড়ার আরাম ফিরে চান।"], ["অর্জুনের হেডলাইট টিমটিম করছে। সে উদ্দেশ্যহীন বোধ করছে। সে সিরিশজির মনের কথা পড়তে পারে। সে মহিলার মনোভাবও আঁচ করতে পারে।  সে ধীরে ধীরে নিচে নামতে থাকে। শহর উষ্ণতা ছড়াচ্ছে। যতই নিচে নামছে, অর্জুন ততই শক্তি পাচ্ছে।"], ["সিরিশজি আবার ব্যস্ত হয়ে পড়েন। তিনি শহরের পরিচিত জাদুতে নিমজ্জিত হন। শহরের প্রতিটি চিহ্ন ও প্রতি মোড় তার সঙ্গে কথা বলছে। শীঘ্রই তিনি নিজেকে আবার চিনতে পারেন। তিনি বুঝতে পারেন কোথায় যেতে হবে। তিনি এর মধ্যে সেখানে পৌঁছে গেছেন। মহিলার শাড়ি ফ্যাকাসে হয়ে এসেছে, কিন্তু তার চেহারা জ্বলজ্বল করছে।  অর্জুনের চাকাগুলো পাকা রাস্তা স্পর্শ করে এবং তার ইঞ্জিন স্বস্তির নিঃশ্বাস ছাড়ে। ফট ফট ভট ভট... “সাবধানে যেও” সড়কের এক কোণা থেকে তার এক ভাই পিপপিপ করে বলে।"], ["মহিলার নাতিনাতনি ওপরের জানালা থেকে তার উদ্দেশ্য হাত নাড়ে।  তিনি অটো থেকে নেমে সিরিশজিকে তার ভাড়া মিটিয়ে দেন।  প্রতিবার চলাতেই নতুন অভিজ্ঞতা। নিরন্তর এই চলা, আশ্চর্য ঘটনায় পূর্ণ।"]]
                
                // Prevent text from being stored as ""text""
                // TODO: find more robust solution (e.g. by using CSV parser library or JSON array parsing)
                row = row.replace("\"", "");
                logger.info("row (after removing '\"'): " + row);
                
                // Prevent "java.lang.StoryBookFormatException: For input string: " 6]""
                // TODO: find more robust solution
                row = row.replace(", ", "|");
                logger.info("row (after removing ', '): " + row);
                
                String[] rowValues = row.split(",");
                logger.info("rowValues: " + Arrays.toString(rowValues));
                
                // "id"
                Long id = Long.valueOf(rowValues[0]);
                logger.info("id: " + id);
                
                StoryBook storyBook = storyBookDao.read(id);
                logger.info("storyBook: " + storyBook);
                
                // chapter_ids
                if (!"null".equals(rowValues[7])) {
                    // "[1|2|3]" --> [1, 2, 3]
                    String chapterIdArrayAsString = rowValues[7];
                    logger.info("chapterIdArrayAsString: " + chapterIdArrayAsString);
                    chapterIdArrayAsString = chapterIdArrayAsString.replace("[", "");
                    logger.info("chapterIdArrayAsString (after removing '['): " + chapterIdArrayAsString);
                    chapterIdArrayAsString = chapterIdArrayAsString.replace("]", "");
                    logger.info("chapterIdArrayAsString (after removing ']'): " + chapterIdArrayAsString);
                    String[] chapterIdArray = chapterIdArrayAsString.split("\\|");
                    logger.info("Arrays.toString(chapterIdArray) (after removing '|'): " + Arrays.toString(chapterIdArray));
                    for (int i = 0; i < chapterIdArray.length; i++) {
                        Long storyBookChapterId = Long.valueOf(chapterIdArray[i]);
                        
                        StoryBookChapter storyBookChapter = new StoryBookChapter();
                        // storyBook.setId(storyBookChapterId); // TODO: to enable later lookup of the same StoryBook by its ID
                        storyBookChapter.setStoryBook(storyBook);
                        storyBookChapter.setSortOrder(i);
                        storyBookChapters.add(storyBookChapter);
                    }
                }
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
        }
        
        return storyBookChapters;
    }
}
