package service;

import domain.Train;
import domain.validator.Validator;
import repository.Repository;

public class TrainService extends Service<Integer, Train>
{
    public TrainService(Repository<Integer, Train> repository, Validator<Train> validator)
    {
        super(repository, validator);
    }
}
