package service;

import dto.PagedResult;
import dto.PollOptionReadFeedDTO;
import dto.PollReadFeedDTO;
import dto.PollReadSearchResultDTO;
import entities.PollRead;
import entities.PollVotesRead;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestScoped
public class PollReadService {

    @PersistenceContext
    EntityManager em;

    public PagedResult<PollReadFeedDTO> getPollReadFeed(int page, int size, String sort, Long userId) {

        String orderBy = "old".equalsIgnoreCase(sort)
                ? "ORDER BY p.createdAt ASC"
                : "ORDER BY p.createdAt DESC";

        String countQueryStr = "SELECT COUNT(p) FROM PollRead p WHERE p.active = true";
        long total = em.createQuery(countQueryStr, Long.class).getSingleResult();

        if (total == 0) {
            return new PagedResult<>(List.of(), total, page, size, false);
        }

        List<Long> pollIds = em.createQuery(
                        "SELECT p.id FROM PollRead p WHERE p.active = true " + orderBy, Long.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        if (pollIds.isEmpty()) {
            return new PagedResult<>(List.of(), total, page, size, false);
        }

        List<PollRead> polls = PollRead.find(
                "SELECT DISTINCT p FROM PollRead p LEFT JOIN FETCH p.options WHERE p.id IN ?1",
                pollIds).list();

        List<PollVotesRead> userVotes = PollVotesRead.find(
                "userId = ?1 AND option.poll.id IN ?2", userId, pollIds).list();

        Map<Long, Long> voteMap = userVotes.stream()
                .collect(Collectors.toMap(v -> v.option.poll.id, v -> v.option.id, (a, b) -> a));

        Map<Long, PollRead> pollMap = polls.stream()
                .collect(Collectors.toMap(p -> p.id, p -> p));

        List<PollReadFeedDTO> content = pollIds.stream()
                .map(id -> {
                    PollRead poll = pollMap.get(id);
                    if (poll == null) return null;
                    List<PollOptionReadFeedDTO> options = poll.options.stream()
                            .map(opt -> new PollOptionReadFeedDTO(opt.id, opt.text, opt.displayOrder, opt.totalVotes, opt.percentage))
                            .collect(Collectors.toList());
                    return new PollReadFeedDTO(poll.id, poll.title, poll.category, poll.authorId,
                            poll.authorName, poll.authorGradient, poll.authorAvatarUrl,
                            poll.createdAt, poll.active, poll.totalVotes, voteMap.get(poll.id), options);
                })
                .collect(Collectors.toList());

        boolean hasNext = (long) (page + 1) * size < total;
        return new PagedResult<>(content, total, page, size, hasNext);
    }

    public PollReadFeedDTO findById(Long pollId, Long userId) {
        PollRead poll = PollRead.find(
                "SELECT DISTINCT p FROM PollRead p LEFT JOIN FETCH p.options WHERE p.id = ?1",
                pollId).firstResult();

        if (poll == null) throw new NotFoundException("Poll não encontrada");

        PollVotesRead userVote = PollVotesRead.find(
                "userId = ?1 AND option.poll.id = ?2", userId, pollId).firstResult();

        List<PollOptionReadFeedDTO> options = poll.options.stream()
                .map(opt -> new PollOptionReadFeedDTO(opt.id, opt.text, opt.displayOrder, opt.totalVotes, opt.percentage))
                .collect(Collectors.toList());

        return new PollReadFeedDTO(poll.id, poll.title, poll.category, poll.authorId,
                poll.authorName, poll.authorGradient, poll.authorAvatarUrl,
                poll.createdAt, poll.active, poll.totalVotes,
                userVote != null ? userVote.option.id : null, options);
    }

    public List<PollReadSearchResultDTO> search(String query, int size) {
        String pattern = "%" + query.trim().toLowerCase() + "%";

        List<Long> pollIds = em.createQuery(
                        "SELECT p.id FROM PollRead p WHERE LOWER(p.title) LIKE :q " +
                                "OR LOWER(p.authorName) LIKE :q OR LOWER(p.category) LIKE :q " +
                                "ORDER BY p.createdAt DESC", Long.class)
                .setParameter("q", pattern)
                .setMaxResults(size)
                .getResultList();

        if (pollIds.isEmpty()) return List.of();

        return em.createQuery(
                        "SELECT p FROM PollRead p WHERE p.id IN :ids ORDER BY p.createdAt DESC", PollRead.class)
                .setParameter("ids", pollIds)
                .getResultList()
                .stream()
                .map(p -> new PollReadSearchResultDTO(
                        p.id, p.title, p.category, p.active,
                        p.totalVotes, p.authorName, p.authorGradient, p.authorAvatarUrl))
                .collect(Collectors.toList());
    }

    public PagedResult<PollReadFeedDTO> getMyPolls(int page, int size, Long userId) {
        // 1. Contagem
        String countQueryStr = "SELECT COUNT(p) FROM PollRead p WHERE p.authorId = :userId";
        TypedQuery<Long> countQuery = em.createQuery(countQueryStr, Long.class)
                .setParameter("userId", userId);
        long total = countQuery.getSingleResult();

        if (total == 0) {
            return new PagedResult<>(List.of(), total, page, size, false);
        }

        String idQueryStr = "SELECT p.id FROM PollRead p WHERE p.authorId = :userId ORDER BY p.createdAt DESC";
        TypedQuery<Long> idQuery = em.createQuery(idQueryStr, Long.class)
                .setParameter("userId", userId)
                .setFirstResult(page * size)
                .setMaxResults(size);
        List<Long> pollIds = idQuery.getResultList();

        if (pollIds.isEmpty()) {
            return new PagedResult<>(List.of(), total, page, size, false);
        }

        List<PollRead> polls = PollRead.find(
                "SELECT DISTINCT p FROM PollRead p LEFT JOIN FETCH p.options WHERE p.id IN ?1",
                pollIds
        ).list();

        List<PollVotesRead> userVotes = PollVotesRead.find(
                "userId = ?1 AND option.poll.id IN ?2",
                userId, pollIds
        ).list();

        Map<Long, Long> voteMap = userVotes.stream()
                .collect(Collectors.toMap(
                        v -> v.option.poll.id,
                        v -> v.option.id,
                        (v1, v2) -> v1
                ));

        Map<Long, PollRead> pollMap = polls.stream()
                .collect(Collectors.toMap(p -> p.id, p -> p));

        List<PollReadFeedDTO> content = pollIds.stream()
                .map(id -> {
                    PollRead poll = pollMap.get(id);
                    if (poll == null) return null;

                    List<PollOptionReadFeedDTO> optionDTOs = poll.options.stream()
                            .map(opt -> new PollOptionReadFeedDTO(
                                    opt.id,
                                    opt.text,
                                    opt.displayOrder,
                                    opt.totalVotes,
                                    opt.percentage
                            ))
                            .collect(Collectors.toList());

                    return new PollReadFeedDTO(
                            poll.id,
                            poll.title,
                            poll.category,
                            poll.authorId,
                            poll.authorName,
                            poll.authorGradient,
                            poll.authorAvatarUrl,
                            poll.createdAt,
                            poll.active,
                            poll.totalVotes,
                            voteMap.get(poll.id),
                            optionDTOs
                    );
                })
                .collect(Collectors.toList());

        boolean hasNext = (long) (page + 1) * size < total;
        return new PagedResult<>(content, total, page, size, hasNext);
    }

    public PagedResult<PollReadFeedDTO> getMyVotes(int page, int size, Long userId) {
        String countQueryStr = "SELECT COUNT(DISTINCT p.id) FROM PollRead p " +
                "JOIN p.options o " +
                "JOIN o.votes v " +
                "WHERE v.userId = :userId";
        TypedQuery<Long> countQuery = em.createQuery(countQueryStr, Long.class)
                .setParameter("userId", userId);
        long total = countQuery.getSingleResult();

        if (total == 0) {
            return new PagedResult<>(List.of(), total, page, size, false);
        }

        String idQueryStr = "SELECT p.id, p.createdAt FROM PollRead p " +
                "JOIN p.options o " +
                "JOIN o.votes v " +
                "WHERE v.userId = :userId " +
                "ORDER BY p.createdAt DESC";
        TypedQuery<Object[]> idQuery = em.createQuery(idQueryStr, Object[].class)
                .setParameter("userId", userId)
                .setFirstResult(page * size)
                .setMaxResults(size);
        List<Object[]> results = idQuery.getResultList();
        List<Long> pollIds = results.stream()
                .map(row -> (Long) row[0])
                .collect(Collectors.toList());

        if (pollIds.isEmpty()) {
            return new PagedResult<>(List.of(), total, page, size, false);
        }

        List<PollRead> polls = PollRead.find(
                "SELECT DISTINCT p FROM PollRead p LEFT JOIN FETCH p.options WHERE p.id IN ?1",
                pollIds
        ).list();

        List<PollVotesRead> userVotes = PollVotesRead.find(
                "userId = ?1 AND option.poll.id IN ?2",
                userId, pollIds
        ).list();

        Map<Long, Long> voteMap = userVotes.stream()
                .collect(Collectors.toMap(
                        v -> v.option.poll.id,
                        v -> v.option.id,
                        (v1, v2) -> v1
                ));

        Map<Long, PollRead> pollMap = polls.stream()
                .collect(Collectors.toMap(p -> p.id, p -> p));

        List<PollReadFeedDTO> content = pollIds.stream()
                .map(id -> {
                    PollRead poll = pollMap.get(id);
                    if (poll == null) return null;

                    List<PollOptionReadFeedDTO> optionDTOs = poll.options.stream()
                            .map(opt -> new PollOptionReadFeedDTO(
                                    opt.id,
                                    opt.text,
                                    opt.displayOrder,
                                    opt.totalVotes,
                                    opt.percentage
                            ))
                            .collect(Collectors.toList());

                    return new PollReadFeedDTO(
                            poll.id,
                            poll.title,
                            poll.category,
                            poll.authorId,
                            poll.authorName,
                            poll.authorGradient,
                            poll.authorAvatarUrl,
                            poll.createdAt,
                            poll.active,
                            poll.totalVotes,
                            voteMap.get(poll.id),
                            optionDTOs
                    );
                })
                .collect(Collectors.toList());

        boolean hasNext = (long) (page + 1) * size < total;
        return new PagedResult<>(content, total, page, size, hasNext);
    }
}