package searchengine.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Entity
@Table(name = "site")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "enum('INDEXING', 'INDEXED', 'FAILED')")
    @Enumerated(EnumType.STRING)
    private SiteStatus status;

    @Column(name = "status_time")
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    private String url;

    private String name;

    @OneToMany(mappedBy = "site",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<Lemma> lemmaList;

    @OneToMany(mappedBy = "site",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<Page> pages;

}

