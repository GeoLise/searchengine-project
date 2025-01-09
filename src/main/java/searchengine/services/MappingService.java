package searchengine.services;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.dao.IndexDao;
import searchengine.dao.LemmaDao;
import searchengine.dao.PageDao;
import searchengine.dao.SiteDao;
import searchengine.model.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class MappingService extends RecursiveTask<Page> {
    private static final CopyOnWriteArrayList<String> links = new CopyOnWriteArrayList<>();

    private final PageDao pageRepository;
    private final SiteDao siteRepository;
    private final LemmaDao lemmaRepository;
    private final IndexDao indexRepository;

    private final String url;
    private final Site site;
    private static final AtomicBoolean isStopped = new AtomicBoolean(false);


    public MappingService(String url, PageDao pageRepository, Site site, SiteDao siteRepository, LemmaDao lemmaRepository, IndexDao indexRepository) {
        this.url = url;
        this.site = site;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }


    @Override
    protected Page compute() {
        if (isStopped.get()) {
            return null;
        }
        AtomicReference<Page> page = new AtomicReference<>(new Page());
        List<Page> pageList = new ArrayList<>();
        List<MappingService> mappers = new CopyOnWriteArrayList<>();
        try {
            try {
                Connection.Response response;
                try {
                    response = Jsoup.connect(url).ignoreContentType(true).userAgent("Mozilla/5.0").execute();
                } catch (UnknownHostException e){
                    site.setLastError("Сайта не существует");
                    site.setStatus(SiteStatus.FAILED);
                    site.setStatusTime(LocalDateTime.now());
                    siteRepository.update(site);
                    return null;
                }

                addTreads(mappers, response);

                page.set(addPage(url, response));
            } catch (HttpStatusException e) {
                addErrorPage(e.getUrl(), e.getStatusCode());
                return null;
            }
        } catch (IOException e) {

        }


        mappers.sort(Comparator.comparing((MappingService o) -> o.url));
        mappers.forEach(mapper -> {
            pageList.add(mapper.join());
        });
        return page.get();
    }



    private List<MappingService> addTreads(List<MappingService> mappers, Connection.Response response) throws IOException {
        if (isStopped.get()){
            return null;
        }
        Document document = response.parse();
        Elements elements = document.select("a[href]");
        for (int i = 0; i<elements.size(); i++){
            if (isStopped.get()) {
                break;
            }
            String currentUrl = elements.get(i).absUrl("href");
            if (!currentUrl.isEmpty() && currentUrl.startsWith(url) && !links.contains(currentUrl) && !currentUrl
                    .contains("#") && (!currentUrl.contains(".pdf"))) {
                MappingService linkExecutor = new MappingService(currentUrl, pageRepository, site, siteRepository, lemmaRepository, indexRepository);
                linkExecutor.fork();
                mappers.add(linkExecutor);
                links.add(currentUrl);
            }
        }
        return mappers;
    }



    private Page addPage(String url, Connection.Response response) throws IOException {
        String mainUrl = site.getUrl();
        url = StringUtils.substring(url, 0,  url.charAt(url.length() - 1) == '/' ? url.length() - 1 : url.length());
        Page page = new Page();
        page.setSite(site);
        String body = response.body();
        page.setContent(body);
        page.setPath(url.replace(mainUrl, "").isEmpty() ? "/" : url.replace(mainUrl, ""));
        page.setCode(response.statusCode());
        pageRepository.save(page);
        if (!isStopped.get()) {
            site.setStatusTime(LocalDateTime.now());
            siteRepository.update(site);
            addLemmasAndIndexes(body, page);
        }
        return page;
    }

    private Page addErrorPage(String url, int statusCode) {
        String mainUrl = site.getUrl();
        url = StringUtils.substring(url, 0, url.length() - 1);
        Page page = new Page();
        page.setSite(site);
        page.setContent("");
        page.setPath(url.replace(mainUrl, "").isEmpty() ? "/" : url.replace(mainUrl, ""));
        page.setCode(statusCode);
        pageRepository.save(page);
        if(!isStopped.get()) {
            site.setStatusTime(LocalDateTime.now());
            siteRepository.update(site);
        }
        return page;
    }

    private void addLemmasAndIndexes(String html, Page page) throws IOException {
        HashMap<String, Integer> pageLemmas = LemmaService.lemmasFromText(html);
        for(Map.Entry<String, Integer> entry : pageLemmas.entrySet()) {
            if (isStopped.get()) {
                break;
            }
            synchronized (lemmaRepository) {
                Lemma lemma = lemmaRepository.findByNameAndSite(entry.getKey(), site.getId());
                if (lemma != null) {
                    lemma.setFrequency(lemma.getFrequency() + 1);
                    lemmaRepository.update(lemma);
                } else {
                    lemma = new Lemma();
                    lemma.setFrequency(1);
                    lemma.setSite(site);
                    lemma.setLemma(entry.getKey());
                    lemmaRepository.save(lemma);
                }
                Index index = new Index();
                index.setPage(page);
                index.setLemma(lemma);
                index.setRank(entry.getValue());
                indexRepository.save(index);
            }
        }
    }



    public Site getSite(){
        return this.site;
    }

    public static void setIsStopped(boolean value) {
        isStopped.set(value);
    }

    public static void clearData(){
        links.clear();
        MappingService.getPool().shutdown();
    }

}
