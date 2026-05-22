package com.bibliotheque.repository;

import com.bibliotheque.model.Sanction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SanctionRepository extends JpaRepository<Sanction, Integer> {
    List<Sanction> findByMembreId(Integer membreId);
    List<Sanction> findByMembreIdAndPayee(Integer membreId, Boolean payee);
    Optional<Sanction> findByEmpruntIdAndPayee(Integer empruntId, Boolean payee);
    List<Sanction> findByPayee(Boolean payee);
}
