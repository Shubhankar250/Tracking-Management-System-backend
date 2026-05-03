package com.trackingpath.repositories;


import com.trackingpath.entities.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {


	   List<Menu> findByParentIsNullAndRole_Id(Long roleId);

	    List<Menu> findByParent_IdAndRole_Id(Long parentId, Long roleId);

		List<Menu> findByParentIsNullAndRole_IdIn(List<Long> roleIds);

		List<Menu> findByParent_IdAndRole_IdIn(Long id, List<Long> roleIds);
}
