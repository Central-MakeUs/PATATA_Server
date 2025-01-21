package PATATA.domain.spot.converter;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.dto.SpotRequestDto;
import PATATA.domain.spot.entity.Category;
import PATATA.domain.spot.entity.Spot;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
public class SpotConverter {
    public static Spot toEntity(SpotRequestDto.CreateRequest request, Point point, Category category, Member member) {
        return Spot.builder()
                .spotName(request.getSpotName())
                .spotDescription(request.getSpotDescription())
                .spotAddress(request.getSpotAddress())
                .spotAddressDetail(request.getSpotAddressDetail())
                .spotLocation(point)
                .spotScraps(0)
                .spotCategory(category)
                .member(member)
                .build();
    }
}
