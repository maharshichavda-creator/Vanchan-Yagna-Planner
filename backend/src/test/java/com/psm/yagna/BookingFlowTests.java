package com.psm.yagna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.psm.yagna.domain.BookingStatus;
import com.psm.yagna.dto.GroupNameDto;
import com.psm.yagna.dto.GroupVanchanDto;
import com.psm.yagna.dto.GroupVanchanRequest;
import com.psm.yagna.dto.HaribhaktDto;
import com.psm.yagna.dto.HaribhaktLoginRequest;
import com.psm.yagna.dto.ShravanBookingDto;
import com.psm.yagna.dto.StatusReportDto;
import com.psm.yagna.dto.VanchanBookingDto;
import com.psm.yagna.exception.ConflictException;
import com.psm.yagna.exception.ForbiddenException;
import com.psm.yagna.service.GroupVanchanService;
import com.psm.yagna.service.HaribhaktService;
import com.psm.yagna.service.ShravanService;
import com.psm.yagna.service.StatusReportService;
import com.psm.yagna.service.VanchanService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:yagna;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.vanchan.books-file=classpath:seed/vanchan-books-test.txt",
        "app.shravan.tracks-file=classpath:seed/shravan-tracks-test.txt",
        "app.group-vanchan.names=Group A,Group B"
})
@Transactional
class BookingFlowTests {

    private static final LocalDate DAY = LocalDate.now().plusDays(1);

    @Autowired HaribhaktService haribhaktService;
    @Autowired VanchanService vanchanService;
    @Autowired ShravanService shravanService;
    @Autowired GroupVanchanService groupVanchanService;
    @Autowired StatusReportService statusReportService;

    private HaribhaktDto haribhakt(String name, String bapsId) {
        return haribhaktService.signIn(new HaribhaktLoginRequest(name, "NIBM", bapsId));
    }

    @Test
    void signInReusesHaribhaktForSameBapsIdIgnoringCaseAndSpaces() {
        HaribhaktDto first = haribhakt("Ram", "ab12345");
        HaribhaktDto again = haribhakt("Ram Bhai", " AB12345 ");
        assertThat(again.id()).isEqualTo(first.id());
        assertThat(again.bapsId()).isEqualTo("AB12345");
        assertThat(again.mandal()).isEqualTo("NIBM");
    }

    @Test
    void signInRejectsMandalNotInList() {
        assertThatThrownBy(() -> haribhaktService.signIn(new HaribhaktLoginRequest("Ram", "Unknown", "AB12345")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void signInRejectsInvalidBapsId() {
        assertThatThrownBy(() -> haribhakt("Ram", "ab")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> haribhakt("Ram", "AB 123")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void vanchanHandsOutConsecutivePageRangesWithinEachBookAcrossHaribhaktsAndDates() {
        HaribhaktDto a = haribhakt("A", "9000000001");
        HaribhaktDto b = haribhakt("B", "9000000002");

        VanchanBookingDto first = vanchanService.book(a.id(), DAY);
        VanchanBookingDto second = vanchanService.book(b.id(), DAY);
        VanchanBookingDto third = vanchanService.book(a.id(), DAY.plusDays(1));

        // Test Book A has 20 pages: the second booking gets only the 5 pages left in that book
        assertThat(first.bookTitle()).isEqualTo("Test Book A");
        assertThat(first.startPage()).isEqualTo(1);
        assertThat(first.endPage()).isEqualTo(15);
        assertThat(second.bookTitle()).isEqualTo("Test Book A");
        assertThat(second.startPage()).isEqualTo(16);
        assertThat(second.endPage()).isEqualTo(20);
        // then reading continues from page 1 of the next book (10 pages)
        assertThat(third.bookTitle()).isEqualTo("Test Book B");
        assertThat(third.startPage()).isEqualTo(1);
        assertThat(third.endPage()).isEqualTo(10);
        assertThat(vanchanService.summary().remainingPages()).isZero();
        assertThat(vanchanService.summary().nextBookTitle()).isNull();

        assertThatThrownBy(() -> vanchanService.book(b.id(), DAY.plusDays(1))).isInstanceOf(ConflictException.class);
    }

    @Test
    void vanchanAllowsOnlyOneSlotPerHaribhaktPerDateAndRejectsPastDates() {
        HaribhaktDto a = haribhakt("A", "9000000003");
        vanchanService.book(a.id(), DAY);

        assertThatThrownBy(() -> vanchanService.book(a.id(), DAY)).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> vanchanService.book(a.id(), LocalDate.now().minusDays(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void vanchanCanBeMarkedCompletedOnlyByOwner() {
        HaribhaktDto a = haribhakt("A", "9000000004");
        HaribhaktDto b = haribhakt("B", "9000000005");
        VanchanBookingDto booking = vanchanService.book(a.id(), DAY);

        assertThatThrownBy(() -> vanchanService.complete(booking.id(), b.id())).isInstanceOf(RuntimeException.class);
        assertThat(vanchanService.complete(booking.id(), a.id()).status()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(vanchanService.list(a.id())).extracting(VanchanBookingDto::status).containsExactly(BookingStatus.COMPLETED);
    }

    @Test
    void shravanHandsOutOneUniqueTrackInSequence() {
        HaribhaktDto a = haribhakt("A", "9000000006");
        HaribhaktDto b = haribhakt("B", "9000000007");

        ShravanBookingDto first = shravanService.book(a.id(), DAY);
        ShravanBookingDto second = shravanService.book(b.id(), DAY);
        ShravanBookingDto third = shravanService.book(a.id(), DAY.plusDays(1));

        assertThat(List.of(first, second, third)).extracting(x -> x.track().sequenceNo()).containsExactly(1, 2, 3);
        assertThat(shravanService.summary().remainingTracks()).isZero();
        assertThatThrownBy(() -> shravanService.book(b.id(), DAY.plusDays(1))).isInstanceOf(ConflictException.class);
    }

    @Test
    void shravanAllowsOnlyOneSlotPerHaribhaktPerDate() {
        HaribhaktDto a = haribhakt("A", "9000000008");
        shravanService.book(a.id(), DAY);
        assertThatThrownBy(() -> shravanService.book(a.id(), DAY)).isInstanceOf(ConflictException.class);
    }

    @Test
    void groupVanchanIsCreatedFromListedGroupNameAndCompleted() {
        GroupNameDto groupName = groupVanchanService.groupNames().get(0);

        GroupVanchanDto created = groupVanchanService.create(
                new GroupVanchanRequest(groupName.id(), null, List.of(" Ram ", "Shyam", "ram", "")));

        assertThat(created.groupName()).isEqualTo(groupName.name());
        assertThat(created.members()).containsExactly("Ram", "Shyam", "ram");
        assertThat(created.status()).isEqualTo(BookingStatus.BOOKED);
        assertThat(groupVanchanService.complete(created.id()).status()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(groupVanchanService.list()).hasSize(1);
    }

    @Test
    void statusReportIsAdminOnlyAndGroupsActivityByMandal() {
        HaribhaktDto ram = haribhakt("Ram", "RP00001");
        HaribhaktDto admin = haribhakt("Admin", "ADM0001");
        VanchanBookingDto vanchan = vanchanService.book(ram.id(), DAY);
        vanchanService.complete(vanchan.id(), ram.id());
        shravanService.book(ram.id(), DAY);
        GroupNameDto groupName = groupVanchanService.groupNames().get(0);
        groupVanchanService.create(new GroupVanchanRequest(groupName.id(), null, List.of("Ram", "Stranger")));

        assertThatThrownBy(() -> statusReportService.report(ram.id())).isInstanceOf(ForbiddenException.class);

        StatusReportDto report = statusReportService.report(admin.id());
        assertThat(report.mandals()).hasSize(1);
        assertThat(report.mandals().get(0).mandal()).isEqualTo("NIBM");
        assertThat(report.mandals().get(0).haribhakts()).extracting(h -> h.name()).containsExactly("Ram");
        assertThat(report.summary().vanchan()).isEqualTo(new StatusReportDto.Counts(1, 0, 1));
        assertThat(report.summary().shravan()).isEqualTo(new StatusReportDto.Counts(1, 1, 0));
        assertThat(report.summary().groupVanchan()).isEqualTo(new StatusReportDto.Counts(1, 1, 0));
        assertThat(report.groups().get(0).members()).extracting(m -> m.mandal()).containsExactly("NIBM", null);
    }

    @Test
    void usedGroupNameIsNoLongerAvailableAndCannotBeUsedAgain() {
        GroupNameDto groupName = groupVanchanService.groupNames().get(0);
        int before = groupVanchanService.groupNames().size();

        groupVanchanService.create(new GroupVanchanRequest(groupName.id(), null, List.of("Ram")));

        assertThat(groupVanchanService.groupNames()).extracting(GroupNameDto::id).doesNotContain(groupName.id());
        assertThat(groupVanchanService.groupNames()).hasSize(before - 1);
        assertThatThrownBy(() -> groupVanchanService.create(new GroupVanchanRequest(groupName.id(), null, List.of("Shyam"))))
                .isInstanceOf(ConflictException.class);
    }
}
