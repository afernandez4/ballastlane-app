package com.ballastlane.pokedex.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PokemonJpaRepository extends JpaRepository<PokemonEntity, Long> {

  Optional<PokemonEntity> findByPokeApiId(int pokeApiId);

  /**
   * Filters are always bound (empty string = no filter) so the query has the same shape for every
   * combination, which keeps Postgres parameter typing simple.
   */
  @Query(
      value =
          """
                    select p from PokemonEntity p
                    where (lower(p.name) like :namePattern
                           or lower(coalesce(p.localName, '')) like :namePattern)
                      and (:region = '' or lower(coalesce(p.region, '')) = :region)
                      and (:tag = '' or :tag member of p.tags)
                    """,
      countQuery =
          """
                    select count(p) from PokemonEntity p
                    where (lower(p.name) like :namePattern
                           or lower(coalesce(p.localName, '')) like :namePattern)
                      and (:region = '' or lower(coalesce(p.region, '')) = :region)
                      and (:tag = '' or :tag member of p.tags)
                    """)
  Page<PokemonEntity> search(
      @Param("namePattern") String namePattern,
      @Param("region") String region,
      @Param("tag") String tag,
      Pageable pageable);
}
