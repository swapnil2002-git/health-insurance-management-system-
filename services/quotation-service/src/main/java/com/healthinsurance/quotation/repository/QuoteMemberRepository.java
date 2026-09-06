package com.healthinsurance.quotation.repository;

import com.healthinsurance.quotation.entity.QuoteMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface QuoteMemberRepository extends JpaRepository<QuoteMember, UUID> {}