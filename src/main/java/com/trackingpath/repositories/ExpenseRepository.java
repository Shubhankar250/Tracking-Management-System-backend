package com.trackingpath.repositories;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trackingpath.dtos.ExpenseDTO;
import com.trackingpath.entities.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT SUM(e.cost) FROM Expense e WHERE e.date = :date")
    Double findTotalExpense(@Param("date") String date);
    
    @Query("""
        SELECT new com.trackingpath.dtos.ExpenseDTO(
            e.id,
            e.expenseName,
            e.date,
            e.expenseOdometer,
            e.cost,
            e.description,
            e.supplier,
            e.buyer,
            e.quantity,
            e.engineHour,
            e.adminId,
            e.userId,
            d.id,
            d.name
        )
        FROM Expense e
        LEFT JOIN e.device d
        WHERE e.userId = :userId
    """)
    Page<ExpenseDTO> findByUserIdWithDevice(
        @Param("userId") Long userId,
        Pageable pageable
    );

    @Query("""
        SELECT new com.trackingpath.dtos.ExpenseDTO(
            e.id,
            e.expenseName,
            e.date,
            e.expenseOdometer,
            e.cost,
            e.description,
            e.supplier,
            e.buyer,
            e.quantity,
            e.engineHour,
            e.adminId,
            e.userId,
            d.id,
            d.name
        )
        FROM Expense e
        LEFT JOIN e.device d
        WHERE e.userId = :userId 
        AND (
            e.expenseName LIKE :search 
            OR e.supplier LIKE :search 
            OR e.buyer LIKE :search
        )
    """)
    Page<ExpenseDTO> searchByUserIdWithDevice(
        @Param("userId") Long userId,
        @Param("search") String search,
        Pageable pageable
    );
}
