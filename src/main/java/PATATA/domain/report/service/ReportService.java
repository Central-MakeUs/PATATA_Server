package PATATA.domain.report.service;

import PATATA.domain.member.entity.Member;
import PATATA.domain.member.repository.MemberRepository;
import PATATA.domain.report.entity.MemberReport;
import PATATA.domain.report.entity.Report;
import PATATA.domain.report.entity.ReportType;
import PATATA.domain.report.entity.SpotReport;
import PATATA.domain.spot.dto.SpotRequestDto;
import PATATA.domain.spot.dto.SpotResponseDto;
import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.repository.ReportRepository;
import PATATA.domain.spot.repository.SpotRepository;
import PATATA.global.error.exception.MemberHandler;
import PATATA.global.error.exception.ReportHandler;
import PATATA.global.error.exception.SpotHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static PATATA.global.error.code.status.ErrorStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportRepository reportRepository;
    private final SpotRepository spotRepository;
    private final MemberRepository memberRepository;

    public SpotResponseDto.ReportResponse reportSpot(Long spotId, SpotRequestDto.ReportRequest request, Member reporter) {

        Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                .orElseThrow(() -> new SpotHandler(SPOT_NOT_FOUND));

        log.info("reporter: {}", reporter.getMemberId() );
        log.info("spot member: {}", spot.getMember().getMemberId());
        // 자신의 스팟은 신고 불가
        if (spot.getMember().getMemberId().equals(reporter.getMemberId())) {
            throw new ReportHandler(CANNOT_REPORT_OWN_SPOT);
        }

        // 신고 생성 및 저장
        Report report = SpotReport.builder()
                .spot(spot)
                .reporter(reporter)
                .reason(request.getReason())
                .description(request.getDescription())
                .type(ReportType.SPOT)
                .build();

        reportRepository.save(report);

        // 신고 횟수가 일정 기준을 넘으면 스팟 비활성화 처리
//        long reportCount = reportRepository.countBySpot(spot);
//        if (reportCount >= 5) {  // 예시로 5회로 설정
//            spot.deactivate();
//            spotRepository.save(spot);
//        }

        return SpotResponseDto.ReportResponse.of();
    }

    public SpotResponseDto.ReportResponse reportMember(Long reportedMemberId, SpotRequestDto.ReportRequest request, Member reporter) {

        Member reportedMember = memberRepository.findById(reportedMemberId)
                .orElseThrow(() -> new MemberHandler(MEMBER_NOT_FOUND));

        // 신고 생성 및 저장
        Report report = MemberReport.builder()
                .reportedMember(reportedMember)
                .reporter(reporter)
                .reason(request.getReason())
                .description(request.getDescription())
                .type(ReportType.MEMBER)
                .build();

        reportRepository.save(report);

        return SpotResponseDto.ReportResponse.of();
    }
}
