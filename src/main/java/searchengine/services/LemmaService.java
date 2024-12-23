package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.WrongCharaterException;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;

public class LemmaService {

    private static LuceneMorphology luceneMorph;

    static {
        try {
            luceneMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static HashMap<String, Integer> lemmasFromText(String text) throws IOException {
        text = removeHtmlTags(text);
        HashMap<String, Integer> lemmas = new HashMap<>();
        text = text.replaceAll("[\\p{Punct}\\d]", "").replaceAll("\\s+", " ").trim().toLowerCase();
        String[] words = text.split(" ");
        for (String word : words){
            try {
                word = word.replaceAll(" ", "");
                word = luceneMorph.getNormalForms(word).get(0);
                String partOfSpeech = luceneMorph.getMorphInfo(word).get(0);
                if (partOfSpeech.contains("МЕЖД") || partOfSpeech.contains("ПРЕДЛ") || partOfSpeech.contains("СОЮЗ")) {
                    continue;
                }
                if (!lemmas.containsKey(word)) {
                    lemmas.put(word, 1);
                } else {
                    lemmas.replace(word, lemmas.get(word) + 1);
                }
            } catch (WrongCharaterException e){

            }
        }
        return lemmas;
    }

    public static String toSimpleWord(String word){
        word = word.replaceAll("[\\p{Punct}\\d]", "").replaceAll("\\s+", " ").trim().toLowerCase();
        try {
            String simpleWord = luceneMorph.getNormalForms(word).get(0);
            return simpleWord;
        } catch (Exception e){
            return word;
        }
    }

    public static String toSimpleText(String text) throws IOException {
        text = removeHtmlTags((text));
        String simpleText = "";
        text = text.replaceAll("[\\p{Punct}\\d]", "").replaceAll("\\s+", " ").trim().toLowerCase();
        String[] words = text.split(" ");
        for(String word : words){
            word = word.replaceAll(" ", "");
            word = luceneMorph.getNormalForms(word).get(0);
            simpleText += word + " ";
        }
        simpleText = simpleText.trim();

        return simpleText;
    }

    public static String removeHtmlTags(String html){
        return Jsoup.parse(html).text();
    }
}
