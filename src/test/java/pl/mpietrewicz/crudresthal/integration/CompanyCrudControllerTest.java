package pl.mpietrewicz.crudresthal.integration;

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
import pl.mpietrewicz.crudresthal.Application;
import pl.mpietrewicz.crudresthal.entity.Company;
import pl.mpietrewicz.crudresthal.repository.CompanyRepository;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("tc")
public class CompanyCrudControllerTest {

    private static final String COMPANIES_ENDPOINT = "http://localhost:8080/companies";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompanyRepository companyRepository;

    @Before
    public void before() {
        Company company = new Company();
        company.setName("Existing company");
        companyRepository.save(company);
    }

    @After
    public void after() {
        companyRepository.deleteAll();
    }

    @Test
    public void shouldGetAllCompanies() {
        Company otherCompany = new Company();
        otherCompany.setName("Other company");
        companyRepository.save(otherCompany);

        CollectionModel<EntityModel<Company>> companies = getCompanies();

        assertThat(companies).isNotNull();
        assertThat(companies.getContent()).hasSize(2);
    }

    @Test
    public void shouldGetCompanyById() {
        Optional<EntityModel<Company>> existingCompany = getCompany("Existing company");
        assertThat(existingCompany).isPresent();
    }

    @Test
    public void shouldCreateNewCompany() {
        Company company = new Company();
        company.setName("New company");

        ResponseEntity<Company> postResponse = restTemplate.postForEntity(COMPANIES_ENDPOINT, company, Company.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(postResponse.getBody()).getName()).isEqualTo("New company");
        assertThat(getCompany("New company")).isPresent();
    }

    @Test
    public void shouldUpdateCompany() {
        Optional<EntityModel<Company>> existingCompany = getCompany("Existing company");
        assertThat(existingCompany).isPresent();
        Link selfLink = existingCompany.get().getLink("self")
                .orElseThrow(() -> new AssertionError("Self link not found"));

        Company updatedCompany = new Company();
        updatedCompany.setName("Updated company");
        restTemplate.put(selfLink.getHref(), updatedCompany, Company.class);

        assertThat(getCompany("Existing company")).isEmpty();
        assertThat(getCompany("Updated company")).isPresent();
    }

    @Test
    public void shouldDeleteCompany() {
        Optional<EntityModel<Company>> existingCompany = getCompany("Existing company");
        assertThat(existingCompany).isPresent();
        Link selfLink = existingCompany.get().getLink("self")
                .orElseThrow(() -> new AssertionError("Self link not found"));

        restTemplate.delete(selfLink.getHref());

        ResponseEntity<Company> getResponse = restTemplate.getForEntity(selfLink.getHref(), Company.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Optional<EntityModel<Company>> getCompany(String name) {
        CollectionModel<EntityModel<Company>> companies = getCompanies();
        assertThat(companies).isNotNull();

        return companies.getContent().stream()
                .filter(entityModel -> name.equals(Objects.requireNonNull(entityModel.getContent()).getName()))
                .findFirst();
    }

    private CollectionModel<EntityModel<Company>> getCompanies() {
        ResponseEntity<CollectionModel<EntityModel<Company>>> response = restTemplate.exchange(
                COMPANIES_ENDPOINT,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

}