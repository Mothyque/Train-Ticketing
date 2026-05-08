package domain;

import java.util.ArrayList;
import java.util.List;

public class Route extends Entity<Integer>
{
    private List<Station> stations;

    public Route()
    {
        this.stations = new ArrayList<>();
    }

    public Route(List<Station> stations)
    {
        this.stations = stations;
    }

    public List<Station> getStations()
    {
        return stations;
    }

    public void setStations(List<Station> stations)
    {
        this.stations = stations;
    }

    public void addStation(Station station)
    {
        this.stations.add(station);
    }

    public void removeStation(Station station)
    {
        this.stations.remove(station);
    }

    public int getStopCount()
    {
        return stations.size();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Route with ").append(stations.size()).append(" stops:\n");
        for (Station station : stations)
        {
            sb.append("- ").append(station.toString()).append("\n");
        }
        return sb.toString();
    }

}