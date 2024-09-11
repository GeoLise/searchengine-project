package searchengine.model;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "site")
public class site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "enum('INDEXING', 'INDEXED', 'FAILED')")
    @Enumerated(EnumType.STRING)
    private SiteStatus status;

    @Column(name = "status_time")
    private Date statusTime;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    private String url;

    private String name;
}
