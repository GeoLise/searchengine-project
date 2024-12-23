package searchengine.dto.statistics;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
public class SearchResponse {

    private boolean result;

    private int count;

    private List<SearchEntity> data = new ArrayList<>();
}
