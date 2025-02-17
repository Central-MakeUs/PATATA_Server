package PATATA.domain.spot.entity;

import PATATA.domain.spot.service.SpotService;
import PATATA.global.BaseEntity;
import PATATA.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false, length = 45)
    private String spotName;

    @Column(length = 300)
    private String spotDescription;

    @Column(nullable = false)
    private String spotAddress;

    private String spotAddressDetail;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Point spotLocation;

    private Integer spotScraps;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category spotCategory;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private boolean deleted;

    public void delete() {
        this.deleted = true;
    }

    @OneToMany(mappedBy = "spot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpotTag> spotTags = new ArrayList<>();

    public void updateTags(List<Tag> newTags) {
        // 기존 태그들 모두 제거
        this.spotTags.clear();

        // 새로운 태그들 추가
        newTags.forEach(tag -> {
            SpotTag spotTag = SpotTag.builder()
                    .spot(this)
                    .tag(tag)
                    .build();
            this.spotTags.add(spotTag);
        });
    }

    public void updateSpot(String spotName, String spotDescription, String spotAddress, String spotAddressDetail, Category category, Point spotLocation) {
        this.spotName = spotName;
        this.spotDescription = spotDescription;
        this.spotAddress = spotAddress;
        this.spotAddressDetail = spotAddressDetail;
        this.spotLocation = spotLocation;
        this.spotCategory = category;
    }

    public void incrementScrapCount() {
        if (this.spotScraps == null) {
            this.spotScraps = 0;
        }
        this.spotScraps++;
    }

    public void decrementScrapCount() {
        if (this.spotScraps != null && this.spotScraps > 0) {
            this.spotScraps--;
        }
    }
}
