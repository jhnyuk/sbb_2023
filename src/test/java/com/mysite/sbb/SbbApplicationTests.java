package com.mysite.sbb;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerRepository;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionRepository;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserRepository;
import com.mysite.sbb.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SbbApplicationTests {
	@Autowired
	private QuestionService questionService;

	@Autowired
	private UserService userService;

	@Autowired
	private AnswerService answerService;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private AnswerRepository answerRepository;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
		// 아래 메서드는 각 테스트케이스가 실행되기 전에 실행된다.
	void beforeEach() {
		// 모든 데이터 삭제
		answerRepository.deleteAll();
		answerRepository.clearAutoIncrement();

		// 모든 데이터 삭제
		questionRepository.deleteAll();

		// 흔적삭제(다음번 INSERT 때 id가 1번으로 설정되도록)
		questionRepository.clearAutoIncrement();

		// 모든 데이터 삭제
		userRepository.deleteAll();
		userRepository.clearAutoIncrement();

		// 회원 2명 생성
		SiteUser user1 = userService.create("user1", "user1@test.com", "1234");
		SiteUser user2 = userService.create("user2", "user2@test.com", "1234");

		// 질문 1개 생성
		Question q1 = questionService.create("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.", user1);


		// 질문 1개 생성
		Question q2 = questionService.create("스프링부트 모델 질문입니다.", "id는 자동으로 생성되나요?", user1);
		Answer a1 = answerService.create(q2, "네 자동으로 생성됩니다.", user2);

		q1.addVoter(user1);
		q1.addVoter(user2);
		questionRepository.save(q1);

		q2.addVoter(user1);
		q2.addVoter(user2);
		questionRepository.save(q2);

		a1.addVoter(user1);
		a1.addVoter(user2);
		answerRepository.save(a1);
	}

	@Test
	@DisplayName("데이터 저장")
	void t001() {
		SiteUser user1 = userService.getUser("user1");

		Question q = questionService.create("세계에서 가장 부유한 국가가 어디인가요?", "알고 싶습니다.", user1);

		assertEquals("세계에서 가장 부유한 국가가 어디인가요?", questionRepository.findById(3).get().getSubject());
	}

	/*
	SQL
	SELECT * FROM question
	 */
	@Test
	@DisplayName("findAll")
	void t002() {
		List<Question> all = questionRepository.findAll();
		assertEquals(2, all.size());

		Question q = all.get(0);
		assertEquals("sbb가 무엇인가요?", q.getSubject());
	}

	/*
    SQL
    SELECT *
    FROM question
    WHERE id = 1
    */
	@Test
	@DisplayName("findById")
	void t003() {
		Optional<Question> oq = questionRepository.findById(1);

		if (oq.isPresent()) {
			Question q = oq.get();
			assertEquals("sbb가 무엇인가요?", q.getSubject());
		}
	}

	/*
    SQL
    SELECT *
    FROM question
    WHERE subject = 'sbb가 무엇인가요?'
    */
	@Test
	@DisplayName("findBySubject")
	void t004() {
		Question q = questionRepository.findBySubject("sbb가 무엇인가요?");
		assertEquals(1, q.getId());
	}

	/*
    SQL
    SELECT *
    FROM question
    WHERE subject = 'sbb가 무엇인가요?'
    AND content = 'sbb에 대해서 알고 싶습니다.'
    */
	@Test
	@DisplayName("findBySubjectAndContent")
	void t005() {
		Question q = questionRepository.findBySubjectAndContent(
				"sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다."
		);
		assertEquals(1, q.getId());
	}

	/*
	SQL
	SELECT *
	FROM question
	WHERE subject LIKE 'sbb%'
	 */
	@Test
	@DisplayName("findBySubjectAndContent")
	void t006() {
		List<Question> qList = questionRepository.findBySubjectLike("sbb%");
		Question q = qList.get(0);
		assertEquals("sbb가 무엇인가요?", q.getSubject());
	}

	/*
    SQL
    UPDATE
        question
    SET
        content = ?,
        create_date = ?,
        subject = ?
    WHERE
        id = ?
    */
	@Test
	@DisplayName("데이터 수정")
	void t007() {
		Optional<Question> oq = questionRepository.findById(1);
		assertTrue(oq.isPresent());
		Question q = oq.get();
		q.setSubject("수정된 제목");
		questionRepository.save(q);
	}

	/*
        SQL
        DELETE
        FROM
            question
        WHERE
            id = ?
        */
	@Test
	@DisplayName("데이터 삭제")
	void t008() {
		assertEquals(2, questionRepository.count());
		Optional<Question> oq = questionRepository.findById(1);
		assertTrue(oq.isPresent());
		Question q = oq.get();
		questionRepository.delete(q);
		assertEquals(1, questionRepository.count());
	}

	@Transactional
	@Rollback(false)
	@Test
	@DisplayName("답변 데이터 생성 후 저장하기")
	void t009() {
		Optional<Question> oq = questionRepository.findById(2);
		assertTrue(oq.isPresent());
		Question q = oq.get();
		SiteUser user2 = userService.getUser("user2");

		Answer a = answerService.create(q, "네 자동으로 생성됩니다.", user2);
	}

	@Test
	@DisplayName("답변 조회하기")
	void t010() {
		Optional<Answer> oa = answerRepository.findById(1);
		assertTrue(oa.isPresent());
		Answer a = oa.get();
		assertEquals(2, a.getQuestion().getId());
	}

	@Transactional
	@Rollback(value = false)
	@Test
	@DisplayName("질문에 달린 답변 찾기")
	void t011() {
		Optional<Question> oq = questionRepository.findById(2);
		assertTrue(oq.isPresent());
		Question q = oq.get();

		List<Answer> answerList = q.getAnswerList();

		assertEquals(1, answerList.size());
		assertEquals("네 자동으로 생성됩니다.", answerList.get(0).getContent());
	}

	@Test
	@DisplayName("대량 테스트 데이터 만들기")
	void t012() {
		SiteUser user2 = userService.getUser("user2");

		IntStream.rangeClosed(3, 300).forEach(no -> questionService.create("테스트 제목입니다. %d".formatted(no), "테스트 내용입니다. %d".formatted(no), user2));
	}

	@Test@DisplayName("검색, 질문내용으로 검색할 수 있다.")
	void t013() {
		Page<Question> searchResult = questionService.getList(0, "sbb에 대해서 알고 싶습니다.");

		assertEquals(1, searchResult.getTotalElements());
	}

	@Test
	@DisplayName("검색, 질문자이름으로 검색할 수 있다.")
	void t014() {
		Page<Question> searchResult = questionService.getList(0, "user1");

		assertEquals(2, searchResult.getTotalElements());
	}

	@Test
	@DisplayName("검색, 답변내용으로 검색할 수 있다.")
	void t015() {
		Page<Question> searchResult = questionService.getList(0, "네 자동으로 생성됩니다.");

		assertEquals(2, searchResult.getContent().get(0).getId());
		assertEquals(1, searchResult.getTotalElements());
	}

	@Test
	@DisplayName("검색, 답변자이름으로 검색할 수 있다.")
	void t016() {
		Page<Question> searchResult = questionService.getList(0, "user2");

		assertEquals(2, searchResult.getContent().get(0).getId());
		assertEquals(1, searchResult.getTotalElements());
	}
}
