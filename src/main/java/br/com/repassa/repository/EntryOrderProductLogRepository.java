package br.com.repassa.repository;

import br.com.repassa.entity.EntryOrderProductLog;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EntryOrderProductLogRepository implements PanacheRepository<EntryOrderProductLog> {
}
