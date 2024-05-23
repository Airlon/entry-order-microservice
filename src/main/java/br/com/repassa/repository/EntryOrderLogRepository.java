package br.com.repassa.repository;

import br.com.repassa.entity.EntryOrderLog;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EntryOrderLogRepository implements PanacheRepository<EntryOrderLog> {
}
