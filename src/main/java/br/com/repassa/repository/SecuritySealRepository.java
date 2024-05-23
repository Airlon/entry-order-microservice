package br.com.repassa.repository;

import br.com.repassa.entity.EntryOrder;
import br.com.repassa.entity.SecuritySeal;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class SecuritySealRepository implements PanacheRepository<SecuritySeal> {

    public Optional<SecuritySeal> findBySecuritySealCodeOptional(String securitySealCode) {
        return find("security_seal_code", securitySealCode).firstResultOptional();
    }

    public Optional<SecuritySeal> findByEntryOrderOptional(EntryOrder entryOrder) {
        return find("entryOrder", entryOrder).firstResultOptional();
    }
}
