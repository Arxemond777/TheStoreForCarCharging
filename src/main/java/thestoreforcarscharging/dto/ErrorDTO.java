package thestoreforcarscharging.dto;

/**
 * Representation of an error for API
 *
 * @param <T> - type of error
 *
 * @author <a href="mailto:1arxemond1@gmail.com">Yuri Glushenkov</a>
 */
public class ErrorDTO<T> {
    private T error;

    /**
     * For deserialize
     */
    public ErrorDTO() {
    }

    public ErrorDTO(final T error) {
        this.error = error;
    }

    public T getError() {
        return error;
    }
}
