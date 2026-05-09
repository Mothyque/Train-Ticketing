package domain;

public class Train extends Entity<Integer>
{
    private Integer capacity;

    public Train(Integer capacity)
    {
        this.capacity = capacity;
    }

    public Integer getCapacity()
    {
        return capacity;
    }

    public void setCapacity(Integer capacity)
    {
        this.capacity = capacity;
    }

    private boolean isFull(Integer bookedTickets)
    {
        return bookedTickets > capacity;
    }
}