package com.trackingpath.services;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.trackingpath.entities.Dgm;
import com.trackingpath.repositories.RCDRepository;
import com.trackingpath.services.RCDService;

@Service
public class RCDServiceImpl implements RCDService {

    @Autowired
    private RCDRepository repo;

    @Override
    public List<Dgm> getAll() {
        return repo.findAll();
    }

    @Override
    public Dgm getById(Long id) {
        return repo.findById(id).orElse(null);
    }
}
