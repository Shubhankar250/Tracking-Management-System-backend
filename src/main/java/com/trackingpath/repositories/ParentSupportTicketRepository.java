package com.trackingpath.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.trackingpath.entities.ParentSupportTicket;

@Repository
public interface ParentSupportTicketRepository extends JpaRepository<ParentSupportTicket, Long> {
    List<ParentSupportTicket> findByParentIdOrderByIdDesc(Long parentId);
}
