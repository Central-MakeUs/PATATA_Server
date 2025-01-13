package PATATA.spot.entity;

import PATATA.common.BaseEntity;
import PATATA.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.awt.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Spot extends BaseEntity {

    @Id
    @Column(name = "spot_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long spotId;

    @Column(nullable = false, length = 30)
    private String spotName;

    @Column(length = 200)
    private String spotDescription;

    @Column(nullable = false)
    private String spotAddress;

    private String spotAddressDetail;

    @Column(nullable = false)
    private Point spotLocation;

    private Integer spotScraps;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category spotCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}
