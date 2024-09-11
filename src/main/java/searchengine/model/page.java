package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "page")
public class page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "site_id")
    private int siteId;

    @Column(columnDefinition = "text")
    private String path;

    private int code;

    @Column(columnDefinition = "mediumtext")
    private String content;
}
