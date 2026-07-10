package maker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Module 15 — Traçabilité : chaque action métier est enregistrée ici.
 */
@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Entité concernée : "Produit", "Stock", "BonReception"... */
    private String entite;

    /** ID de la ressource modifiée */
    private Long entiteId;

    /** Action : CREER, MODIFIER, SUPPRIMER, VALIDER... */
    private String action;

    /** Username de l'auteur de l'action */
    private String utilisateur;

    /** Ancienne valeur (résumé texte) */
    @Column(length = 2000)
    private String ancienneValeur;

    /** Nouvelle valeur (résumé texte) */
    @Column(length = 2000)
    private String nouvelleValeur;

    private LocalDateTime date = LocalDateTime.now();

    /** IP du client */
    private String ipAdresse;
}
