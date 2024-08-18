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
import org.springframework.test.context.junit4.SpringRunner;
import pl.mpietrewicz.crudresthal.entity.Department;
import pl.mpietrewicz.crudresthal.repository.DepartmentRepository;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class DepartmentCrudControllerTest {

    private static final String DEPARTMENTS_ENDPOINT = "http://localhost:8080/departments";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Before
    public void before() {
        Department department = new Department();
        department.setName("Existing department");
        departmentRepository.save(department);
    }

    @After
    public void after() {
        departmentRepository.deleteAll();
    }

    @Test
    public void shouldGetAllDepartments() {
        Department otherDepartment = new Department();
        otherDepartment.setName("Other department");
        departmentRepository.save(otherDepartment);

        CollectionModel<EntityModel<Department>> departments = getDepartments();

        assertThat(departments).isNotNull();
        assertThat(departments.getContent()).hasSize(2);
    }

    @Test
    public void shouldGetDepartmentById() {
        Optional<EntityModel<Department>> existingDepartment = getDepartments("Existing department");
        assertThat(existingDepartment).isPresent();
    }

    @Test
    public void shouldCreateNewDepartment() {
        Department department = new Department();
        department.setName("New department");

        ResponseEntity<Department> postResponse = restTemplate.postForEntity(DEPARTMENTS_ENDPOINT, department, Department.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(postResponse.getBody()).getName()).isEqualTo("New department");
        assertThat(getDepartments("New department")).isPresent();
    }

    @Test
    public void shouldUpdateDepartment() {
        Optional<EntityModel<Department>> existingDepartment = getDepartments("Existing department");
        assertThat(existingDepartment).isPresent();
        Link selfLink = existingDepartment.get().getLink("self")
                .orElseThrow(() -> new AssertionError("Self link not found"));

        Department updatedDepartment = new Department();
        updatedDepartment.setName("Updated department");
        restTemplate.put(selfLink.getHref(), updatedDepartment, Department.class);

        assertThat(getDepartments("Existing department")).isEmpty();
        assertThat(getDepartments("Updated department")).isPresent();
    }

    @Test
    public void shouldDeleteDepartment() {
        Optional<EntityModel<Department>> existingDepartment = getDepartments("Existing department");
        assertThat(existingDepartment).isPresent();
        Link selfLink = existingDepartment.get().getLink("self")
                .orElseThrow(() -> new AssertionError("Self link not found"));

        restTemplate.delete(selfLink.getHref());

        ResponseEntity<Department> getResponse = restTemplate.getForEntity(selfLink.getHref(), Department.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Optional<EntityModel<Department>> getDepartments(String name) {
        CollectionModel<EntityModel<Department>> departments = getDepartments();
        assertThat(departments).isNotNull();

        return departments.getContent().stream()
                .filter(entityModel -> name.equals(Objects.requireNonNull(entityModel.getContent()).getName()))
                .findFirst();
    }

    private CollectionModel<EntityModel<Department>> getDepartments() {
        ResponseEntity<CollectionModel<EntityModel<Department>>> response = restTemplate.exchange(
                DEPARTMENTS_ENDPOINT,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

}