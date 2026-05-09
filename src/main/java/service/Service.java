package service;


import domain.Entity;
import domain.exception.ValidationException;
import domain.validator.Validator;
import repository.Repository;
import utils.Observable;

import java.util.Optional;

public class Service<ID, E extends Entity<ID>> extends Observable
{
    private final Repository<ID, E> repository;
    private final Validator<E> validator;

    public Service(Repository<ID, E> repository, Validator<E> validator)
    {
        this.repository = repository;
        this.validator = validator;
    }

    public Service(Repository<ID, E> repository)
    {
        this.repository = repository;
        this.validator = null;
    }

    public Optional<E> add(E entity) throws ValidationException
    {
        if (validator != null)
        {
            validator.validate(entity);
        }

        Optional<E> saved = repository.save(entity);

        if (saved.isPresent())
        {
            notifyObservers();
        }
        return saved;
    }

    public Optional<E> delete(ID id)
    {
        Optional<E> deleted = repository.delete(id);
        if (deleted.isPresent())
        {
            notifyObservers();
        }
        return deleted;
    }

    public Optional<E> update(E entity) throws ValidationException
    {
        if (validator != null)
        {
            validator.validate(entity);
        }

        Optional<E> updated = repository.update(entity);
        if (updated.isPresent())
        {
            notifyObservers();
        }
        return updated;
    }

    public Optional<E> findOne(ID id)
    {
        return repository.findOne(id);
    }

    public Iterable<E> findAll()
    {
        return repository.findAll();
    }

    public int size()
    {
        return repository.size();
    }
}