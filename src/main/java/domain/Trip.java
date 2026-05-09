package domain;

import java.time.LocalTime;

public class Trip extends Entity<Integer>
{
    private Train train;
    private Route route;
    private LocalTime departureTime;
    private Integer delay;
    private LocalTime arrivalTime;

    public Trip(Train train, Route route, LocalTime departureTime, Integer delay, LocalTime arrivalTime)
    {
        this.train = train;
        this.route = route;
        this.departureTime = departureTime;
        this.delay = delay;
        this.arrivalTime = arrivalTime;
    }

    public Train getTrain()
    {
        return train;
    }

    public void setTrain(Train train)
    {
        this.train = train;
    }

    public Route getRoute()
    {
        return route;
    }

    public void setRoute(Route route)
    {
        this.route = route;
    }

    public LocalTime getDepartureTime()
    {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime)
    {
        this.departureTime = departureTime;
    }

    public Integer getDelay()
    {
        return delay;
    }

    public void setDelay(Integer delay)
    {
        this.delay = delay;
    }

    public LocalTime getArrivalTime()
    {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime)
    {
        this.arrivalTime = arrivalTime;
    }

}