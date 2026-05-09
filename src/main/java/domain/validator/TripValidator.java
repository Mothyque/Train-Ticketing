package domain.validator;

import domain.Trip;
import domain.exception.ValidationException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TripValidator implements Validator<Trip>
{
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(java.time.format.ResolverStyle.STRICT);

    public LocalTime parseTime(String time, String fieldName) throws ValidationException
    {
        if (time == null || time.trim().isEmpty())
        {
            throw new ValidationException(fieldName + " cannot be empty");
        }

        try
        {
            return LocalTime.parse(time.trim(), TIME_FORMATTER);
        }
        catch (DateTimeParseException ex)
        {
            throw new ValidationException(fieldName + " must be in HH:mm format and contain a valid hour");
        }
    }

    @Override
    public void validate(Trip entity) throws ValidationException
    {
        if (entity.getTrain() == null)
        {
            throw new ValidationException("Trip train cannot be null");
        }
        if (entity.getRoute() == null)
        {
            throw new ValidationException("Trip route cannot be null");
        }
        if (entity.getDepartureTime() == null)
        {
            throw new ValidationException("Departure time cannot be null");
        }
        if (entity.getArrivalTime() == null)
        {
            throw new ValidationException("Arrival time cannot be null");
        }
        if (!entity.getDepartureTime().isBefore(entity.getArrivalTime()))
        {
            throw new ValidationException("Departure time must be before arrival time");
        }
        if (entity.getDelay() == null || entity.getDelay() < 0)
        {
            throw new ValidationException("Delay cannot be negative");
        }
    }
}
