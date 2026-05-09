package domain.validator;

import domain.Route;
import domain.exception.ValidationException;

public class RouteValidator implements Validator<Route> 
{
    @Override
    public void validate(Route entity) throws ValidationException 
    {
        if (entity.getStations() == null || entity.getStations().isEmpty()) 
        {
            throw new ValidationException("Route must have at least one station");
        }
        if (entity.getStations().size() < 2) 
        {
            throw new ValidationException("Route must have at least two stations");
        }
    }
}
