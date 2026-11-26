package com.psm.yagna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.psm.yagna.domain.BookingStatus;
import com.psm.yagna.dto.GroupNameDto;
import com.psm.yagna.dto.GroupProgressRequest;
import com.psm.yagna.dto.GroupVanchanDto;
import com.psm.yagna.dto.GroupVanchanRequest;
import com.psm.yagna.dto.HaribhaktDto;
import com.psm.yagna.dto.HaribhaktLoginRequest;
import com.psm.yagna.dto.ShravanBookingDto;
import com.psm.yagna.dto.StatusReportDto;
import com.psm.yagna.dto.VanchanBookDto;
import com.psm.yagna.dto.VanchanBookingDto;
import com.psm.yagna.exception.ConflictException;
import com.psm.yagna.exception.ForbiddenException;
import com.psm.yagna.service.GroupVanchanService;
import com.psm.yagna.service.HaribhaktService;
import com.psm.yagna.service.ShravanService;
import com.psm.yagna.service.StatusReportService;
import com.psm.yagna.service.VanchanService;
import java.time.LocalDate;
import java.util.ArrayList;
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

    @Autowired HaribhaktService haribhaktService;
    @Autowired VanchanService vanchanService;
    @Autowired ShravanService shravanService;
    @Autowired GroupVanchanService groupVanchanService;
    @Autowired StatusReportService statusReportService;

    private HaribhaktDto haribhakt(String name) {
        return haribhaktService.signIn(new HaribhaktLoginRequest(name, "NIBM"));
    }

    @Test
    void signInReusesHaribhaktForSameNameAndMandalIgnoringCaseAndSpaces() {
        HaribhaktDto first = haribhakt("Ram Bhai");
        HaribhaktDto again = haribhakt("  ram bhai ");
        assertThat(again.id()).isEqualTo(first.id());
        assertThat(again.mandal()).isEqualTo("NIBM");

        HaribhaktDto otherMandal = haribhaktService.signIn(new HaribhaktLoginRequest("Ram Bhai", "M-NIBM"));
        assertThat(otherMandal.id()).isNotEqualTo(first.id());
    }

    @Test
    void signInRejectsMandalNotInList() {
        assertThatThrownBy(() -> haribhaktService.signIn(new HaribhaktLoginRequest("Ram", "Unknown")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void vanchanHandsOutConsecutivePageRangesWithinEachBookAcrossHaribhaktsAndDates() {
        HaribhaktDto a = haribhakt("A");
        HaribhaktDto b = haribhakt("B");

        VanchanBookingDto first = vanchanService.book(a.id());
        VanchanBookingDto second = vanchanService.book(b.id());
        vanchanService.complete(first.id(), a.id());
        VanchanBookingDto third = vanchanService.book(a.id());

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

        vanchanService.complete(second.id(), b.id());
        assertThatThrownBy(() -> vanchanService.book(b.id())).isInstanceOf(ConflictException.class);
    }

    @Test
    void vanchanNumbersAreSequentialAcrossAllHaribhakts() {
        HaribhaktDto a = haribhakt("A");
        HaribhaktDto b = haribhakt("B");
        VanchanBookingDto first = vanchanService.book(a.id());
        VanchanBookingDto second = vanchanService.book(b.id());
        vanchanService.complete(first.id(), a.id());
        VanchanBookingDto third = vanchanService.book(a.id());

        assertThat(List.of(first, second, third)).extracting(VanchanBookingDto::pushpNumber).containsExactly(1L, 2L, 3L);
        assertThat(vanchanService.list(b.id())).extracting(VanchanBookingDto::pushpNumber).containsExactly(2L);
        assertThat(vanchanService.list(a.id())).extracting(VanchanBookingDto::pushpNumber).containsExactly(3L, 1L);
    }

    @Test
    void vanchanNewSlotRequiresPreviousSlotToBeCompleted() {
        HaribhaktDto a = haribhakt("A");
        VanchanBookingDto first = vanchanService.book(a.id());

        assertThat(first.date()).isEqualTo(LocalDate.now());
        assertThatThrownBy(() -> vanchanService.book(a.id())).isInstanceOf(ConflictException.class);

        vanchanService.complete(first.id(), a.id());
        VanchanBookingDto second = vanchanService.book(a.id());
        assertThat(second.startPage()).isEqualTo(first.endPage() + 1);
        assertThat(vanchanService.list(a.id())).extracting(VanchanBookingDto::id).containsExactly(second.id(), first.id());
    }

    @Test
    void vanchanCanBeMarkedCompletedOnlyByOwner() {
        HaribhaktDto a = haribhakt("A");
        HaribhaktDto b = haribhakt("B");
        VanchanBookingDto booking = vanchanService.book(a.id());

        assertThatThrownBy(() -> vanchanService.complete(booking.id(), b.id())).isInstanceOf(RuntimeException.class);
        assertThat(vanchanService.complete(booking.id(), a.id()).status()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(vanchanService.list(a.id())).extracting(VanchanBookingDto::status).containsExactly(BookingStatus.COMPLETED);
    }

    @Test
    void shravanHandsOutOneUniqueTrackInSequence() {
        HaribhaktDto a = haribhakt("A");
        HaribhaktDto b = haribhakt("B");

        ShravanBookingDto first = shravanService.book(a.id());
        ShravanBookingDto second = shravanService.book(b.id());
        shravanService.complete(first.id(), a.id());
        ShravanBookingDto third = shravanService.book(a.id());

        assertThat(List.of(first, second, third)).extracting(x -> x.track().sequenceNo()).containsExactly(1, 2, 3);
        assertThat(shravanService.summary().remainingTracks()).isZero();
        shravanService.complete(second.id(), b.id());
        assertThatThrownBy(() -> shravanService.book(b.id())).isInstanceOf(ConflictException.class);
    }

    @Test
    void shravanNewSlotRequiresPreviousSlotToBeCompleted() {
        HaribhaktDto a = haribhakt("A");
        ShravanBookingDto first = shravanService.book(a.id());

        assertThat(first.date()).isEqualTo(LocalDate.now());
        assertThatThrownBy(() -> shravanService.book(a.id())).isInstanceOf(ConflictException.class);

        shravanService.complete(first.id(), a.id());
        ShravanBookingDto second = shravanService.book(a.id());
        assertThat(List.of(first, second)).extracting(x -> x.track().sequenceNo()).containsExactly(1, 2);
        assertThat(shravanService.list(a.id())).hasSize(2);
    }

    private GroupVanchanRequest groupRequest(Long groupNameId, String... names) {
        List<VanchanBookDto> books = groupVanchanService.books();
        List<GroupVanchanRequest.Assignment> assignments = new ArrayList<>();
        for (int i = 0; i < books.size(); i++) {
            assignments.add(new GroupVanchanRequest.Assignment(books.get(i).id(), names[i]));
        }
        return new GroupVanchanRequest(groupNameId, null, assignments);
    }

    @Test
    void groupVanchanAssignsOneHaribhaktToEveryBook() {
        GroupNameDto groupName = groupVanchanService.groupNames().get(0);

        GroupVanchanDto created = groupVanchanService.create(groupRequest(groupName.id(), " Ram ", "Shyam"));

        assertThat(created.groupName()).isEqualTo(groupName.name());
        assertThat(created.members()).extracting(GroupVanchanDto.MemberDto::name).containsExactly("Ram", "Shyam");
        assertThat(created.members()).extracting(GroupVanchanDto.MemberDto::bookTitle).containsExactly("Test Book A", "Test Book B");
        assertThat(created.status()).isEqualTo(BookingStatus.BOOKED);
        assertThat(groupVanchanService.list()).hasSize(1);

        List<VanchanBookDto> books = groupVanchanService.books();
        GroupNameDto other = groupVanchanService.groupNames().get(0);
        assertThatThrownBy(() -> groupVanchanService.create(new GroupVanchanRequest(other.id(), null,
                List.of(new GroupVanchanRequest.Assignment(books.get(0).id(), "Ram")))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> groupVanchanService.create(new GroupVanchanRequest(other.id(), null,
                List.of(new GroupVanchanRequest.Assignment(books.get(0).id(), "Ram"),
                        new GroupVanchanRequest.Assignment(books.get(0).id(), "Shyam")))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void statusReportIsAdminOnlyAndGroupsActivityByMandal() {
        HaribhaktDto ram = haribhakt("Ram");
        HaribhaktDto admin = haribhakt("Admin");
        VanchanBookingDto vanchan = vanchanService.book(ram.id());
        vanchanService.complete(vanchan.id(), ram.id());
        shravanService.book(ram.id());
        GroupNameDto groupName = groupVanchanService.groupNames().get(0);
        groupVanchanService.create(groupRequest(groupName.id(), "Ram", "Stranger"));

        assertThatThrownBy(() -> statusReportService.report(ram.id())).isInstanceOf(ForbiddenException.class);

        StatusReportDto report = statusReportService.report(admin.id());
        assertThat(report.mandals()).hasSize(1);
        assertThat(report.mandals().get(0).mandal()).isEqualTo("NIBM");
        assertThat(report.mandals().get(0).haribhakts()).extracting(h -> h.name()).containsExactly("Ram");
        assertThat(report.summary().vanchan()).isEqualTo(new StatusReportDto.Counts(1, 0, 1));
        assertThat(report.summary().shravan()).isEqualTo(new StatusReportDto.Counts(1, 1, 0));
        assertThat(report.summary().groupVanchan()).isEqualTo(new StatusReportDto.Counts(1, 1, 0));
        assertThat(report.groups().get(0).members()).extracting(m -> m.mandal()).containsExactly("NIBM", null);
        assertThat(report.groups().get(0).members()).extracting(m -> m.bookTitle()).containsExactly("Test Book A", "Test Book B");
    }

    @Test
    void groupProgressIsStoredPerMemberAndShownAsPercentageOfAllPages() {
        GroupNameDto groupName = groupVanchanService.groupNames().get(0);
        GroupVanchanDto created = groupVanchanService.create(groupRequest(groupName.id(), "Ram", "Shyam"));
        assertThat(created.totalPages()).isEqualTo(30);
        assertThat(created.pagesRead()).isZero();

        GroupVanchanDto updated = groupVanchanService.updateProgress(created.id(), new GroupProgressRequest(List.of(6, 9)));

        assertThat(updated.members()).extracting(GroupVanchanDto.MemberDto::pagesRead).containsExactly(6, 9);
        assertThat(updated.pagesRead()).isEqualTo(15);
        assertThat(updated.percentComplete()).isEqualTo(50.0);
        assertThat(updated.status()).isEqualTo(BookingStatus.BOOKED);
        assertThat(groupVanchanService.list().get(0).percentComplete()).isEqualTo(50.0);

        // Book A has 20 pages and Book B has 10: no haribhakt can have read more than the book holds
        assertThatThrownBy(() -> groupVanchanService.updateProgress(created.id(), new GroupProgressRequest(List.of(21, 0))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> groupVanchanService.updateProgress(created.id(), new GroupProgressRequest(List.of(5, 11))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> groupVanchanService.updateProgress(created.id(), new GroupProgressRequest(List.of(1))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void groupCompletesAutomaticallyWhenAllPagesAreReadAndReopensIfLowered() {
        GroupNameDto groupName = groupVanchanService.groupNames().get(0);
        GroupVanchanDto created = groupVanchanService.create(groupRequest(groupName.id(), "Ram", "Shyam"));

        GroupVanchanDto done = groupVanchanService.updateProgress(created.id(), new GroupProgressRequest(List.of(20, 10)));
        assertThat(done.pagesRead()).isEqualTo(30);
        assertThat(done.percentComplete()).isEqualTo(100.0);
        assertThat(done.status()).isEqualTo(BookingStatus.COMPLETED);

        GroupVanchanDto reopened = groupVanchanService.updateProgress(created.id(), new GroupProgressRequest(List.of(20, 9)));
        assertThat(reopened.status()).isEqualTo(BookingStatus.BOOKED);
    }

    @Test
    void usedGroupNameIsNoLongerAvailableAndCannotBeUsedAgain() {
        GroupNameDto groupName = groupVanchanService.groupNames().get(0);
        int before = groupVanchanService.groupNames().size();

        groupVanchanService.create(groupRequest(groupName.id(), "Ram", "Ram"));

        assertThat(groupVanchanService.groupNames()).extracting(GroupNameDto::id).doesNotContain(groupName.id());
        assertThat(groupVanchanService.groupNames()).hasSize(before - 1);
        assertThatThrownBy(() -> groupVanchanService.create(groupRequest(groupName.id(), "Shyam", "Shyam")))
                .isInstanceOf(ConflictException.class);
    }
}
