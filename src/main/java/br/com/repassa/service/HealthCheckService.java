package br.com.repassa.service;

import br.com.repassa.repository.EntryOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

@ApplicationScoped
@Slf4j
public class HealthCheckService implements HealthCheck {

    @Inject
    EntryOrderRepository entryOrderRepository;

    @Override
    public HealthCheckResponse call() {
        try {
            checkDatabaseConnection();
            log.info("O servico esta ativo");
            return HealthCheckResponse.named("O serviço está online").up().build();
        } catch (PersistenceException e) {
            log.info("O servico esta fora do ar");
            return HealthCheckResponse.named("O serviço está offline").down().build();
        }
    }

    private void checkDatabaseConnection() {
        log.info("Verificando se o banco esta ativo");
        entryOrderRepository.findAll().firstResult();
        log.info("O banco esta ativo");
    }
}
