package com.example.admin.mapper;

import com.example.admin.model.AppUser;
import com.example.admin.model.FamilyBinding;
import com.example.admin.model.FamilyMember;
import com.example.admin.model.MedicineProfile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface FamilySafetyAdminMapper {

    @Select("""
            select fm.id, fm.name, fm.phone, fm.email, fm.created_at, fm.updated_at
            from family_member fm
            where lower(fm.name) = lower(#{name})
              and ((#{phone} is null and fm.phone is null) or fm.phone = #{phone})
            limit 1
            """)
    FamilyMember findFamilyMember(@Param("name") String name, @Param("phone") String phone);

    @Select("""
            select fm.id, fm.name, fm.phone, fm.email, fm.created_at, fm.updated_at
            from family_member fm
            where fm.id = #{id}
            limit 1
            """)
    FamilyMember findFamilyMemberById(@Param("id") Long id);

    @Insert("""
            insert into family_member(name, phone, email, created_at, updated_at)
            values(#{name}, #{phone}, #{email}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertFamilyMember(FamilyMember familyMember);

    @Select("""
            select count(1)
            from family_binding
            where family_member_id = #{familyMemberId}
              and user_id = #{userId}
            """)
    int countBinding(@Param("familyMemberId") Long familyMemberId, @Param("userId") Long userId);

    @Insert("""
            insert into family_binding(family_member_id, user_id, relationship, status, created_at)
            values(#{familyMemberId}, #{userId}, #{relationship}, #{status}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertBinding(FamilyBinding binding);

    @Select("""
            <script>
            select fb.id,
                   fb.family_member_id,
                   fm.name as family_name,
                   fm.phone as family_phone,
                   fm.email as family_email,
                   fb.user_id,
                   u.username,
                   u.nickname,
                   fb.relationship,
                   fb.status,
                   fb.created_at
            from family_binding fb
            join family_member fm on fm.id = fb.family_member_id
            join `user` u on u.id = fb.user_id
            <where>
                <if test="keyword != null and keyword != ''">
                    (
                        fm.name like concat('%', #{keyword}, '%')
                        or fm.phone like concat('%', #{keyword}, '%')
                        or u.username like concat('%', #{keyword}, '%')
                        or u.nickname like concat('%', #{keyword}, '%')
                    )
                </if>
            </where>
            order by fb.created_at desc, fb.id desc
            </script>
            """)
    List<Map<String, Object>> findBindings(@Param("keyword") String keyword);

    @Select("select id, username, password_hash, nickname, phone, email, status, last_login_at, created_at, updated_at from `user` where id = #{id} limit 1")
    AppUser findUserById(@Param("id") Long id);

    @Select("""
            <script>
            select mp.id,
                   mp.user_id,
                   u.username,
                   u.nickname,
                   mp.family_member_id,
                   fm.name as family_name,
                   mp.medicine_name,
                   mp.generic_name,
                   mp.description,
                   mp.dosage_usage,
                   mp.suitable_people,
                   mp.contraindications,
                   mp.expiry_date,
                   mp.barcode_or_alias,
                   mp.created_at,
                   mp.updated_at
            from medicine_profile mp
            join `user` u on u.id = mp.user_id
            left join family_member fm on fm.id = mp.family_member_id
            <where>
                <if test="userId != null">
                    mp.user_id = #{userId}
                </if>
                <if test="familyMemberId != null">
                    and mp.family_member_id = #{familyMemberId}
                </if>
                <if test="keyword != null and keyword != ''">
                    and (
                        mp.medicine_name like concat('%', #{keyword}, '%')
                        or mp.generic_name like concat('%', #{keyword}, '%')
                        or mp.barcode_or_alias like concat('%', #{keyword}, '%')
                    )
                </if>
            </where>
            order by mp.updated_at desc, mp.id desc
            </script>
            """)
    List<Map<String, Object>> findMedicineProfiles(@Param("userId") Long userId,
                                                   @Param("familyMemberId") Long familyMemberId,
                                                   @Param("keyword") String keyword);

    @Select("""
            select mp.id,
                   mp.user_id,
                   u.username,
                   u.nickname,
                   mp.family_member_id,
                   fm.name as family_name,
                   mp.medicine_name,
                   mp.generic_name,
                   mp.description,
                   mp.dosage_usage,
                   mp.suitable_people,
                   mp.contraindications,
                   mp.expiry_date,
                   mp.barcode_or_alias,
                   mp.created_at,
                   mp.updated_at
            from medicine_profile mp
            join `user` u on u.id = mp.user_id
            left join family_member fm on fm.id = mp.family_member_id
            where mp.id = #{id}
            limit 1
            """)
    Map<String, Object> findMedicineProfileDetailById(@Param("id") Long id);

    @Select("""
            select id, user_id, family_member_id, medicine_name, generic_name, description, dosage_usage,
                   suitable_people, contraindications, expiry_date, barcode_or_alias, created_at, updated_at
            from medicine_profile
            where id = #{id}
            limit 1
            """)
    MedicineProfile findMedicineProfileById(@Param("id") Long id);

    @Insert("""
            insert into medicine_profile(user_id, family_member_id, medicine_name, generic_name, description, dosage_usage,
            suitable_people, contraindications, expiry_date, barcode_or_alias, created_at, updated_at)
            values(#{userId}, #{familyMemberId}, #{medicineName}, #{genericName}, #{description}, #{dosageUsage},
            #{suitablePeople}, #{contraindications}, #{expiryDate}, #{barcodeOrAlias}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMedicineProfile(MedicineProfile medicineProfile);

    @Update("""
            update medicine_profile
            set user_id = #{userId},
                family_member_id = #{familyMemberId},
                medicine_name = #{medicineName},
                generic_name = #{genericName},
                description = #{description},
                dosage_usage = #{dosageUsage},
                suitable_people = #{suitablePeople},
                contraindications = #{contraindications},
                expiry_date = #{expiryDate},
                barcode_or_alias = #{barcodeOrAlias},
                updated_at = now()
            where id = #{id}
            """)
    int updateMedicineProfile(MedicineProfile medicineProfile);

    @Delete("delete from medicine_profile where id = #{id}")
    int deleteMedicineProfile(@Param("id") Long id);

    @Select("""
            <script>
            select sr.id as record_id,
                   sr.user_id,
                   u.username,
                   u.nickname,
                   sr.mode,
                   sr.trigger_command,
                   sr.scene,
                   sr.location_text,
                   sr.captured_at,
                   si.name as core_item,
                   msa.alert_type,
                   msa.alert_message
            from family_binding fb
            join `user` u on u.id = fb.user_id
            join scan_record sr on sr.user_id = fb.user_id
            left join scan_item si on si.record_id = sr.id and si.is_primary = 1
            left join (
                select a1.scan_record_id, a1.alert_type, a1.alert_message
                from medicine_scan_alert a1
                join (
                    select scan_record_id, max(id) as latest_id
                    from medicine_scan_alert
                    group by scan_record_id
                ) a2 on a2.latest_id = a1.id
            ) msa on msa.scan_record_id = sr.id
            where fb.family_member_id = #{familyMemberId}
              and fb.status = 'ACTIVE'
            <if test="userId != null">
              and fb.user_id = #{userId}
            </if>
            <if test="mode != null and mode != ''">
              and sr.mode = #{mode}
            </if>
            <if test="start != null">
              and sr.captured_at &gt;= #{start}
            </if>
            <if test="end != null">
              and sr.captured_at &lt;= #{end}
            </if>
            order by sr.captured_at desc, sr.id desc
            limit 100
            </script>
            """)
    List<Map<String, Object>> findSharedLogs(@Param("familyMemberId") Long familyMemberId,
                                             @Param("userId") Long userId,
                                             @Param("mode") String mode,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}
