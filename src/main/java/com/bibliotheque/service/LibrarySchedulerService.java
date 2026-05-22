package com.bibliotheque.service;

import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Parametre;
import com.bibliotheque.model.Reservation;
import com.bibliotheque.model.Sanction;
import com.bibliotheque.repository.EmpruntRepository;
import com.bibliotheque.repository.ParametreRepository;
import com.bibliotheque.repository.ReservationRepository;
import com.bibliotheque.repository.SanctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class LibrarySchedulerService {

    @Autowired
    private EmpruntRepository empruntRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SanctionRepository sanctionRepository;

    @Autowired
    private ParametreRepository parametreRepository;

    // Run every hour
    @Scheduled(fixedRate = 3600000)
    public void processScheduledTasks() {
        checkExpiredReservations();
        checkLateLoans();
    }

    public void checkExpiredReservations() {
        List<Reservation> notified = reservationRepository.findByStatut("notifie");
        LocalDateTime now = LocalDateTime.now();
        for (Reservation r : notified) {
            if (r.getDateNotification() != null && r.getDateNotification().plusHours(48).isBefore(now)) {
                r.setStatut("expiree");
                reservationRepository.save(r);
                
                // Notify next in queue
                List<Reservation> queue = reservationRepository.findByLivreIdAndStatutOrderByDateReservationAsc(
                        r.getLivre().getId(), "en_attente");
                if (!queue.isEmpty()) {
                    Reservation next = queue.get(0);
                    next.setStatut("notifie");
                    next.setDateNotification(LocalDateTime.now());
                    reservationRepository.save(next);
                }
            }
        }
    }

    public void checkLateLoans() {
        double amendeParJour = 0.50;
        Optional<Parametre> paramOpt = parametreRepository.findById("amende_journaliere");
        if (paramOpt.isPresent()) {
            try {
                amendeParJour = Double.parseDouble(paramOpt.get().getValeur());
            } catch (NumberFormatException e) {
                // Ignore and use default
            }
        }

        List<Emprunt> loans = empruntRepository.findByStatutIn(Arrays.asList("en_cours", "en_retard"));
        LocalDate today = LocalDate.now();

        for (Emprunt loan : loans) {
            if (loan.getDateRetourPrevue().isBefore(today)) {
                if ("en_cours".equals(loan.getStatut())) {
                    loan.setStatut("en_retard");
                    empruntRepository.save(loan);
                }

                long daysLate = ChronoUnit.DAYS.between(loan.getDateRetourPrevue(), today);
                if (daysLate > 0) {
                    double fineAmount = daysLate * amendeParJour;

                    Optional<Sanction> existingSanction = sanctionRepository.findByEmpruntIdAndPayee(loan.getId(), false);
                    if (existingSanction.isPresent()) {
                        Sanction s = existingSanction.get();
                        s.setMontant(fineAmount);
                        sanctionRepository.save(s);
                    } else {
                        Sanction s = new Sanction();
                        s.setMembre(loan.getMembre());
                        s.setEmprunt(loan);
                        s.setMontant(fineAmount);
                        s.setDateSanction(LocalDateTime.now());
                        s.setPayee(false);
                        sanctionRepository.save(s);
                    }
                }
            }
        }
    }
}
