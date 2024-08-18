package pl.mpietrewicz.crudresthal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pl.mpietrewicz.crudresthal.entity.Company;

@RepositoryRestResource
public interface CompanyRepository extends JpaRepository<Company, Long> { }