package com.bibliotheque.repository;

import com.bibliotheque.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByMembreId(Integer membreId);
    List<Reservation> findByMembreIdAndStatut(Integer membreId, String statut);
    Optional<Reservation> findByMembreIdAndLivreIdAndStatut(Integer membreId, Integer livreId, String statut);
    List<Reservation> findByMembreIdAndLivreIdAndStatutIn(Integer membreId, Integer livreId, java.util.List<String> statuts);
    List<Reservation> findByLivreIdAndStatutOrderByDateReservationAsc(Integer livreId, String statut);
    List<Reservation> findByStatut(String statut);
}
