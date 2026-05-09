package service;

import domain.*;
import domain.exception.ValidationException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminService 
{
    private final TripService tripService;
    private final BookingService bookingService;
    private final TrainService trainService;
    private final RouteService routeService;
    private final StationService stationService;

    public AdminService(TripService tripService, BookingService bookingService, 
                       TrainService trainService, RouteService routeService, StationService stationService)
    {
        this.tripService = tripService;
        this.bookingService = bookingService;
        this.trainService = trainService;
        this.routeService = routeService;
        this.stationService = stationService;
    }

    public void addTrain(Integer capacity) throws ValidationException 
    {
        Train train = new Train(capacity);
        Optional<Train> result = trainService.add(train);
        if (result.isPresent()) 
        {
            System.out.println("Train added successfully with ID: " + result.get().getId());
        }
    }

    public void removeTrain(Integer trainId) 
    {
        Optional<Train> deleted = trainService.delete(trainId);
        if (deleted.isPresent()) 
        {
            System.out.println("Train removed successfully");
        } 
        else 
        {
            System.out.println("Train not found");
        }
    }

    public void modifyTrain(Integer trainId, Integer newCapacity) throws ValidationException 
    {
        Optional<Train> trainOpt = trainService.findOne(trainId);
        if (trainOpt.isPresent()) 
        {
            Train train = trainOpt.get();
            train.setCapacity(newCapacity);
            Optional<Train> updated = trainService.update(train);
            if (updated.isPresent()) 
            {
                System.out.println("Train modified successfully");
            }
        } 
        else 
        {
            System.out.println("Train not found");
        }
    }

    public void showAllTrains() 
    {
        List<Train> trains = (List<Train>) trainService.findAll();
        if (trains.isEmpty()) 
        {
            System.out.println("No trains found");
            return;
        }
        System.out.println("\n===== ALL TRAINS =====");
        for (Train train : trains) 
        {
            System.out.println("ID: " + train.getId() + " | Capacity: " + train.getCapacity());
        }
    }

    public void addStation(String name, String location) throws ValidationException 
    {
        Station station = new Station(name, location);
        Optional<Station> result = stationService.add(station);
        if (result.isPresent()) 
        {
            System.out.println("Station added successfully with ID: " + result.get().getId());
        }
    }

    public void removeStation(Integer stationId) 
    {
        Optional<Station> deleted = stationService.delete(stationId);
        if (deleted.isPresent()) 
        {
            System.out.println("Station removed successfully");
        } 
        else 
        {
            System.out.println("Station not found");
        }
    }

    public void modifyStation(Integer stationId, String name, String location) throws ValidationException 
    {
        Optional<Station> stationOpt = stationService.findOne(stationId);
        if (stationOpt.isPresent()) 
        {
            Station station = stationOpt.get();
            station.setName(name);
            station.setLocation(location);
            Optional<Station> updated = stationService.update(station);
            if (updated.isPresent()) 
            {
                System.out.println("Station modified successfully");
            }
        } 
        else 
        {
            System.out.println("Station not found");
        }
    }

    public void showAllStations() 
    {
        List<Station> stations = (List<Station>) stationService.findAll();
        if (stations.isEmpty()) 
        {
            System.out.println("No stations found");
            return;
        }
        System.out.println("\n===== ALL STATIONS =====");
        for (Station station : stations) 
        {
            System.out.println("ID: " + station.getId() + " | " + station.toString());
        }
    }

    public void addRoute(List<Integer> stationIds) throws ValidationException 
    {
        Route route = new Route();
        for (Integer stationId : stationIds) 
        {
            Optional<Station> station = stationService.findOne(stationId);
            if (station.isPresent()) 
            {
                route.addStation(station.get());
            } 
            else 
            {
                System.out.println("Station with ID " + stationId + " not found");
                return;
            }
        }
        Optional<Route> result = routeService.add(route);
        if (result.isPresent()) 
        {
            System.out.println("Route added successfully with ID: " + result.get().getId());
        }
    }

    public void removeRoute(Integer routeId) 
    {
        Optional<Route> deleted = routeService.delete(routeId);
        if (deleted.isPresent()) 
        {
            System.out.println("Route removed successfully");
        } 
        else 
        {
            System.out.println("Route not found");
        }
    }

    public void showAllRoutes() 
    {
        List<Route> routes = (List<Route>) routeService.findAll();
        if (routes.isEmpty()) 
        {
            System.out.println("No routes found");
            return;
        }
        System.out.println("\n===== ALL ROUTES =====");
        for (Route route : routes) 
        {
            System.out.println("Route ID: " + route.getId());
            System.out.println(route.toString());
        }
    }

    public void showBookingsForTrain(Integer trainId) 
    {
        List<Booking> trainBookings = new ArrayList<>();
        for (Booking booking : bookingService.findAll()) 
        {
            if (booking.getTrip().getTrain().getId().equals(trainId)) 
            {
                trainBookings.add(booking);
            }
        }

        if (trainBookings.isEmpty()) 
        {
            System.out.println("No bookings found for this train");
            return;
        }

        int totalTickets = 0;
        System.out.println("\n===== BOOKINGS FOR TRAIN " + trainId + " =====");
        for (Booking booking : trainBookings) 
        {
            System.out.println("Booking ID: " + booking.getId() + 
                             " | Email: " + booking.getEmail() + 
                             " | Tickets: " + booking.getSeatCount() +
                             " | Trip ID: " + booking.getTrip().getId());
            totalTickets += booking.getSeatCount();
        }
        System.out.println("Total Tickets Booked: " + totalTickets);
    }

    public void showAllBookings() 
    {
        List<Booking> allBookings = (List<Booking>) bookingService.findAll();
        if (allBookings.isEmpty()) 
        {
            System.out.println("No bookings found");
            return;
        }
        System.out.println("\n===== ALL BOOKINGS =====");
        for (Booking booking : allBookings) 
        {
            System.out.println("Booking ID: " + booking.getId() + 
                             " | Customer: " + booking.getEmail() + 
                             " | Tickets: " + booking.getSeatCount() +
                             " | Trip ID: " + booking.getTrip().getId());
        }
    }

    public void updateTripDelay(Integer tripId, int delayMinutes) 
    {
        tripService.updateDelayAndNotify(tripId, delayMinutes, bookingService);
        System.out.println("Trip delay updated and customers notified");
    }

    public void addTrip(Integer trainId, Integer routeId, String departureTime, String arrivalTime) throws ValidationException
    {
        Optional<Train> trainOpt = trainService.findOne(trainId);
        Optional<Route> routeOpt = routeService.findOne(routeId);

        if (!trainOpt.isPresent())
        {
            System.out.println("Train not found");
            return;
        }
        if (!routeOpt.isPresent())
        {
            System.out.println("Route not found");
            return;
        }

        Optional<Trip> result = tripService.addTrip(trainOpt.get(), routeOpt.get(), departureTime, arrivalTime);
        if (result.isPresent())
        {
            System.out.println("Trip added successfully with ID: " + result.get().getId());
        }
    }

    public void removeTrip(Integer tripId)
    {
        Optional<Trip> deleted = tripService.delete(tripId);
        if (deleted.isPresent())
        {
            System.out.println("Trip removed successfully");
        }
        else
        {
            System.out.println("Trip not found");
        }
    }

    public void showAllTrips()
    {
        List<Trip> trips = (List<Trip>) tripService.findAll();
        if (trips.isEmpty())
        {
            System.out.println("No trips found");
            return;
        }
        System.out.println("\n===== ALL TRIPS =====");
        for (Trip trip : trips)
        {
            System.out.println("Trip ID: " + trip.getId() +
                             " | Train: " + trip.getTrain().getId() +
                             " | Route: " + trip.getRoute().getId() +
                             " | Departure: " + trip.getDepartureTime() +
                             " | Arrival: " + trip.getArrivalTime() +
                             " | Delay: " + trip.getDelay() + " min");
        }
    }
}