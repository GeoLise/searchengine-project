package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dao.*;
import searchengine.dto.statistics.*;
import searchengine.model.Page;
import searchengine.model.SiteStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;


@Service
@RequiredArgsConstructor
public class IndexingService {

    @Getter
    private static boolean inProcess = false;
    private final SitesList sites;
    private final SiteDao siteRepository;
    private final PageDao pageRepository;
    private final LemmaDao lemmaRepository;
    private final IndexDao indexRepository;

    public void startIndexing() throws IOException {
        try {
            MappingService.clearData();
        } catch (Exception e){

        }
        process();

        List<Site> sitesList = sites.getSites();

        indexRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        siteRepository.deleteAll();

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (Site site : sitesList) {
            executor.submit(() -> {
                mapper(site.getUrl());
            });
        }
        executor.shutdown();
    }





    private void mapper(String url){
        String siteName = url.replace("https://", "").replace("http://", "").replace("/", "");
        searchengine.model.Site site = new searchengine.model.Site();
        site.setName(siteName);
        site.setLastError(null);
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        site.setUrl(url);
        siteRepository.save(site);
        MappingService service = new MappingService(url, pageRepository, site, siteRepository, lemmaRepository, indexRepository);
        new ForkJoinPool().invoke(service);
        site = service.getSite();
        if (site.getStatus() == SiteStatus.FAILED){
            return;
        }
        if (inProcess) {
            site.setStatus(SiteStatus.INDEXED);
            siteRepository.update(site);
        } else {
            site.setStatus(SiteStatus.FAILED);
            site.setLastError("Индексация прервана пользователем");
            siteRepository.update(site);
        }
    }



    public static void stop() {
        inProcess = false;
        MappingService.setIsStopped(true);
    }

    public static void process(){
        inProcess = true;
        MappingService.setIsStopped(false);
    }

    public Object indexPage(String url){
        try {
            MappingService.clearData();
        } catch (Exception e){

        }
        process();
        String path = url.replace("https://", "").replace("http://", "");
        if (url.charAt(url.length() - 1) != '/'){
            path = path.substring(path.indexOf('/'));
        } else {
            path = StringUtils.chop(path.substring(path.indexOf('/')));
        }
        String mainUrl = url.substring(0, url.indexOf('/', 8));
        searchengine.model.Site site = (searchengine.model.Site) siteRepository.findByUrl(mainUrl);
        List<Page> pages = pageRepository.findBySiteId(site.getId());
        int countDeleted = 0;
        for (Page page : pages){
            if (page.getPath().startsWith(path)){
                try {
                    pageRepository.delete(page);
                    countDeleted++;
                } catch (Exception e){

                }
            }
        }
        if (countDeleted == 0){
            ErrorResponse response = new ErrorResponse("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
            return response;
        }
        Response response = new Response();

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executor.submit(() -> {
            new ForkJoinPool().invoke(new MappingService(url, pageRepository, site, siteRepository, lemmaRepository, indexRepository));
        });
        executor.shutdown();

        if (inProcess) {
            site.setStatus(SiteStatus.INDEXED);
            siteRepository.update(site);
        } else {
            site.setStatus(SiteStatus.FAILED);
            site.setLastError("Индексация прервана пользователем");
            siteRepository.update(site);
        }
        return response;
    }

}
