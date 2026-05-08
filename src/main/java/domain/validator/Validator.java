package domain.validator;

import domain.exception.ValidationException;

public interface Validator<E> {
    void validate(E entity) throws ValidationException;
}