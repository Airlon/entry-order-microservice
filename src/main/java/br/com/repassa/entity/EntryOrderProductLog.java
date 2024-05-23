package br.com.repassa.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name ="entry_order_product_log")
public class EntryOrderProductLog extends PanacheEntityBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 189739615841644323L;

    @Id
    @Column(name = "id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_order_code", nullable = false)
    private String entryOrderCode;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_status_update_id", nullable = false)
    private String userStatusUpdateId;

    @Column(name = "user_status_update_email", nullable = false)
    private String userStatusUpdateEmail;

    @Column(name = "entry_order_product_status", nullable = false)
    private String entryOrderProductStatus;

    @Column(name = "date_status_update", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime dateStatusUpdate;
}
