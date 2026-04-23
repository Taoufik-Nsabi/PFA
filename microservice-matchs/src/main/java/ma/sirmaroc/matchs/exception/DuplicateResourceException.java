package ma.sirmaroc.matchs.exception;
// the goal there is to reduce the trow sytax and make the code cleaner
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s existe déjà avec %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
