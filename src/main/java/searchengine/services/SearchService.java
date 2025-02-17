package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.dao.LemmaDao;
import searchengine.dto.statistics.SearchEntity;
import searchengine.dto.statistics.SearchResponse;
import searchengine.exception.SearchServiceException;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final static LemmaDao lemmaRepository = new LemmaDao();
    private final SearchResponse searchResponse = new SearchResponse();

    public SearchResponse search(String query, int siteId, int offset, int limit) throws IOException {

        try{
            query.isEmpty();
        } catch (Exception e){
            throw new SearchServiceException("Задан пустой поисковой индекс");
        }
        HashMap<Integer, List<Lemma>> siteLemmasMap;
        try {
            siteLemmasMap = getLemmasFromSite(query, siteId);
        }catch (Exception e){
            throw new SearchServiceException("Запрос введен некорректно");
        }


        Map<Page, Double> pagesAndRelevance = new HashMap<>();
        try {
            pagesAndRelevance = getPagesAndRelevance(siteLemmasMap);
        } catch (ArrayIndexOutOfBoundsException e){
            throw new SearchServiceException("Не найдено совпаденний");
        }

        List<SearchEntity> data = new ArrayList<>();
        try {
            data = getDataForResponse(pagesAndRelevance, siteLemmasMap);
        } catch (NullPointerException e){
            System.out.println(e);
            throw new SearchServiceException("Не найдено совпаденний");
        }


        List<SearchEntity> responseData = new ArrayList<>();
        int finalPos;
        if (offset + limit < data.size()){
            finalPos = offset+limit;
        }else {
            finalPos = data.size();
        }
        for (int i = offset; i<finalPos; i++){
            responseData.add(data.get(i));
        }

        searchResponse.setData(responseData);
        searchResponse.setResult(true);

        return searchResponse;
    }


    private HashMap<Integer, List<Lemma>> getLemmasFromSite(String query, int siteId) throws IOException {
        HashMap<String, Integer> lemmaMap = LemmaService.lemmasFromText(query);

        HashMap<Integer, List<Lemma>> siteLemmaMap = new HashMap<>();
        if (siteId != 0) {
            List<Lemma> lemmas = new ArrayList<>();
            int currentSiteId = 0;
            for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()) {
                    Lemma lemma = lemmaRepository.findByNameAndSite(entry.getKey(), siteId);
                    currentSiteId = lemma.getSite().getId();
                if (lemma.getFrequency() <= 20) {
                    lemmas.add(lemma);
                }
            }
            lemmas = lemmas.stream().sorted(Comparator.comparingInt(Lemma::getFrequency)).toList();
            siteLemmaMap.put(currentSiteId, lemmas);
        } else {
            int currentSiteId = 0;
            for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()){
                List<Lemma> resultList = lemmaRepository.findByName(entry.getKey());
                for(Lemma lemma : resultList){
                    currentSiteId = lemma.getSite().getId();
                    if (lemma.getFrequency() <= 20 && !siteLemmaMap.containsKey(currentSiteId)){
                        List<Lemma> lemmas = new ArrayList<>();
                        lemmas.add(lemma);
                        siteLemmaMap.put(currentSiteId, lemmas);
                    } else if(lemma.getFrequency() <= 20 && siteLemmaMap.containsKey(currentSiteId)){
                        List<Lemma> lemmas = siteLemmaMap.get(currentSiteId);
                        lemmas.add(lemma);
                        siteLemmaMap.put(currentSiteId, lemmas);
                    }
                }
            }
            siteLemmaMap.replaceAll((k, v) -> v.stream().sorted(Comparator.comparingInt(Lemma::getFrequency)).toList());
        }
        return siteLemmaMap;
    }


    private Map<Page, Double> getPagesAndRelevance(HashMap<Integer, List<Lemma>> siteLemmasMap){

        List<Page> pages = new ArrayList<>();

        List<Lemma> lemmas = new ArrayList<>();

        for(Map.Entry<Integer, List<Lemma>> entry : siteLemmasMap.entrySet()){
            List<Page> currentSitePages = lemmaRepository.getPages(entry.getValue().get(0));
            for (Lemma lemma : entry.getValue()){
                lemmas.add(lemma);
                List<Page> lemmaPages = lemmaRepository.getPages(lemma);
                currentSitePages.retainAll(lemmaPages);
            }
            pages.addAll(currentSitePages);
        }
        if (pages.isEmpty()){
            return null;
        }



        Map<Page, Double> absRelMap = new HashMap<>();
        int count = 0;
        for (Page page : pages){
            int rank = 0;
            for (Index index : page.getIndexes()){
                if (lemmas.contains(index.getLemma())){
                    rank+=index.getRank();
                }
            }
            absRelMap.put(page, (double) rank);
            count+=1;
        }
        Map<Page, Double> sortedMap = absRelMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
        double maxRel = new ArrayList<>(sortedMap.entrySet()).get(sortedMap.size() - 1).getValue();
        sortedMap.replaceAll((key, value) -> value / maxRel);
        searchResponse.setCount(count);
        return sortedMap;
    }

    private List<SearchEntity> getDataForResponse(Map<Page, Double> sortedMap, HashMap<Integer, List<Lemma>> siteLemmasMap){
        List<SearchEntity> data = new ArrayList<>();

        List<String> stringLemmas = siteLemmasMap.values().stream()
                .flatMap(List::stream)
                .map(Lemma::getLemma)
                .toList();

        for(Map.Entry<Page, Double> entry : sortedMap.entrySet()){
            String snippet = "";
            Page page = entry.getKey();
            String html = page.getContent();
            Document doc = Jsoup.parse(html);
            Elements elements = doc.body().select("*");
            for(Element element : elements){
                String text = element.ownText();
                text = LemmaService.removeHtmlTags(text);
                if (text.isEmpty()){
                    continue;
                }
                List<String> words = new ArrayList<>(Arrays.asList(text.split(" ")));
                for(int i = 0; i < words.size(); i++){
                    String simpleWord = LemmaService.toSimpleWord(words.get(i));
                    if (!stringLemmas.contains(simpleWord)){
                        continue;
                    }
                    String snippetText = "";
                    int startPos = i >= 20 ? i - 20 : 0;
                    int finalPos = (words.size() - 1) - 20 < i ? words.size() - 1 : i + 20;

                    for(int j = startPos; j <= finalPos; j++){
                        snippetText += words.get(j) + " ";
                    }
                    snippetText = snippetText.replace(words.get(i), "<b>" + words.get(i) + "</b>").trim();

                    if (startPos != 0){
                        snippetText = "..." + snippetText;
                    }

                    if (finalPos == words.size() - 1){
                        snippetText+= "...";
                    }

                    snippet += snippetText + "\t";
                }
            }
            SearchEntity entity = new SearchEntity();
            entity.setRelevance(entry.getValue());
            entity.setSite(page.getSite().getUrl());
            entity.setSiteName(page.getSite().getName());
            entity.setUri(page.getPath());
            entity.setTitle(doc.title());
            entity.setSnippet(snippet);
            data.add(entity);
        }
        return data;
    }

}
