package com.example.demo.mapper;

import com.example.demo.model.MedicineProfile;
import com.example.demo.model.MedicineScanAlert;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MedicineSafetyMapper {

    @Select("""
            select id, user_id, family_member_id, medicine_name, generic_name, description, dosage_usage,
                   suitable_people, contraindications, expiry_date, barcode_or_alias, created_at, updated_at
            from medicine_profile
            where user_id = #{userId}
            order by updated_at desc, id desc
            """)
    List<MedicineProfile> findByUserId(@Param("userId") Long userId);

    @Insert("""
            insert into medicine_scan_alert(user_id, scan_record_id, medicine_profile_id, alert_type, alert_message, created_at)
            values(#{userId}, #{scanRecordId}, #{medicineProfileId}, #{alertType}, #{alertMessage}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAlert(MedicineScanAlert alert);

    @Select("""
            select count(1)
            from scan_record sr
            join scan_item si on si.record_id = sr.id
            where sr.user_id = #{userId}
              and sr.captured_at >= #{start}
              and sr.mode = 'analyze'
              and lower(si.name) like concat('%', lower(#{medicineName}), '%')
            """)
    int countRecentScanByMedicineName(@Param("userId") Long userId,
                                      @Param("medicineName") String medicineName,
                                      @Param("start") LocalDateTime start);
}
