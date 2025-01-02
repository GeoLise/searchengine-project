package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.WrongCharaterException;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;

public class LemmaService {

    private static final LuceneMorphology RussianLuceneMorph;
    private static final LuceneMorphology EnglishLuceneMorph;

    static {
        try {
            EnglishLuceneMorph = new EnglishLuceneMorphology();
            RussianLuceneMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static HashMap<String, Integer> lemmasFromText(String text) throws IOException {
        text = removeHtmlTags(text);
        HashMap<String, Integer> lemmas = new HashMap<>();
        text = text.replaceAll("[\\p{Punct}\\d]", "").replaceAll("\\s+", " ").trim().toLowerCase();
        String[] words = text.split(" ");
        for (String word : words) {
            word = word.replaceAll(" ", "");
            String partOfSpeech;

            try {
                partOfSpeech = RussianLuceneMorph.getMorphInfo(word).get(0);
                word = RussianLuceneMorph.getNormalForms(word).get(0);
            } catch (WrongCharaterException e1) {
                try {
                    partOfSpeech = EnglishLuceneMorph.getMorphInfo(word).get(0);
                    word = EnglishLuceneMorph.getNormalForms(word).get(0);
                } catch (WrongCharaterException e2) {
                    continue;
                }
            }

            if (partOfSpeech.contains("МЕЖД") || partOfSpeech.contains("ПРЕДЛ") || partOfSpeech.contains("СОЮЗ")
                    || partOfSpeech.contains("CONJ") || partOfSpeech.contains("PREP") || partOfSpeech.contains("INT")) {
                continue;
            }
            if (!lemmas.containsKey(word)) {
                lemmas.put(word, 1);
            } else {
                lemmas.replace(word, lemmas.get(word) + 1);
            }
        }
        return lemmas;
    }

    public static String toSimpleWord(String word) {
        word = word.replaceAll("[\\p{Punct}\\d]", "").replaceAll("\\s+", " ").trim().toLowerCase();
        String simpleWord;
        try {
            simpleWord = RussianLuceneMorph.getNormalForms(word).get(0);
            return simpleWord;
        } catch (Exception e1) {
            try {
                simpleWord = EnglishLuceneMorph.getNormalForms(word).get(0);
                return simpleWord;
            } catch (Exception e2) {
                return word;
            }
        }
    }


    public static String removeHtmlTags(String html) {
        return Jsoup.parse(html).text();
    }
}
