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
import searchengine.dto.statistics.Response;
import searchengine.exception.SearchServiceException;
import searchengine.model.SiteStatus;

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

    private int indexedSites;

    private final SitesList sites;
    private final SiteDao siteRepository;
    private final PageDao pageRepository;
    private final LemmaDao lemmaRepository;
    private final IndexDao indexRepository;

    private List<MappingService> services = new ArrayList<>();

    public Response startIndexing(){
        if (inProcess){
            throw new SearchServiceException("Индексация уже запущена");
        }

        process();

        List<Site> sitesList = sites.getSites();

        indexRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        siteRepository.deleteAll();

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        indexedSites = 0;

        List<searchengine.model.Site> sitesToIndex = new ArrayList<>();

        for (Site configSite : sitesList) {
                String url = configSite.getUrl();
                String siteName = url.replace("https://", "").replace("http://", "").replace("/", "");
                if (url.charAt(url.length() - 1) == '/'){
                    url = url.substring(0, url.length() - 1);
                }
                searchengine.model.Site site = new searchengine.model.Site();
                site.setName(siteName);
                site.setLastError(null);
                site.setStatus(SiteStatus.INDEXING);
                site.setStatusTime(LocalDateTime.now());
                site.setUrl(url);
                sitesToIndex.add(site);
                siteRepository.saveOrUpdate(site);
        }
        executor.submit(() -> {
            mapper(sitesToIndex);
        });

        executor.shutdown();
        return new Response();
    }



    private void mapper(List<searchengine.model.Site> sitesToIndex){
        sitesToIndex.forEach(site -> {
            MappingService service = new MappingService(site.getUrl(), site);
            services.add(service);
            new ForkJoinPool().invoke(service);
            site = service.getSite();
            if (site.getStatus() == SiteStatus.FAILED){
                indexedSites+=1;
                return;
            }
            else if(inProcess) {
                indexedSites+=1;
                site.setStatus(SiteStatus.INDEXED);
                siteRepository.update(site);
            }
            if (indexedSites == sites.getSites().size()){
                stop();
            }
        });

    }


    public Response stop() {
        System.out.println(indexedSites);
        services.forEach(MappingService::clearData);
        services.clear();
        if (!inProcess){
            throw new SearchServiceException("Индексация не запущена");
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



    public Response indexPage(String url){

        process();
        boolean pageIsInSite = false;
        for(Site site : sites.getSites()){
            if (url.startsWith(site.getUrl())){
                pageIsInSite = true;
            }
        }
        if (!pageIsInSite) {
            throw new SearchServiceException("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
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

        pageRepository.deleteAllByPath(path);

        searchengine.model.Site currentSite = site;
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<searchengine.model.Site> sitesToIndex = new ArrayList<>();
        sitesToIndex.add(currentSite);
        executor.submit(() -> {
            mapper(sitesToIndex);
        });
        executor.shutdown();
        return new Response();
    }

}
