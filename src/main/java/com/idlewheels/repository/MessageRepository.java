package com.idlewheels.repository;

import com.idlewheels.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @EntityGraph(attributePaths = {"listing", "sender", "receiver"})
    @Query("select m from Message m where m.sender.id = :userId or m.receiver.id = :userId order by m.sentAt desc, m.id desc")
    List<Message> findAllForUser(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"listing", "sender", "receiver"})
    @Query("select m from Message m where m.listing.id = :listingId and ((m.sender.id = :firstUserId and m.receiver.id = :secondUserId) or (m.sender.id = :secondUserId and m.receiver.id = :firstUserId)) order by m.sentAt asc, m.id asc")
    List<Message> findConversation(
            @Param("listingId") Long listingId,
            @Param("firstUserId") Long firstUserId,
            @Param("secondUserId") Long secondUserId
    );

    @Query("select count(m) from Message m where m.receiver.id = :userId and m.isRead = false")
    long countUnreadForUser(@Param("userId") Long userId);

}
