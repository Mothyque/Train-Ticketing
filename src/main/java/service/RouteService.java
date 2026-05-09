package service;

import domain.Route;
import domain.validator.Validator;
import repository.Repository;

public class RouteService extends Service<Integer, Route>
{
    public RouteService(Repository<Integer, Route> repository, Validator<Route> validator)
    {
        super(repository, validator);
    }
}
