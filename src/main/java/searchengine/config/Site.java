package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import searchengine.model.SiteStatus;

import java.util.Date;

@Setter
@Getter
public class Site {
    private SiteStatus status;
    private Date statusTime;
    private String lastError;
    private String url;
    private String name;
}
