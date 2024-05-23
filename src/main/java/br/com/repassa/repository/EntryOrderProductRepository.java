package br.com.repassa.repository;

import br.com.repassa.entity.EntryOrder;
import br.com.repassa.entity.EntryOrderProduct;
import br.com.repassa.enums.EntryOrderProductStatusEnum;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EntryOrderProductRepository implements PanacheRepository<EntryOrderProduct> {

    public Optional<EntryOrderProduct> findByIdProductAndStatus(Long productId, String status) {
        return find("product_id = ?1 and entryOrder.status = ?2", productId, status).stream().findFirst();
    }

    public Optional<EntryOrderProduct> findProductByProductIdAndStatusExcludingNotReceived(Long productId) {
        return find("product_id = ?1 and entry_order_product_status <> ?2",
                productId, EntryOrderProductStatusEnum.NOT_RECEIVED.name()).stream().findFirst();
    }


    public List<EntryOrderProduct> findByEntryOrder(EntryOrder entryOrder) {
        return find("entryOrder = ?1", entryOrder).stream().toList();
    }

    public Optional<EntryOrderProduct> findByProductId(Long productId) {
        return find("product_id", productId).stream().findFirst();
    }

    public Optional<EntryOrderProduct> findByEntryOrderAndProductId(EntryOrder entryOrder, Long productId) {
        return find("entryOrder = ?1 and productId = ?2", entryOrder, productId)
                .singleResultOptional();
    }

    public List<EntryOrderProduct> findByEntryOrderAndStatus(EntryOrder entryOrder,
                                                             EntryOrderProductStatusEnum entryOrderProductStatusEnum) {
        return find("entryOrder = ?1 and entryOrderProductStatus = ?2", entryOrder,
                entryOrderProductStatusEnum.getStatus())
                .stream()
                .toList();
    }
}
