package com.trackingpath.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.trackingpath.dtos.ExpenseDTO;
import com.trackingpath.entities.Expense;
import com.trackingpath.entities.Users;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.ExpenseService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService service;

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private ActivityLogService activityLogService;

    // CREATE
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public String addExpense(@RequestBody Expense expense,HttpServletRequest request) {

        Users user = authenticationService.getUser();
        activityLogService.createActivity("NEW EXPENSE CREATED",
				"EXPENSE(" + expense.getExpenseName() + ") CREATED BY " + user.getUsername(), user, request);

        if (service.addExpense(expense, user))
            return "Added Successfully";
        else
            return "Failed!";
    }

    // READ ALL
    @GetMapping
    public Map<String, Object> getExpenses(   @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String search) {

        Users user = authenticationService.getUser();
        return service.getExpenseList(user, page, pageSize, search);
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ExpenseDTO getExpense(@PathVariable long id) {
        return service.getExpenseById(id);
    }

    // UPDATE
    @PutMapping
    public String updateExpense(@RequestBody Expense expense, HttpServletRequest request) {

        Users user = authenticationService.getUser();

        if (service.updateExpense(expense, user)) {

            // 🔥 ACTIVITY LOG
            activityLogService.createActivity(
                    "EXPENSE_UPDATED",
                    "EXPENSE (" + expense.getExpenseName() + ") UPDATED BY " + user.getUsername(),
                    user,
                    request
            );

            return "Updated Successfully";
        } else {
            return "Failed!";
        }
    }


    // DELETE
    @DeleteMapping("/{id}")
    public String deleteExpense(@PathVariable long id,HttpServletRequest request) {
    	Users user = authenticationService.getUser();
        if (service.deleteExpense(id)) {
        	
        	activityLogService.createActivity("EXPENSE DELETED",
					"EXPENSE DELETED WITH ID (" + id + ") DELETED BY " + user.getUsername(), user, request);	
            return "Deleted Successfully";
            
        } else {
            return "Failed!";
    }
    }
}
