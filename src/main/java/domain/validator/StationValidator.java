package domain.validator;

import domain.Station;
import domain.exception.ValidationException;

public class StationValidator implements Validator<Station> 
{
    @Override
    public void validate(Station entity) throws ValidationException 
    {
        if (entity.getName() == null || entity.getName().trim().isEmpty()) 
        {
            throw new ValidationException("Station name cannot be empty");
        }
        if (entity.getLocation() == null || entity.getLocation().trim().isEmpty()) 
        {
            throw new ValidationException("Station location cannot be empty");
        }
    }
}
