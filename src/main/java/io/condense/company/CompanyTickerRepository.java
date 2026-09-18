package io.condense.company;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyTickerRepository extends JpaRepository<CompanyTicker, String> {

    @Query("""
            select new io.condense.company.CompanySearchResult(t.ticker, c.name)
            from CompanyTicker t, Company c
            where c.cik = t.cik
              and (t.ticker like :anywhere or upper(c.name) like :anywhere)
            order by
              case
                when t.ticker = :exact then 0
                when t.ticker like :prefix then 1
                when upper(c.name) like :prefix then 2
                else 3
              end,
              length(c.name),
              t.ticker
            """)
    List<CompanySearchResult> search(@Param("exact") String exact,
                                     @Param("prefix") String prefix,
                                     @Param("anywhere") String anywhere,
                                     Pageable pageable);
}
