package domain.validator;

import domain.Train;
import domain.exception.ValidationException;

public class TrainValidator implements Validator<Train>
{
    @Override
    public void validate(Train entity) throws ValidationException
    {
        if (entity.getCapacity() == null || entity.getCapacity() <= 0)
        {
            throw new ValidationException("Train capacity must be greater than 0");
        }
        if (entity.getCapacity() > 1000)
        {
            throw new ValidationException("Train capacity cannot exceed 1000");
        }
    }
}
