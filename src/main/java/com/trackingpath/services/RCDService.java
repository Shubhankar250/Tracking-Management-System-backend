package com.trackingpath.services;

import java.util.List;
import com.trackingpath.entities.*;

public interface RCDService {
    List<Dgm> getAll();
    Dgm getById(Long id);
}
