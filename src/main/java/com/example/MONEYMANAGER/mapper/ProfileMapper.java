package com.example.MONEYMANAGER.mapper;

import com.example.MONEYMANAGER.dto.ProfileDto;
import com.example.MONEYMANAGER.entity.ProfileEntity;
import org.springframework.stereotype.Service;

@Service
public class ProfileMapper {

    public ProfileDto toDto(ProfileEntity profileEntity) {
        return ProfileDto.builder()
                .id(profileEntity.getId())
                .fullname(profileEntity.getFullname())
                .email(profileEntity.getEmail())
                .image(profileEntity.getImage())
                .createdDate(profileEntity.getCreatedDate())
                .updatedDate(profileEntity.getUpdatedDate())
                .build();
    }

    public ProfileEntity toEntity(ProfileDto profileDto) {
        return ProfileEntity.builder()
                .id(profileDto.getId())
                .fullname(profileDto.getFullname())
                .email(profileDto.getEmail())
                .password(profileDto.getPassword())
                .image(profileDto.getImage())
                .createdDate(profileDto.getCreatedDate())
                .updatedDate(profileDto.getUpdatedDate())
                .build();
    }
}
