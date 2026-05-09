package service;

import domain.Station;
import domain.validator.Validator;
import repository.Repository;

public class StationService extends Service<Integer, Station>
{
    public StationService(Repository<Integer, Station> repository, Validator<Station> validator)
    {
        super(repository, validator);
    }
}
