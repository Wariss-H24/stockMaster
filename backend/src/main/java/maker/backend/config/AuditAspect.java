package maker.backend.config;

import maker.backend.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * Module 15 — Aspect AOP pour la traçabilité automatique.
 * Intercepte les méthodes annotées @Tracable et enregistre l'action dans audit_logs.
 */
@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Around("@annotation(maker.backend.config.Tracable)")
    public Object auditTracable(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        Tracable tracable = method.getAnnotation(Tracable.class);

        String username = getCurrentUser();
        String ip       = getCurrentIp();
        String entite   = tracable.entite();
        String action   = tracable.action();

        // Exécuter la méthode
        Object result = pjp.proceed();

        // Extraire le body si c'est un ResponseEntity
        Object body = result;
        if (result instanceof ResponseEntity<?> re) {
            body = re.getBody();
        }

        // Tenter d'extraire l'ID via getId()
        Long entiteId = null;
        if (body != null) {
            try {
                entiteId = (Long) body.getClass().getMethod("getId").invoke(body);
            } catch (Exception ignored) {}
        }

        // Résumé de la nouvelle valeur
        String nouvelleValeur = null;
        if (body != null) {
            try {
                // Essayer getReference() pour un nom lisible
                Object ref = body.getClass().getMethod("getReference").invoke(body);
                nouvelleValeur = entite + "#" + entiteId + " [" + ref + "]";
            } catch (Exception e) {
                nouvelleValeur = entite + (entiteId != null ? "#" + entiteId : "");
            }
        }

        auditService.enregistrer(entite, entiteId, action, username, null, nouvelleValeur, ip);
        return result;
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }

    private String getCurrentIp() {
        try {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest req = attrs.getRequest();
            String xff = req.getHeader("X-Forwarded-For");
            return xff != null ? xff.split(",")[0].trim() : req.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
