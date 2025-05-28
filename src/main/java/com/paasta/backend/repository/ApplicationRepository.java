package com.paasta.backend.repository;

import com.paasta.backend.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    // 사번으로 신청서 조회
    List<Application> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);
    
    // 상태별 신청서 조회
    List<Application> findByStatusOrderByCreatedAtDesc(Application.Status status);
    
    // 사번과 상태로 신청서 조회
    List<Application> findByEmployeeIdAndStatusOrderByCreatedAtDesc(String employeeId, Application.Status status);
    
    // 전체 신청서를 최신순으로 조회 (관리자용)
    List<Application> findAllByOrderByCreatedAtDesc();
    
    // 삭제되지 않은 신청서만 조회
    @Query("SELECT a FROM Application a WHERE a.status != 'DELETED' ORDER BY a.createdAt DESC")
    List<Application> findAllNotDeleted();
    
    // 특정 사용자의 삭제되지 않은 신청서만 조회
    @Query("SELECT a FROM Application a WHERE a.employeeId = :employeeId AND a.status != 'DELETED' ORDER BY a.createdAt DESC")
    List<Application> findByEmployeeIdNotDeleted(@Param("employeeId") String employeeId);
    
    // ✅ 중복 확인용 - 같은 사용자, 같은 제목, 특정 시간 이후 신청서 조회
    @Query("SELECT a FROM Application a WHERE a.employeeId = :employeeId AND a.title = :title AND a.createdAt > :after")
    List<Application> findByEmployeeIdAndTitleAndCreatedAtAfter(
            @Param("employeeId") String employeeId, 
            @Param("title") String title, 
            @Param("after") LocalDateTime after
    );
}