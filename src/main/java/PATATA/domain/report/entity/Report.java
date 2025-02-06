package PATATA.domain.report.entity;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.entity.Spot;
import PATATA.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@SuperBuilder
@Entity
@Getter
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private Member reporter;

    private String reason;
    private String description;

    @Enumerated(EnumType.STRING)
    private ReportType type;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
