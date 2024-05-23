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
@Table(name ="entry_order_log")
public class EntryOrderLog extends PanacheEntityBase implements Serializable {

    @Serial
    private static final long serialVersionUID = -2626309187291780653L;

    @Id
    @Column(name = "id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_order_code", nullable = false)
    @NotNull
    private String entryOrderCode;

    @Column(name = "status", nullable = false)
    @NotNull
    private String status;

    @Column(name = "user_status_update_id")
    private String userStatusUpdateId;

    @Column(name = "user_status_update_email")
    private String userStatusUpdateEmail;

    @Column(name = "date_status_update", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime dateStatusUpdate;
}
