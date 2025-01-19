package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dao.IndexDao;
import searchengine.dao.LemmaDao;
import searchengine.dao.PageDao;
import searchengine.dao.SiteDao;
import searchengine.dto.statistics.ErrorResponse;
import searchengine.dto.statistics.Response;
import searchengine.model.Page;
import searchengine.model.SiteStatus;

import javax.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    List<MappingService> services = new ArrayList<>();

    public Object startIndexing(){
        if (inProcess){
            return new ErrorResponse("Индексация уже запущена");
        }

        process();

        List<Site> sitesList = sites.getSites();

//        indexRepository.deleteAll();
//        lemmaRepository.deleteAll();
//        pageRepository.deleteAll();
//        siteRepository.deleteAll();

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (Site configSite : sitesList) {
            executor.submit(() -> {
                String url = configSite.getUrl();
                String siteName = url.replace("https://", "").replace("http://", "").replace("/", "");
                searchengine.model.Site site = new searchengine.model.Site();
                site.setName(siteName);
                site.setLastError(null);
                site.setStatus(SiteStatus.INDEXING);
                site.setStatusTime(LocalDateTime.now());
                site.setUrl(url);
                mapper(url, site);
            });
        }
        executor.shutdown();
        return new Response();
    }



    private void mapper(String url, searchengine.model.Site site){
        siteRepository.saveOrUpdate(site);
        MappingService service = new MappingService(url, site);
        services.add(service);
        new ForkJoinPool().invoke(service);
        site = service.getSite();
        if (site.getStatus() == SiteStatus.FAILED){
            return;
        }
        else if (inProcess) {
            site.setStatus(SiteStatus.INDEXED);
            siteRepository.update(site);
        }
        inProcess = false;
    }


    public Object stop() {
        services.forEach(MappingService::clearData);
        services.clear();
        if (!inProcess){
            return new ErrorResponse("Индексация не запущена");
        }
        inProcess = false;
        MappingService.setIsStopped(true);
        try {
            Thread.sleep(100);
        } catch (Exception e){

        }
        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals(SiteStatus.INDEXING)){
                site.setStatus(SiteStatus.FAILED);
                site.setLastError("Индексация прервана пользователем");
                siteRepository.update(site);
            }
        });
        return new Response();
    }

    public void process(){
        inProcess = true;
        MappingService.setIsStopped(false);
    }



    public Object indexPage(String url){

        process();
        boolean pageIsInSite = false;
        for(Site site : sites.getSites()){
            if (url.startsWith(site.getUrl())){
                pageIsInSite = true;
            }
        }
        if (!pageIsInSite) {
            return new ErrorResponse("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        String path = url.replace("https://", "").replace("http://", "");
        if (url.charAt(url.length() - 1) != '/'){
            path = path.substring(path.indexOf('/'));
        } else {
            path = StringUtils.chop(path.substring(path.indexOf('/')));
        }
        String mainUrl = url.substring(0, url.indexOf('/', 8));

        searchengine.model.Site site;


        site = (searchengine.model.Site) siteRepository.findByUrl(mainUrl);
        if (site == null){
            String siteName = mainUrl.replace("https://", "").replace("http://", "").replace("/", "");
            site = new searchengine.model.Site();
            site.setName(siteName);
            site.setStatusTime(LocalDateTime.now());
            site.setUrl(mainUrl);
            site.setLastError(null);
        }
        site.setStatus(SiteStatus.INDEXING);
        List<Page> pages = pageRepository.findBySiteId(site.getId());
        if (!pages.isEmpty()) {
            for (Page page : pages) {
                if (page.getPath().startsWith(path)) {
                    try {
                        pageRepository.delete(page);
                    } catch (Exception e){}
                }
            }
        }
        searchengine.model.Site currentSite = site;
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executor.submit(() -> {
            mapper(url, currentSite);
        });
        executor.shutdown();
        return new Response();
    }

}
