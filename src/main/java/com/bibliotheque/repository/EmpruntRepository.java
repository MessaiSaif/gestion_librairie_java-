package com.bibliotheque.repository;

import com.bibliotheque.model.Emprunt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmpruntRepository extends JpaRepository<Emprunt, Integer> {
    List<Emprunt> findByMembreId(Integer membreId);
    List<Emprunt> findByMembreIdAndStatutIn(Integer membreId, List<String> statuts);
    List<Emprunt> findByStatutIn(List<String> statuts);
    List<Emprunt> findByLivreIdAndStatutIn(Integer livreId, List<String> statuts);
    long countByLivreIdAndStatutIn(Integer livreId, List<String> statuts);
    long countByMembreIdAndStatutIn(Integer membreId, List<String> statuts);
}
