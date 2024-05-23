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
@Table(name ="security_seal_log")
public class SecuritySealLog extends PanacheEntityBase implements Serializable {

    @Serial
    private static final long serialVersionUID = -6452464436808142603L;

    @Id
    @Column(name = "id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "security_seal_id", referencedColumnName = "id")
    private SecuritySeal securitySeal;

    @ManyToOne
    @JoinColumn(name = "entry_order_id", referencedColumnName = "id")
    private EntryOrder entryOrder;

    @Column(name = "security_seal_code", nullable = false)
    @NotNull
    private String securitySealCode;

    @Column(name = "last_user_id")
    private String lastUserId;

    @Column(name = "last_user_updated_email")
    private String lastUserEmail;

    @Column(name = "last_date_updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime lastDateAt;
}
