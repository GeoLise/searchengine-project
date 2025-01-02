package searchengine.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "page", indexes = {@javax.persistence.Index(name = "idx_path", columnList = "path")})
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_id")
    @EqualsAndHashCode.Exclude
    private Site site;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    @EqualsAndHashCode.Exclude
    private String path;

    @EqualsAndHashCode.Exclude
    private int code;

    @Column(columnDefinition = "mediumtext")
    @EqualsAndHashCode.Exclude
    private String content;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "indexes",
            joinColumns = @JoinColumn(name="page_id"),
            inverseJoinColumns = @JoinColumn(name = "lemma_id"))
    @EqualsAndHashCode.Exclude
    private List<Lemma> lemmas = new ArrayList<>();

    @OneToMany(mappedBy = "page", fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    private List<Index> indexes = new ArrayList<>();

    public String toString(){
        return Integer.toString(id);
    }

}
