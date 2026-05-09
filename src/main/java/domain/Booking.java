package domain;

public class Booking extends Entity<Integer>
{
    private String email;
    private Trip trip;
    private Integer seats;

    public Booking(String email, Trip trip, Integer seats)
    {
        this.email = email;
        this.trip = trip;
        this.seats = seats;
    }

    public String getEmail()
    {
        return email;
    }

    public Trip getTrip()
    {
        return trip;
    }

    public Integer getSeatCount()
    {
        return seats;
    }

    public void setSeatCount(Integer seats)
    {
        this.seats = seats;
    }

    public void setTrip(Trip trip)
    {
        this.trip = trip;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}