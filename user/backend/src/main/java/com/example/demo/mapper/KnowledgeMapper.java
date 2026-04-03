package com.example.demo.mapper;

import com.example.demo.model.ScanItem;
import com.example.demo.model.ScanRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface KnowledgeMapper {

    @Insert("""
            insert into scan_record(user_id, session_id, mode, trigger_command, scene, narration, position_summary,
            captured_at, latitude, longitude, location_text, created_at)
            values(#{userId}, #{sessionId}, #{mode}, #{triggerCommand}, #{scene}, #{narration}, #{positionSummary},
            #{capturedAt}, #{latitude}, #{longitude}, #{locationText}, now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRecord(ScanRecord record);

    @Insert("""
            insert into scan_item(record_id, name, position, confidence, attributes_json, is_primary)
            values(#{recordId}, #{name}, #{position}, #{confidence}, #{attributesJson}, #{isPrimary})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertItem(ScanItem item);

    @Select("""
            select id, user_id, session_id, mode, trigger_command, scene, narration, position_summary,
                   captured_at, latitude, longitude, location_text, created_at
            from scan_record
            where user_id = #{userId}
            order by captured_at desc, id desc
            limit #{limit}
            """)
    List<ScanRecord> findRecentRecords(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("""
            select id, user_id, session_id, mode, trigger_command, scene, narration, position_summary,
                   captured_at, latitude, longitude, location_text, created_at
            from scan_record
            where user_id = #{userId} and session_id = #{sessionId}
            order by captured_at desc, id desc
            limit #{limit}
            """)
    List<ScanRecord> findRecordsBySession(@Param("userId") Long userId, @Param("sessionId") String sessionId, @Param("limit") int limit);

    @Select("""
            select id, user_id, session_id, mode, trigger_command, scene, narration, position_summary,
                   captured_at, latitude, longitude, location_text, created_at
            from scan_record
            where user_id = #{userId}
              and captured_at between #{start} and #{end}
            order by captured_at desc, id desc
            """)
    List<ScanRecord> findRecordsBetween(@Param("userId") Long userId,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    @Select("""
            select id, record_id, name, position, confidence, attributes_json, is_primary
            from scan_item
            where record_id = #{recordId}
            order by is_primary desc, confidence desc, id asc
            """)
    List<ScanItem> findItemsByRecordId(@Param("recordId") Long recordId);

    @Select("""
            select count(1)
            from scan_record
            where user_id = #{userId}
              and captured_at between #{start} and #{end}
            """)
    int countRecordsBetween(@Param("userId") Long userId,
                            @Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end);

    @Select("""
            select session_id
            from scan_record
            where user_id = #{userId}
            order by captured_at desc, id desc
            limit 1
            """)
    String findLatestSessionId(@Param("userId") Long userId);

    @Select("""
            select count(1)
            from scan_record
            where user_id = #{userId}
              and session_id = #{sessionId}
              and mode = #{mode}
            """)
    int countBySessionAndMode(@Param("userId") Long userId,
                              @Param("sessionId") String sessionId,
                              @Param("mode") String mode);
}
