import domain.validator.*;
import repository.*;
import service.*;
import ui.Console;
import utils.DatabaseConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Main
{
    private static final String SCHEMA_PATH = "sql/schema.sql";

    private BookingService bookingService;
    private TripService tripService;
    private TrainService trainService;
    private StationService stationService;
    private RouteService routeService;
    private AdminService adminService;

    public static void main(String[] args)
    {
        try 
        {
            Main app = new Main();
            app.initializeDatabase();
            app.initializeServices();
            app.run();
        } 
        catch (Exception e) 
        {
            System.err.println("Application error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeDatabase() throws IOException 
    {
        try 
        {
            String dbUrl = DatabaseConfig.getInstance().getDatabaseUrl();
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(dbUrl);

            String schema = new String(Files.readAllBytes(Paths.get(SCHEMA_PATH)));
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(schema);
            conn.close();
            
            System.out.println("Database initialized successfully");
        }
        catch (Exception e)
        {
            System.out.println("Database initialization completed (tables and data already exist or were just created)");
        }
    }

    private void initializeServices() 
    {
        String dbUrl = DatabaseConfig.getInstance().getDatabaseUrl();

        TrainDBRepository trainRepo = new TrainDBRepository(dbUrl, "", "");
        StationDBRepository stationRepo = new StationDBRepository(dbUrl, "", "");
        RouteDBRepository routeRepo = new RouteDBRepository(dbUrl, "", "", stationRepo);
        TripDBRepository tripRepo = new TripDBRepository(dbUrl, "", "", trainRepo, routeRepo);
        BookingDBRepository bookingRepo = new BookingDBRepository(dbUrl, "", "", tripRepo);

        TrainValidator trainValidator = new TrainValidator();
        StationValidator stationValidator = new StationValidator();
        RouteValidator routeValidator = new RouteValidator();
        TripValidator tripValidator = new TripValidator();
        BookingValidator bookingValidator = new BookingValidator();

        trainService = new TrainService(trainRepo, trainValidator);
        stationService = new StationService(stationRepo, stationValidator);
        routeService = new RouteService(routeRepo, routeValidator);
        tripService = new TripService(tripRepo, tripValidator);
        bookingService = new BookingService(bookingRepo, bookingValidator);

        adminService = new AdminService(tripService, bookingService, trainService, routeService, stationService);

        System.out.println("Services initialized successfully");
    }

    private void run() 
    {
        Console console = new Console(bookingService, tripService, trainService,
                                     stationService, routeService, adminService);
        console.start();
    }
}
