package br.com.repassa.repository;

import br.com.repassa.entity.SecuritySealLog;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SecuritySealLogRepository implements PanacheRepository<SecuritySealLog> {
}
