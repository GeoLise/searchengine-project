package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dao.LemmaDao;
import searchengine.dao.PageDao;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.config.SitesList;
import searchengine.dao.SiteDao;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final Random random = new Random();
    private final SitesList sites;
    private final SiteDao siteRepository;
    private final PageDao pageRepository;
    private final LemmaDao lemmaRepository;


    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        List<Site> siteList = siteRepository.findAll();
        List<Page> pageList = pageRepository.findAll();
        List<Lemma> lemmaList = lemmaRepository.findAll();


        TotalStatistics total = new TotalStatistics();
        total.setSites(siteList.size());
        total.setPages(pageList.size());
        total.setLemmas(lemmaList.size());
        total.setIndexing(true);



        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        for(int i = 0; i < siteList.size(); i++) {
            Site site = siteList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            item.setPages(pageRepository.findBySiteId(site.getId()).size());
            item.setLemmas(lemmaRepository.findBySite(site.getId()).size());
            item.setError(site.getLastError());
            item.setStatusTime(site.getStatusTime());
            item.setStatus(site.getStatus().toString());
            detailed.add(item);
        }




        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
