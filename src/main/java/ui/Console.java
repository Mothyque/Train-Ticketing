package ui;

import domain.*;
import domain.exception.ValidationException;
import service.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Console
{
    private Scanner scanner;
    private BookingService bookingService;
    private TripService tripService;
    private TrainService trainService;
    private StationService stationService;
    private RouteService routeService;
    private AdminService adminService;

    public Console(BookingService bookingService, TripService tripService, TrainService trainService,
                   StationService stationService, RouteService routeService, AdminService adminService)
    {
        this.scanner = new Scanner(System.in);
        this.bookingService = bookingService;
        this.tripService = tripService;
        this.trainService = trainService;
        this.stationService = stationService;
        this.routeService = routeService;
        this.adminService = adminService;
    }

    public void start()
    {
        printWelcome();
        boolean running = true;
        while (running)
        {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice)
            {
                case "1":
                    bookTicket();
                    break;
                case "2":
                    findRoute();
                    break;
                case "3":
                    adminPanel();
                    break;
                case "4":
                    running = false;
                    printGoodbye();
                    break;
                default:
                    printInvalidOption();
            }
        }
        scanner.close();
    }

    private void printWelcome()
    {
        System.out.println("\n[TRAIN TICKETING APPLICATION]");
        System.out.println("[Welcome to Train Booking System]\n");
    }

    private void printMainMenu()
    {
        System.out.println("\n========== MAIN MENU ==========");
        System.out.println("1. Book a ticket");
        System.out.println("2. Find departure/arrival times");
        System.out.println("3. Admin Panel");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");
    }

    private void printGoodbye()
    {
        System.out.println("\nThank you for using our service. Goodbye!");
    }

    private void printInvalidOption()
    {
        System.out.println("Invalid option. Please try again.");
    }

    private void bookTicket()
    {
        System.out.println("\n========== BOOK TICKET ==========");

        List<Trip> trips = (List<Trip>) tripService.findAll();
        if (trips.isEmpty())
        {
            System.out.println("No trips available at the moment");
            return;
        }

        displayAvailableTrips(trips);

        System.out.print("Select trip number: ");
        try
        {
            int tripIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (tripIndex < 0 || tripIndex >= trips.size())
            {
                System.out.println("Invalid trip selection");
                return;
            }

            Trip selectedTrip = trips.get(tripIndex);

            System.out.print("Enter email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Enter number of seats: ");
            int seats = Integer.parseInt(scanner.nextLine().trim());

            Booking booking = new Booking(email, selectedTrip, seats);
            Optional<Booking> result = bookingService.add(booking);

            if (result.isPresent())
            {
                System.out.println("\nBooking successful!");
                System.out.println("Booking ID: " + result.get().getId());
                System.out.println("Confirmation email sent to: " + email);
            }
        }
        catch (NumberFormatException e)
        {
            System.out.println("Invalid input. Please enter valid numbers.");
        }
        catch (ValidationException e)
        {
            System.out.println("Booking failed: " + e.getMessage());
        }
    }

    private void displayAvailableTrips(List<Trip> trips)
    {
        System.out.println("\n--- Available Trips ---");
        for (int i = 0; i < trips.size(); i++)
        {
            Trip trip = trips.get(i);
            System.out.println((i + 1) + ". Trip ID: " + trip.getId() +
                             " | Train: " + trip.getTrain().getId() +
                             " | Departure: " + trip.getDepartureTime() +
                             " | Arrival: " + trip.getArrivalTime() +
                             " | Route stations: " + trip.getRoute().getStations().size());
        }
    }

    private void findRoute()
    {
        System.out.println("\n========== FIND ROUTE ==========");

        List<Station> stations = (List<Station>) stationService.findAll();
        if (stations.size() < 2)
        {
            System.out.println("Not enough stations available");
            return;
        }

        displayAvailableStations(stations);

        System.out.print("Select departure station number: ");
        try
        {
            int departIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (departIndex < 0 || departIndex >= stations.size())
            {
                System.out.println("Invalid station selection");
                return;
            }

            System.out.print("Select arrival station number: ");
            int arrivalIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (arrivalIndex < 0 || arrivalIndex >= stations.size())
            {
                System.out.println("Invalid station selection");
                return;
            }

            if (departIndex == arrivalIndex)
            {
                System.out.println("Departure and arrival stations must be different");
                return;
            }

            Station startStation = stations.get(departIndex);
            Station endStation = stations.get(arrivalIndex);

            List<List<Trip>> routes = tripService.findRoutes(startStation, endStation);

            if (routes.isEmpty())
            {
                System.out.println("\nNo routes found between " + startStation.getName() +
                                 " and " + endStation.getName());
                return;
            }

            displayAvailableRoutes(routes);
        }
        catch (NumberFormatException e)
        {
            System.out.println("Invalid input. Please enter valid numbers.");
        }
    }

    private void displayAvailableStations(List<Station> stations)
    {
        System.out.println("\n--- Available Stations ---");
        for (int i = 0; i < stations.size(); i++)
        {
            System.out.println((i + 1) + ". " + stations.get(i).toString());
        }
    }

    private void displayAvailableRoutes(List<List<Trip>> routes)
    {
        System.out.println("\n--- Available Routes ---");
        for (int i = 0; i < routes.size(); i++)
        {
            List<Trip> route = routes.get(i);
            System.out.println("\nRoute " + (i + 1) + ":");
            LocalTime currentDeparture = null;
            LocalTime currentArrival = null;

            for (int j = 0; j < route.size(); j++)
            {
                Trip trip = route.get(j);
                displayTripLeg(trip, j + 1);

                if (j == 0) currentDeparture = trip.getDepartureTime();
                if (j == route.size() - 1) currentArrival = trip.getArrivalTime();
            }
            System.out.println("Total journey time: " + currentDeparture + " to " + currentArrival);
        }
    }

    private void displayTripLeg(Trip trip, int legNumber)
    {
        System.out.println("  Leg " + legNumber + ": Train " + trip.getTrain().getId() +
                         " | Departure: " + trip.getDepartureTime() +
                         " | Arrival: " + trip.getArrivalTime() +
                         " | Delay: " + trip.getDelay() + " min");

        int bookedSeats = 0;
        for (Booking b : bookingService.findAll())
        {
            if (b.getTrip().getId().equals(trip.getId()))
            {
                bookedSeats += b.getSeatCount();
            }
        }
        int availableSeats = trip.getTrain().getCapacity() - bookedSeats;
        System.out.println("    Available seats: " + availableSeats + "/" + trip.getTrain().getCapacity());
    }

    private void adminPanel()
    {
        System.out.print("\nEnter admin password: ");
        String password = scanner.nextLine().trim();

        if (!password.equals("admin123"))
        {
            System.out.println("Incorrect password");
            return;
        }

        System.out.println("\n[ADMIN PANEL]");

        boolean adminRunning = true;
        while (adminRunning)
        {
            printAdminMenu();
            String choice = scanner.nextLine().trim();
            switch (choice)
            {
                case "1":
                    manageTrains();
                    break;
                case "2":
                    manageStations();
                    break;
                case "3":
                    manageRoutes();
                    break;
                case "4":
                    manageTrips();
                    break;
                case "5":
                    viewBookings();
                    break;
                case "6":
                    handleDelays();
                    break;
                case "7":
                    adminRunning = false;
                    System.out.println("Exiting admin panel");
                    break;
                default:
                    printInvalidOption();
            }
        }
    }

    private void printAdminMenu()
    {
        System.out.println("\n========== ADMIN MENU ==========");
        System.out.println("1. Manage Trains");
        System.out.println("2. Manage Stations");
        System.out.println("3. Manage Routes");
        System.out.println("4. Manage Trips");
        System.out.println("5. View Bookings");
        System.out.println("6. Handle Delays");
        System.out.println("7. Exit Admin Panel");
        System.out.print("Choose an option: ");
    }

    private void manageTrains()
    {
        System.out.println("\n========== MANAGE TRAINS ==========");
        System.out.println("1. Add train");
        System.out.println("2. Remove train");
        System.out.println("3. Modify train");
        System.out.println("4. View all trains");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine().trim();
        try
        {
            switch (choice)
            {
                case "1":
                    addTrain();
                    break;
                case "2":
                    removeTrain();
                    break;
                case "3":
                    modifyTrain();
                    break;
                case "4":
                    adminService.showAllTrains();
                    break;
                default:
                    printInvalidOption();
            }
        }
        catch (NumberFormatException | ValidationException e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addTrain() throws ValidationException
    {
        System.out.print("Enter train capacity: ");
        int capacity = Integer.parseInt(scanner.nextLine().trim());
        adminService.addTrain(capacity);
    }

    private void removeTrain() throws NumberFormatException
    {
        adminService.showAllTrains();
        System.out.print("Enter train ID to remove: ");
        int trainId = Integer.parseInt(scanner.nextLine().trim());
        adminService.removeTrain(trainId);
    }

    private void modifyTrain() throws ValidationException
    {
        adminService.showAllTrains();
        System.out.print("Enter train ID to modify: ");
        int modifyId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter new capacity: ");
        int newCapacity = Integer.parseInt(scanner.nextLine().trim());
        adminService.modifyTrain(modifyId, newCapacity);
    }

    private void manageStations()
    {
        System.out.println("\n========== MANAGE STATIONS ==========");
        System.out.println("1. Add station");
        System.out.println("2. Remove station");
        System.out.println("3. Modify station");
        System.out.println("4. View all stations");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine().trim();
        try
        {
            switch (choice)
            {
                case "1":
                    addStation();
                    break;
                case "2":
                    removeStation();
                    break;
                case "3":
                    modifyStation();
                    break;
                case "4":
                    adminService.showAllStations();
                    break;
                default:
                    printInvalidOption();
            }
        }
        catch (NumberFormatException | ValidationException e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addStation() throws ValidationException
    {
        System.out.print("Enter station name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter station location: ");
        String location = scanner.nextLine().trim();
        adminService.addStation(name, location);
    }

    private void removeStation() throws NumberFormatException
    {
        adminService.showAllStations();
        System.out.print("Enter station ID to remove: ");
        int stationId = Integer.parseInt(scanner.nextLine().trim());
        adminService.removeStation(stationId);
    }

    private void modifyStation() throws ValidationException
    {
        adminService.showAllStations();
        System.out.print("Enter station ID to modify: ");
        int modifyId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter new name: ");
        String newName = scanner.nextLine().trim();
        System.out.print("Enter new location: ");
        String newLocation = scanner.nextLine().trim();
        adminService.modifyStation(modifyId, newName, newLocation);
    }

    private void manageRoutes()
    {
        System.out.println("\n========== MANAGE ROUTES ==========");
        System.out.println("1. Add route");
        System.out.println("2. Remove route");
        System.out.println("3. View all routes");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine().trim();
        try
        {
            switch (choice)
            {
                case "1":
                    addRoute();
                    break;
                case "2":
                    removeRoute();
                    break;
                case "3":
                    adminService.showAllRoutes();
                    break;
                default:
                    printInvalidOption();
            }
        }
        catch (NumberFormatException | ValidationException e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addRoute() throws ValidationException
    {
        adminService.showAllStations();
        System.out.print("Enter station IDs separated by commas (e.g., 1,2,3): ");
        String input = scanner.nextLine().trim();
        List<Integer> stationIds = new ArrayList<>();
        for (String id : input.split(","))
        {
            stationIds.add(Integer.parseInt(id.trim()));
        }
        adminService.addRoute(stationIds);
    }

    private void removeRoute() throws NumberFormatException
    {
        adminService.showAllRoutes();
        System.out.print("Enter route ID to remove: ");
        int routeId = Integer.parseInt(scanner.nextLine().trim());
        adminService.removeRoute(routeId);
    }

    private void manageTrips()
    {
        System.out.println("\n========== MANAGE TRIPS ==========");
        System.out.println("1. Add trip");
        System.out.println("2. Remove trip");
        System.out.println("3. View all trips");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine().trim();
        try
        {
            switch (choice)
            {
                case "1":
                    addTrip();
                    break;
                case "2":
                    removeTrip();
                    break;
                case "3":
                    adminService.showAllTrips();
                    break;
                default:
                    printInvalidOption();
            }
        }
        catch (NumberFormatException | ValidationException e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addTrip() throws ValidationException
    {
        adminService.showAllTrains();
        System.out.print("Enter train ID: ");
        int trainId = Integer.parseInt(scanner.nextLine().trim());

        adminService.showAllRoutes();
        System.out.print("Enter route ID: ");
        int routeId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Enter departure time (HH:mm): ");
        String departureTime = scanner.nextLine().trim();

        System.out.print("Enter arrival time (HH:mm): ");
        String arrivalTime = scanner.nextLine().trim();

        adminService.addTrip(trainId, routeId, departureTime, arrivalTime);
    }

    private void removeTrip() throws NumberFormatException
    {
        adminService.showAllTrips();
        System.out.print("Enter trip ID to remove: ");
        int tripId = Integer.parseInt(scanner.nextLine().trim());
        adminService.removeTrip(tripId);
    }

    private void viewBookings()
    {
        System.out.println("\n========== VIEW BOOKINGS ==========");
        System.out.println("1. View all bookings");
        System.out.println("2. View bookings for specific train");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine().trim();
        try
        {
            switch (choice)
            {
                case "1":
                    adminService.showAllBookings();
                    break;
                case "2":
                    viewBookingsForTrain();
                    break;
                default:
                    printInvalidOption();
            }
        }
        catch (NumberFormatException e)
        {
            System.out.println("Invalid input");
        }
    }

    private void viewBookingsForTrain() throws NumberFormatException
    {
        adminService.showAllTrains();
        System.out.print("Enter train ID: ");
        int trainId = Integer.parseInt(scanner.nextLine().trim());
        adminService.showBookingsForTrain(trainId);
    }

    private void handleDelays()
    {
        System.out.println("\n========== HANDLE DELAYS ==========");

        List<Trip> trips = (List<Trip>) tripService.findAll();
        if (trips.isEmpty())
        {
            System.out.println("No trips available");
            return;
        }

        displayTripsForDelay(trips);

        try
        {
            System.out.print("Select trip number: ");
            int tripIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (tripIndex < 0 || tripIndex >= trips.size())
            {
                System.out.println("Invalid trip selection");
                return;
            }

            Trip selectedTrip = trips.get(tripIndex);
            System.out.print("Enter delay in minutes: ");
            int delayMinutes = Integer.parseInt(scanner.nextLine().trim());

            adminService.updateTripDelay(selectedTrip.getId(), delayMinutes);
        }
        catch (NumberFormatException e)
        {
            System.out.println("Invalid input");
        }
    }

    private void displayTripsForDelay(List<Trip> trips)
    {
        System.out.println("\n--- Available Trips ---");
        for (int i = 0; i < trips.size(); i++)
        {
            Trip trip = trips.get(i);
            System.out.println((i + 1) + ". Trip ID: " + trip.getId() +
                             " | Departure: " + trip.getDepartureTime() +
                             " | Current Delay: " + trip.getDelay() + " min");
        }
    }
}

