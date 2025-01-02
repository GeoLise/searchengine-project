package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dao.SiteDao;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteDao siteRepository;

    @Override
    public StatisticsResponse getStatistics() {

        List<Site> siteList = siteRepository.findAll();

        List<Lemma> lemmaList = new ArrayList<>();

        List<Page> pageList = new ArrayList<>();

        for(Site site : siteList){
            lemmaList.addAll(site.getLemmas());
            pageList.addAll(site.getPages());
        }




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
            item.setPages(
                    (int) pageList.stream()
                            .filter(page -> site.getId() == page.getSite().getId())
                            .count()
            );
            item.setLemmas((int) lemmaList.stream()
                    .filter(lemma -> site.getId() == lemma.getSite().getId())
                    .count()
            );
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
