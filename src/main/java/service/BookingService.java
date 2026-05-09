package service;
import domain.Booking;
import domain.Trip;
import domain.exception.ValidationException;
import domain.validator.Validator;
import repository.BookingDBRepository;
import utils.EmailService;

import java.util.Optional;

public class BookingService extends Service<Integer, Booking> 
{
    private final BookingDBRepository bookingRepo;
    private final EmailService emailService;

    public BookingService(BookingDBRepository repository, Validator<Booking> validator)
    {
        super(repository, validator);
        this.bookingRepo = repository;
        this.emailService = EmailService.getInstance();
    }

    @Override
    public Optional<Booking> add(Booking booking) throws ValidationException
    {
        int capacity = booking.getTrip().getTrain().getCapacity();
        int currentlyBooked = getCurrentlyBookedSeats(booking.getTrip());

        if (currentlyBooked + booking.getSeatCount() > capacity) 
        {
            throw new ValidationException("Overbooking detected! Only " + (capacity - currentlyBooked) + " seats left.");
        }

        Optional<Booking> result = super.add(booking);

        if (result.isPresent()) 
        {
            String subject = "Booking Confirmation";
            String body = "You have successfully booked " + booking.getSeatCount() + 
                         " ticket(s) for trip with route: " + booking.getTrip().getRoute().toString() + 
                         "\nDeparture: " + booking.getTrip().getDepartureTime() + 
                         "\nArrival: " + booking.getTrip().getArrivalTime();
            emailService.send(booking.getEmail(), subject, body);
        }

        return result;
    }

    private int getCurrentlyBookedSeats(Trip trip) 
    {
        int count = 0;
        for (Booking b : findAll()) 
        {
            if (b.getTrip().getId().equals(trip.getId()))
            {
                count += b.getSeatCount();
            }
        }
        return count;
    }
}