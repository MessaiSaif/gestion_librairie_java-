package com.bibliotheque.repository;

import com.bibliotheque.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findByEmail(String email);
    Optional<Utilisateur> findByResetToken(String resetToken);
    Optional<Utilisateur> findByNumeroMembre(String numeroMembre);
    List<Utilisateur> findByRole(String role);
}
