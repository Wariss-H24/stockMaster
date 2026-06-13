package maker.backend.exception;

/**
 * Exception levée quand une ressource n'est pas trouvée en base.
 * Traduite en HTTP 404 par le GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
