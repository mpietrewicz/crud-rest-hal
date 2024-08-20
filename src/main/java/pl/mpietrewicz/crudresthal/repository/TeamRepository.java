package pl.mpietrewicz.crudresthal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pl.mpietrewicz.crudresthal.entity.Team;

@RepositoryRestResource
public interface TeamRepository extends JpaRepository<Team, Long> { }