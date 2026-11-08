package se.hembibliotek.book;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BookRepository implements PanacheRepository<Book> {

    public Optional<Book> findByIsbn(String isbn) {
        return find("isbn", isbn).firstResultOptional();
    }

    public boolean existsByIsbn(String isbn) {
        return count("isbn", isbn) > 0;
    }

    public boolean existsByIsbnAndIdNot(String isbn, Long id) {
        return count("isbn = ?1 and id <> ?2", isbn, id) > 0;
    }

    /** Fritextsökning (titel, ISBN, förlag, författare, genre, ämnesord) med valfritt genrefilter. */
    public List<Book> search(String query, String genre, int page, int size) {
        return searchQuery(query, genre).page(Page.of(page, size)).list();
    }

    public long countSearch(String query, String genre) {
        return searchQuery(query, genre).count();
    }

    public List<String> distinctGenres() {
        return getEntityManager()
                .createQuery("select distinct g from Book b join b.genres g order by g", String.class)
                .getResultList();
    }

    private PanacheQuery<Book> searchQuery(String query, String genre) {
        StringBuilder hql = new StringBuilder("from Book b where 1 = 1");
        List<Object> params = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            params.add("%" + query.trim().toLowerCase() + "%");
            hql.append("""
                     and (lower(b.title) like ?1 or lower(b.subtitle) like ?1 or b.isbn like ?1
                       or lower(b.publisher) like ?1
                       or exists (select 1 from b.authors a where lower(a) like ?1)
                       or exists (select 1 from b.genres g where lower(g) like ?1)
                       or exists (select 1 from b.subjects s where lower(s) like ?1))
                    """);
        }
        if (genre != null && !genre.isBlank()) {
            params.add(genre.trim());
            hql.append(" and exists (select 1 from b.genres gf where gf = ?").append(params.size()).append(")");
        }
        return find(hql.toString(), Sort.ascending("title"), params.toArray());
    }
}
