package com.bibliotheque.repository;

import com.bibliotheque.model.Livre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LivreRepository extends JpaRepository<Livre, Integer> {
    List<Livre> findByCategorieId(Integer categorieId);
    List<Livre> findAllByOrderByTitreAsc();

    @Query("SELECT l FROM Livre l WHERE LOWER(l.titre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(l.auteur) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(l.isbn) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Livre> searchBooks(@Param("search") String search);
}
