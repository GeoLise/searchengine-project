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
@Table(name = "lemma")
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    @EqualsAndHashCode.Exclude
    private Site site;

    @Column(name = "lemma")
    @EqualsAndHashCode.Exclude
    private String lemma;

    @Column(name = "frequency")
    @EqualsAndHashCode.Exclude
    private int frequency;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "indexes",
            joinColumns = @JoinColumn(name="lemma_id"),
            inverseJoinColumns = @JoinColumn(name = "page_id"))
    @EqualsAndHashCode.Exclude
    private List<Page> pages = new ArrayList<>();

    @OneToMany(mappedBy = "lemma", fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    private List<Index> indexes = new ArrayList<>();

    public String toString(){
        return id + " " + lemma;
    }

}
