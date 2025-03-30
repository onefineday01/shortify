package com.onefineday01.shortify.repository;

import com.onefineday01.shortify.entity.Url;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import static org.hibernate.grammars.hql.HqlParser.CURRENT_TIMESTAMP;

public interface UrlRepository extends JpaRepository<Url, Long> {
    public Url findByShortCode(String shortCode);


    @Modifying
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    int incrementClickCount(@Param("shortCode") String shortCode);
}



