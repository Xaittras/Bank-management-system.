package javarax.dto;

import org.mapstruct.Mapper;

import javarax.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
	UserDto toDto(User user);
}
