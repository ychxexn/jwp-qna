package qna.domain;

import qna.CannotDeleteException;
import qna.NotFoundException;
import qna.UnAuthorizedException;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Answer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User writeBy;
    @ManyToOne
    private Question question;
    @Lob
    private String contents;
    @Column(nullable = false)
    private boolean deleted = false;

    protected Answer() {

    }

    public Answer(User writeBy, Question question, String contents) {
        this(null, writeBy, question, contents);
    }

    public Answer(Long id, User writeBy, Question question, String contents) {
        this.id = id;

        if (Objects.isNull(writeBy)) {
            throw new UnAuthorizedException();
        }

        if (Objects.isNull(question)) {
            throw new NotFoundException();
        }

        this.writeBy = writeBy;
        toQuestion(question);
        this.contents = contents;
    }

    public boolean isNotOwner(User writer) {
        return !this.writeBy.equals(writer);
    }

    public void toQuestion(Question question) {
        if (Objects.equals(this.question, question)) {
            return;
        }

        if (this.question != null) {
            this.question.removeAnswer(this);
        }

        if (question != null) {
            question.addAnswer(this);
        }

        this.question = question;
    }

    public void updateContents(String contents) {
        this.contents = contents;
    }

    public DeleteHistory delete(User user) throws CannotDeleteException {
        if (isNotOwner(user)) {
            throw new CannotDeleteException("다른 사람이 쓴 답변이 있어 삭제할 수 없습니다.");
        }

        this.deleted = true;
        return new DeleteHistory(ContentType.ANSWER, id, writeBy, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public User getWriteBy() {
        return writeBy;
    }

    public Question getQuestion() {
        return question;
    }

    public String getContents() {
        return contents;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", writerId=" + writeBy.getId() +
                ", questionId=" + question.getId() +
                ", contents='" + contents + '\'' +
                ", deleted=" + deleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Answer answer = (Answer) o;
        return Objects.equals(id, answer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void validateWriter(User user) throws CannotDeleteException {
        if (isNotOwner(user)) {
            throw new CannotDeleteException("다른 사람이 쓴 답변이 있어 삭제할 수 없습니다.");
        }
    }
}
