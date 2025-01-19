package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.dao.LemmaDao;
import searchengine.dto.statistics.ErrorResponse;
import searchengine.dto.statistics.SearchEntity;
import searchengine.dto.statistics.SearchResponse;
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

    public Object search(String query, int siteId, int offset, int limit) throws IOException {

        if (query.isEmpty()){
            ErrorResponse response = new ErrorResponse("Задан пустой поисковой индекс");
            return response;
        }
        List<Lemma> lemmas;
        try {
            lemmas = getLemmasFromSite(query, siteId);
        }catch (Exception e){
            ErrorResponse response = new ErrorResponse("Запрос введен некорректно");
            return response;
        }


        Map<Page, Double> pagesAndRelevance = new HashMap<>();
        try {
            pagesAndRelevance = getPagesAndRelevance(lemmas);
        } catch (ArrayIndexOutOfBoundsException e){
            ErrorResponse response = new ErrorResponse("Не найдено совпадений");
            return response;
        }

        List<SearchEntity> data = new ArrayList<>();
        try {
            data = getDataForResponse(pagesAndRelevance, lemmas);
        } catch (NullPointerException e){
            ErrorResponse response = new ErrorResponse("Не найдено совпадений");
            return response;
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


    private List<Lemma> getLemmasFromSite(String query, int siteId) throws IOException {
        HashMap<String, Integer> lemmaMap = LemmaService.lemmasFromText(query);
        List<Lemma> lemmas = new ArrayList<>();
        if (siteId != 0) {
            for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()) {
                    Lemma lemma = lemmaRepository.findByNameAndSite(entry.getKey(), siteId);
                if (lemma.getFrequency() <= 20) {
                    lemmas.add(lemma);
                }
            }
        } else {
            for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()){
                List<Lemma> resultList = lemmaRepository.findByName(entry.getKey());
                for(Lemma lemma : resultList){
                    if (lemma.getFrequency() <= 20){
                        lemmas.addAll(resultList);
                    }
                }
            }
        }
        lemmas = lemmas.stream().sorted(Comparator.comparingInt(Lemma::getFrequency)).toList();
        return lemmas;
    }


    private Map<Page, Double> getPagesAndRelevance(List<Lemma> lemmas){
        List<Page> pages = lemmaRepository.getPages(lemmas.get(0));
        for (Lemma lemma : lemmas){
            List<Page> lemmaPages = lemmaRepository.getPages(lemma);
            pages.retainAll(lemmaPages);
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

    private List<SearchEntity> getDataForResponse(Map<Page, Double> sortedMap, List<Lemma> lemmas){
        List<SearchEntity> data = new ArrayList<>();
        List<String> stringLemmas = lemmas.stream()
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
