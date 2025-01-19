package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "indexes")
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private Page page;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lemma_id")
    private Lemma lemma;

    @Column(name = "rank_int")
    private int rank;

    public String toString(){
        return id + " lemma_id: " + lemma.getId() + " page_id:" + page.getId();
    }
}
