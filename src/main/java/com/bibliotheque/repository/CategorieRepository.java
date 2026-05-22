package com.bibliotheque.repository;

import com.bibliotheque.model.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Integer> {
    List<Categorie> findAllByOrderByNomAsc();
}
