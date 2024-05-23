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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name ="entry_order_product")
public class EntryOrderProduct extends PanacheEntityBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 500934974268013001L;

    @Id
    @Column(name = "id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    @NotNull
    private Long productId;

    @Column(name = "title", nullable = false)
    @NotNull
    private String title;

    @Column(name = "product_origin", nullable = false)
    @NotNull
    private String productOrigin;

    @Column(name = "sent_renova", nullable = false)
    @NotNull
    private boolean sentRenova = false;

    @Column(name = "registered_user_id", nullable = false)
    @NotNull
    private String registeredUserId;

    @Column(name = "entry_order_product_status", nullable = false)
    private String entryOrderProductStatus;

    @Column(name = "registered_user_name", nullable = false)
    @NotNull
    private String registeredUserName;

    @Column(name = "registered_user_email", nullable = false)
    @NotNull
    private String registeredUserEmail;

    @Column(name = "registration_date", nullable = false)
    @NotNull
    private LocalDateTime registrationDate;

    @ManyToOne
    @JoinColumn(name = "entry_order_id", referencedColumnName = "id")
    private EntryOrder entryOrder;
}
