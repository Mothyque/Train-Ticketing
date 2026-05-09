package service;

import domain.Booking;
import domain.Route;
import domain.Station;
import domain.Train;
import domain.Trip;
import domain.exception.ValidationException;
import domain.validator.TripValidator;
import repository.Repository;
import utils.EmailService;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TripService extends Service<Integer, Trip>
{
    private final EmailService emailService;
    private final TripValidator tripValidator;

    public TripService(Repository<Integer, Trip> repository, TripValidator validator)
    {
        super(repository, validator);
        this.tripValidator = validator;
        this.emailService = EmailService.getInstance();
    }

    public Optional<Trip> addTrip(Train train, Route route, String departureTime, String arrivalTime) throws ValidationException
    {
        LocalTime departure = tripValidator.parseTime(departureTime, "Departure time");
        LocalTime arrival = tripValidator.parseTime(arrivalTime, "Arrival time");
        Trip trip = new Trip(train, route, departure, 0, arrival);
        return add(trip);
    }

    public List<List<Trip>> findRoutes(Station start, Station end)
    {
        List<List<Trip>> allPossibleRoutes = new ArrayList<>();

        List<Trip> direct = findDirectTrips(start, end);
        for (Trip t : direct)
        {
            allPossibleRoutes.add(List.of(t));
        }

        for (Trip tripA : findAll())
        {
            if (tripA.getRoute().getStations().contains(start))
            {
                Station changeoverStation = getArrivalStation(tripA, start, end);
                if (changeoverStation != null)
                {
                    for (Trip tripB : findAll())
                    {
                        if (tripB.getRoute().getStations().contains(changeoverStation) &&
                                tripB.getRoute().getStations().contains(end))
                        {
                            if (tripB.getDepartureTime().isAfter(tripA.getArrivalTime()))
                            {
                                allPossibleRoutes.add(List.of(tripA, tripB));
                            }
                        }
                    }
                }
            }
        }
        return allPossibleRoutes;
    }

    public void updateDelayAndNotify(Integer tripId, Integer delayMinutes, BookingService bookingService)
    {
        Optional<Trip> tripOpt = findOne(tripId);
        if (tripOpt.isPresent())
        {
            Trip trip = tripOpt.get();
            trip.setDelay(delayMinutes);
            try
            {
                update(trip);
            }
            catch (ValidationException e)
            {
                System.out.println("Failed to update trip delay: " + e.getMessage());
                return;
            }

            for (Booking b : bookingService.findAll())
            {
                if (b.getTrip().getId().equals(tripId))
                {
                    String subject = "Train Delay Notification";
                    String body = "Important: Your train has been delayed by " + delayMinutes + " minutes.\n\n" +
                            "Route: " + b.getTrip().getRoute().toString() + "\n" +
                            "Original Departure: " + b.getTrip().getDepartureTime() + "\n" +
                            "New Estimated Departure: " + b.getTrip().getDepartureTime().plusMinutes(delayMinutes) + "\n" +
                            "Booking Reference: " + b.getId();
                    emailService.send(b.getEmail(), subject, body);
                }
            }
        }
    }

    private List<Trip> findDirectTrips(Station start, Station end)
    {
        List<Trip> results = new ArrayList<>();
        for (Trip trip : findAll())
        {
            List<Station> stops = trip.getRoute().getStations();
            int startIndex = stops.indexOf(start);
            int endIndex = stops.indexOf(end);
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex)
            {
                results.add(trip);
            }
        }
        return results;
    }

    private Station getArrivalStation(Trip tripA, Station start, Station end)
    {
        for (Station station : tripA.getRoute().getStations())
        {
            if (!station.equals(start) && !station.equals(end))
            {
                return station;
            }
        }
        return null;
    }
}