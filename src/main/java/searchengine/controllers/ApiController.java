package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.ErrorResponse;
import searchengine.dto.statistics.SearchResponse;
import searchengine.dto.statistics.Response;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Object> startIndexing() throws IOException {
        if (!IndexingService.isInProcess()) {
            IndexingService.process();
            indexingService.startIndexing();
            return ResponseEntity.ok(new Response());
        }
        else{
            return ResponseEntity.ok(new ErrorResponse("Индексация уже запущена"));
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Object> stopIndexing(){
        if (IndexingService.isInProcess()) {
            IndexingService.stop();
            return ResponseEntity.ok(new Response());
        } else {
            return ResponseEntity.ok(new ErrorResponse("Индексация не запущена"));
        }
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Object> indexPage(@RequestParam(name="url", required=false, defaultValue=" ") String url){
        return ResponseEntity.ok(indexingService.indexPage(url));
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(name="query", required=false)String query,
                                 @RequestParam(name="site_id", required=false, defaultValue = "0")String id,
                                 @RequestParam(name="offset", required=false, defaultValue = "0")String offset,
                                 @RequestParam(name="limit", required=false, defaultValue = "20")String limit) throws IOException {
        SearchService searchService = new SearchService();
        return ResponseEntity.ok(searchService.search(query, Integer.parseInt(id), Integer.parseInt(offset), Integer.parseInt(limit)));
    }

}

