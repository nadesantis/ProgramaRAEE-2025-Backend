package com.products.security.infrastructure.repository;

import com.products.security.domain.Role;
import com.products.security.domain.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  Optional<User> findByEmail(String email);
  boolean existsByEmailIgnoreCase(String email);
  boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

  @Query("""
		     select u from User u join u.roles r
		     where r = 'TECNICO'
		     order by u.name asc
		  """)
		  List<User> findTechnicians();

		
}
