package maker.backend.config;

import maker.backend.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * Module 15 — Aspect AOP pour la traçabilité automatique.
 * Intercepte tous les controllers et enregistre les actions write (POST, PUT, PATCH, DELETE).
 *
 * L'annotation @Tracable sur une méthode force l'audit avec un nom d'action personnalisé.
 */
@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Intercèpte les méthodes annotées avec @Tracable.
     */
    @Around("@annotation(maker.backend.config.Tracable)")
    public Object auditTracable(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        Tracable tracable = method.getAnnotation(Tracable.class);

        String username = getCurrentUser();
        String ip = getCurrentIp();
        String entite = tracable.entite();
        String action = tracable.action();

        Object result = pjp.proceed();

        // Tente d'extraire l'ID du résultat si c'est un DTO avec getId()
        Long entiteId = null;
        try {
            entiteId = (Long) result.getClass().getMethod("getId").invoke(result);
        } catch (Exception ignored) {}

        String nouvelleValeur = result != null ? result.toString() : null;
        auditService.enregistrer(entite, entiteId, action, username, null, nouvelleValeur, ip);
        return result;
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    private String getCurrentIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            String xff = request.getHeader("X-Forwarded-For");
            return xff != null ? xff.split(",")[0].trim() : request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
