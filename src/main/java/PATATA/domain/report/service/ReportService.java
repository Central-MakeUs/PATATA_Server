package PATATA.domain.report.service;

import PATATA.domain.member.entity.Member;
import PATATA.domain.member.entity.Role;
import PATATA.domain.member.repository.MemberRepository;
import PATATA.domain.report.entity.*;
import PATATA.domain.spot.dto.SpotRequestDto;
import PATATA.domain.spot.dto.SpotResponseDto;
import PATATA.domain.spot.entity.Review;
import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.repository.ReportRepository;
import PATATA.domain.spot.repository.ReviewRepository;
import PATATA.domain.spot.repository.SpotRepository;
import PATATA.global.error.exception.MemberHandler;
import PATATA.global.error.exception.ReportHandler;
import PATATA.global.error.exception.ReviewHandler;
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
    private final ReviewRepository reviewRepository;

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

        //신고된 스팟 즉시 삭제
        spot.delete();
        spotRepository.save(spot);

        // 신고된 스팟 작성자의 Role을 REPORTED로 변경
        Member reportedMember = spot.getMember();
        reportedMember.updateRole(Role.REPORTED);
        memberRepository.save(reportedMember);

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

        // 신고된 사용자의 Role을 REPORTED로 변경
        reportedMember.updateRole(Role.REPORTED);
        memberRepository.save(reportedMember);

        return SpotResponseDto.ReportResponse.of();
    }

    public SpotResponseDto.ReportResponse reportReview(Long reviewId, SpotRequestDto.ReportRequest request, Member reporter) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewHandler(REVIEW_NOT_FOUND));

        // 자신의 리뷰는 신고할 수 없음
        if (review.getMember().getMemberId().equals(reporter.getMemberId())) {
            throw new ReviewHandler(CANNOT_REPORT_OWN_REVIEW);
        }

        // 신고 생성 및 저장
        Report report = ReviewReport.builder()
                .review(review)
                .reporter(reporter)
                .reason(request.getReason())
                .description(request.getDescription())
                .type(ReportType.REVIEW)
                .build();
        reportRepository.save(report);

        //리뷰 즉시 삭제
        review.delete();
        reviewRepository.save(review);

        // 신고된 리뷰 작성자의 Role을 REPORTED로 변경
        Member reportedMember = review.getMember();
        reportedMember.updateRole(Role.REPORTED);
        memberRepository.save(reportedMember);

        return SpotResponseDto.ReportResponse.of();
    }
}
