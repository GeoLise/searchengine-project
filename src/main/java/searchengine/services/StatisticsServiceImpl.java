package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dao.LemmaDao;
import searchengine.dao.PageDao;
import searchengine.dao.SiteDao;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteDao siteRepository;
    private final PageDao pageRepository;
    private final LemmaDao lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {

        List<Site> siteList = siteRepository.findAll();

        TotalStatistics total = new TotalStatistics();
        total.setSites(siteList.size());
        total.setPages(pageRepository.count());
        total.setLemmas(lemmaRepository.count());
        total.setIndexing(true);



        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        for (Site site : siteList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            item.setPages(pageRepository.count(site.getId()));
            item.setLemmas(lemmaRepository.count(site.getId()));
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
