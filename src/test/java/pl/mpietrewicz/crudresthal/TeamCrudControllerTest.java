package pl.mpietrewicz.crudresthal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.mpietrewicz.crudresthal.entity.Manager;
import pl.mpietrewicz.crudresthal.entity.Project;
import pl.mpietrewicz.crudresthal.entity.Team;
import pl.mpietrewicz.crudresthal.repository.TeamRepository;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("local")
public class TeamCrudControllerTest {

    private static final String TEAMS_ENDPOINT = "http://localhost:8080/teams";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TeamRepository teamRepository;

    @Before
    public void before() {
        Team team = createTeam("existing_team@test.com");
        teamRepository.save(team);
    }


    @After
    public void after() {
        teamRepository.deleteAll();
    }

    @Test
    public void shouldGetAllTeams() {
        Team otherTeam = createTeam("other_team@test.com");
        teamRepository.save(otherTeam);

        CollectionModel<EntityModel<Team>> teams = getTeams();

        assertThat(teams).isNotNull();
        assertThat(teams.getContent()).hasSize(2);
    }

    @Test
    public void shouldGetTeamById() {
        Optional<EntityModel<Team>> existingTeam = getTeams("existing_team@test.com");
        assertThat(existingTeam).isPresent();
    }

    @Test
    public void shouldCreateNewTeam() {
        Team team = createTeam("new_team@test.com");

        ResponseEntity<Team> postResponse = restTemplate.postForEntity(TEAMS_ENDPOINT, team, Team.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(postResponse.getBody()).getProject().getManager().getContactInfo())
                .isEqualTo("new_team@test.com");
        assertThat(getTeams("new_team@test.com")).isPresent();
    }

    @Test
    public void shouldUpdateTeam() {
        Optional<EntityModel<Team>> existingTeam = getTeams("existing_team@test.com");
        assertThat(existingTeam).isPresent();
        Link selfLink = existingTeam.get().getLink("self")
                .orElseThrow(() -> new AssertionError("Self link not found"));

        Team updatedTeam = createTeam("updated_team@test.com");
        restTemplate.put(selfLink.getHref(), updatedTeam, Team.class);

        assertThat(getTeams("existing_team@test.com")).isEmpty();
        assertThat(getTeams("updated_team@test.com")).isPresent();
    }

    @Test
    public void shouldDeleteTeam() {
        Optional<EntityModel<Team>> existingTeam = getTeams("existing_team@test.com");
        assertThat(existingTeam).isPresent();
        Link selfLink = existingTeam.get().getLink("self")
                .orElseThrow(() -> new AssertionError("Self link not found"));

        restTemplate.delete(selfLink.getHref());

        ResponseEntity<Team> getResponse = restTemplate.getForEntity(selfLink.getHref(), Team.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Optional<EntityModel<Team>> getTeams(String contactInfo) {
        CollectionModel<EntityModel<Team>> teams = getTeams();
        assertThat(teams).isNotNull();

        return teams.getContent().stream()
                .filter(entityModel -> contactInfo.equals(Objects.requireNonNull(entityModel.getContent())
                        .getProject().getManager().getContactInfo()))
                .findFirst();
    }

    private CollectionModel<EntityModel<Team>> getTeams() {
        ResponseEntity<CollectionModel<EntityModel<Team>>> response = restTemplate.exchange(
                TEAMS_ENDPOINT,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private static Team createTeam(String contactInfo) {
        Team team = new Team();
        Project project = new Project();
        Manager manager = new Manager();
        manager.setContactInfo(contactInfo);
        project.setManager(manager);
        team.setProject(project);
        return team;
    }

}