package com.example.demo.mapper;

import com.example.demo.model.FamilyUserBinding;
import com.example.demo.model.UserCareProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FamilyMapper {

    @Select("""
            select id, family_user_id, vision_user_id, relationship, created_at
            from family_user_binding
            where family_user_id = #{familyUserId}
            order by created_at asc, id asc
            """)
    List<FamilyUserBinding> findBindingsByFamilyUserId(@Param("familyUserId") Long familyUserId);

    @Select("""
            select id, family_user_id, vision_user_id, relationship, created_at
            from family_user_binding
            where family_user_id = #{familyUserId}
              and vision_user_id = #{visionUserId}
            limit 1
            """)
    FamilyUserBinding findBindingByFamilyAndVisionUserId(@Param("familyUserId") Long familyUserId,
                                                         @Param("visionUserId") Long visionUserId);

    @Insert("""
            insert into family_user_binding(family_user_id, vision_user_id, relationship, created_at)
            values(#{familyUserId}, #{visionUserId}, #{relationship}, now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertBinding(FamilyUserBinding binding);

    @Insert("""
            insert into family_user_binding(family_user_id, vision_user_id, relationship, created_at)
            values(#{familyUserId}, #{visionUserId}, #{relationship}, now())
            on duplicate key update
                relationship = values(relationship)
            """)
    int upsertBinding(FamilyUserBinding binding);

    @Select("""
            select id, user_id, subject_name, gender, age, vision_level, address, notes,
                   emergency_name, emergency_relation, emergency_phone, emergency_backup_phone,
                   medicine, disease_note, allergy, reminder, created_at, updated_at
            from user_care_profile
            where user_id = #{userId}
            limit 1
            """)
    UserCareProfile findProfileByUserId(@Param("userId") Long userId);

    @Insert("""
            insert into user_care_profile(
                user_id, subject_name, gender, age, vision_level, address, notes,
                emergency_name, emergency_relation, emergency_phone, emergency_backup_phone,
                medicine, disease_note, allergy, reminder, created_at, updated_at
            ) values (
                #{userId}, #{subjectName}, #{gender}, #{age}, #{visionLevel}, #{address}, #{notes},
                #{emergencyName}, #{emergencyRelation}, #{emergencyPhone}, #{emergencyBackupPhone},
                #{medicine}, #{diseaseNote}, #{allergy}, #{reminder}, now(), now()
            )
            on duplicate key update
                subject_name = values(subject_name),
                gender = values(gender),
                age = values(age),
                vision_level = values(vision_level),
                address = values(address),
                notes = values(notes),
                emergency_name = values(emergency_name),
                emergency_relation = values(emergency_relation),
                emergency_phone = values(emergency_phone),
                emergency_backup_phone = values(emergency_backup_phone),
                medicine = values(medicine),
                disease_note = values(disease_note),
                allergy = values(allergy),
                reminder = values(reminder),
                updated_at = now()
            """)
    int upsertProfile(UserCareProfile profile);
}
