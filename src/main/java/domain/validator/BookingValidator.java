package domain.validator;

import domain.Booking;
import domain.exception.ValidationException;

public class BookingValidator implements Validator<Booking>
{
    @Override
    public void validate(Booking entity) throws ValidationException
    {
        if (entity.getEmail() == null || entity.getEmail().trim().isEmpty())
        {
            throw new ValidationException("Email cannot be empty");
        }
        if (!entity.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$"))
        {
            throw new ValidationException("Invalid email format");
        }
        if (entity.getTrip() == null)
        {
            throw new ValidationException("Trip cannot be null");
        }
        if (entity.getSeatCount() == null || entity.getSeatCount() <= 0)
        {
            throw new ValidationException("Seat count must be greater than 0");
        }
    }
}
