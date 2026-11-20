package repository.common;

// ovde nek bude mesto ako se desi recimo da ne postoji ili da su nevalidni podaci
public class RepositoryException extends RuntimeException {
    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}