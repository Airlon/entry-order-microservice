package br.com.repassa.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name ="entry_order")
public class EntryOrder extends PanacheEntityBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 3749957222970920904L;

    @Id
    @Column(name = "id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false)
    @NotNull
    private String code;

    @Column(name = "type", nullable = false)
    @NotNull
    private String type;

    @Column(name = "classification", nullable = false)
    @NotNull
    private String classification;

    @Column(name = "status", nullable = false)
    @NotNull
    private String status;

    @Column(name = "user_created_id")
    private String userCreatedId;

    @Column(name = "user_created_email")
    private String userCreatedEmail;

    @Column(name = "date_created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime dateCreatedAt;

    @Column(name = "user_updated_id")
    private String userUpdatedId;

    @Column(name = "user_updated_email")
    private String userUpdatedEmail;

    @Column(name = "date_updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime dateUpdatedAt;

    @OneToMany(mappedBy = "entryOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntryOrderProduct> entryOrderProductList;
}
