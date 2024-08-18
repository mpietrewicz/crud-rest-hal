package pl.mpietrewicz.crudresthal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pl.mpietrewicz.crudresthal.entity.Company;
import pl.mpietrewicz.crudresthal.entity.Department;

@RepositoryRestResource
public interface DepartmentRepository extends JpaRepository<Department, Long> { }