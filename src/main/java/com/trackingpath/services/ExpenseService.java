package com.trackingpath.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackingpath.dtos.ExpenseDTO;
import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.Expense;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.repositories.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository repo;
    
    @Autowired
    private DeviceRepository deviceRepository;

    public boolean addExpense(Expense expense, Users user) {
        expense.setUserId(user.getId());
        //expense.setAdminId(user.getAdminId());
        if (expense.getDeviceId() != null) {
            DeviceEntity device = deviceRepository
                .findById(expense.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device not found"));

            expense.setDevice(device);
        }
        
        repo.save(expense);
        return true;
    }

    public Map<String, Object> getExpenseList(Users user, int page, int pageSize, String search) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("id").descending());
        Page<ExpenseDTO> expensePage;

        if (search != null && !search.isEmpty()) {
            expensePage = repo.searchByUserIdWithDevice(
                user.getId(), "%" + search + "%", pageable
            );
        } else {
            expensePage = repo.findByUserIdWithDevice(user.getId(), pageable);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("data", expensePage.getContent());
        res.put("totalRecords", expensePage.getTotalElements());

        return res;
    }

    @Transactional(readOnly = true)
    public ExpenseDTO getExpenseById(Long id) {

        Expense e = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Expense not found"));

        return new ExpenseDTO(
            e.getId(),
            e.getExpenseName(),
            e.getDate(),
            e.getExpenseOdometer(),
            e.getCost(),
            e.getDescription(),
            e.getSupplier(),
            e.getBuyer(),
            e.getQuantity(),
            e.getEngineHour(),
            e.getAdminId(),
            e.getUserId(),
            e.getDevice() != null ? e.getDevice().getId() : null,
            e.getDevice() != null ? e.getDevice().getName() : null
        );
    }

    public boolean updateExpense(Expense expense, Users user) {
        if (!repo.existsById(expense.getId())) return false;

        // Ensure user matches
        Expense old = repo.findById(expense.getId()).get();
       // if (!old.getAdminId().equals(user.getAdminId())) return false;

       // expense.setAdminId(user.getAdminId());
        expense.setUserId(user.getId());
        
     // ✅ HANDLE DEVICE PROPERLY
        if (expense.getDeviceId() != null) {
            DeviceEntity device = deviceRepository
                .findById(expense.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device not found"));

            expense.setDevice(device);
        } else {
            // keep old device if frontend didn't send it
            expense.setDevice(old.getDevice());
        }
        
        repo.save(expense);
        return true;
    }

    public boolean deleteExpense(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
