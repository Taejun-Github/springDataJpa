package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    public void testNamedQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertEquals(findMember.getId(), m1.getId());
    }

    @Test
    public void testQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        Member findMember = result.get(0);
        assertEquals(findMember.getId(), m1.getId());
    }

    @Test
    public void findUsernameList() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> result = memberRepository.findUsernameList();
        for (String s : result) {
            System.out.println(s);
        }
    }

    @Test
    public void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();

        for (MemberDto dto : memberDto) {
            System.out.println(dto);
        }
    }

    @Test
    public void findMember() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<Member> memberDto = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));

        for (Member member : memberDto) {
            System.out.println(member);
        }
    }

    @Test
    public void returnType() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> aaa = memberRepository.findListByUsername("AAA");
        System.out.println(aaa);

        Member bbb = memberRepository.findMemberByUsername("BBB");
        System.out.println(bbb);

        Optional<Member> bbb1 = memberRepository.findOptionalByUsername("BBB");
        System.out.println(bbb1.get());

    }

    @Test
    public void paging() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> page = memberRepository.findByAge(age, pageRequest);
//        Slice<Member> slicePage = memberRepository.findByAgeSlice(age, pageRequest);

        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));
        // DTO로 반환하려면 이렇게 하면 된다.
        System.out.println(toMap);

        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        System.out.println(content);
        System.out.println(totalElements);

        assertEquals(content.size(), 3);
        assertEquals(page.getTotalElements(), 6);
        assertEquals(page.getNumber(), 0);
        assertEquals(page.getTotalPages(), 2);
        assertTrue(page.isFirst());
        assertTrue(page.hasNext());
    }

    @Transactional
    @Test
    public void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 20));
        memberRepository.save(new Member("member3", 30));
        memberRepository.save(new Member("member4", 40));
        memberRepository.save(new Member("member5", 50));
        memberRepository.save(new Member("member6", 60));

        int resultCount = memberRepository.bulkAgePlus(20);
        em.flush();
        em.clear();

        // 벌크성 수정 쿼리의 경우 영속성 컨텍스트를 거치지 않고 DB에 바로 반영되기 때문에 DB와 영속성 컨텍스트의 값이 달라지는 경우가 있다.
        // 그러므로 em.flush()와 em.clear()로 영속성 컨텍스트와 DB의 값을 일치시켜야 한다.
        // 따라서 벌크성 수정 연산자 이후에는 영속성 컨텍스트를 반드시 초기화해야 한다.

        List<Member> result = memberRepository.findByUsername("member4");
        Member member4 = result.get(0);
        System.out.println(member4);

        assertEquals(resultCount, 5);
    }

    @Transactional
    @Test
    public void findMemberLazy() {
        Team teamA = new Team("team A");
        Team teamB = new Team("team B");

        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            System.out.println(member.getUsername());
            System.out.println(member.getTeam().getName());
        }


    }
}