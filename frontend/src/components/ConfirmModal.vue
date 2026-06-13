<template>
  <div class="modal-overlay" @click.self="$emit('annuler')">
    <div class="modal confirm-modal">
      <!-- Icône selon le type -->
      <div class="confirm-icon" :class="'confirm-icon--' + type">
        <!-- Danger : corbeille -->
        <svg v-if="type === 'danger'" width="22" height="22" viewBox="0 0 24 24" fill="none">
          <polyline points="3 6 5 6 21 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6" stroke="currentColor" stroke-width="1.8"/>
          <path d="M10 11v6M14 11v6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          <path d="M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2" stroke="currentColor" stroke-width="1.8"/>
        </svg>
        <!-- Warning : désactivation -->
        <svg v-else-if="type === 'warning'" width="22" height="22" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/>
          <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
        <!-- Info : par défaut -->
        <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/>
          <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          <line x1="12" y1="16" x2="12.01" y2="16" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
        </svg>
      </div>

      <div class="confirm-body">
        <h3 class="confirm-title">{{ titre }}</h3>
        <p class="confirm-message">{{ message }}</p>
      </div>

      <div class="confirm-footer">
        <button class="btn btn-outline" @click="$emit('annuler')">
          Annuler
        </button>
        <button
          class="btn"
          :class="type === 'danger' ? 'btn-confirm-danger' : type === 'warning' ? 'btn-confirm-warning' : 'btn-primary'"
          @click="$emit('confirmer')"
        >
          {{ labelConfirmer }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * Modale de confirmation réutilisable.
 * Props :
 *  - titre        : titre de la modale
 *  - message      : texte explicatif
 *  - type         : 'danger' | 'warning' | 'info'
 *  - labelConfirmer : texte du bouton de confirmation
 * Événements :
 *  - confirmer : l'utilisateur confirme
 *  - annuler   : l'utilisateur annule ou clique dehors
 */
export default {
  name: 'ConfirmModal',
  props: {
    titre:          { type: String, default: 'Confirmer' },
    message:        { type: String, default: 'Êtes-vous sûr de vouloir continuer ?' },
    type:           { type: String, default: 'warning' },  // 'danger' | 'warning' | 'info'
    labelConfirmer: { type: String, default: 'Confirmer' }
  },
  emits: ['confirmer', 'annuler']
}
</script>

<style scoped>
/* Centré, plus petit que les modals formulaires */
.confirm-modal {
  max-width: 420px;
  padding: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

/* Zone icône colorée */
.confirm-icon {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 32px auto 0;
  flex-shrink: 0;
}
.confirm-icon--danger  { background: var(--danger-bg);  color: var(--danger); }
.confirm-icon--warning { background: var(--warning-bg); color: var(--warning); }
.confirm-icon--info    { background: var(--info-bg);    color: var(--info); }

.confirm-body {
  padding: 18px 28px 8px;
}
.confirm-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--gray-900);
  margin-bottom: 8px;
}
.confirm-message {
  font-size: .875rem;
  color: var(--gray-500);
  line-height: 1.5;
}

.confirm-footer {
  display: flex;
  gap: 10px;
  justify-content: center;
  padding: 20px 28px 28px;
  width: 100%;
}
.confirm-footer .btn { min-width: 110px; justify-content: center; }

/* Bouton confirmation rouge */
.btn-confirm-danger {
  background: var(--danger);
  color: #fff;
  border-color: var(--danger);
}
.btn-confirm-danger:hover { background: #b91c1c; border-color: #b91c1c; }

/* Bouton confirmation orange */
.btn-confirm-warning {
  background: var(--warning);
  color: #fff;
  border-color: var(--warning);
}
.btn-confirm-warning:hover { background: #b45309; border-color: #b45309; }
</style>
