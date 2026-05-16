package com.yunus.customer;

import com.yunus.customer.dto.CustomerCreateRequest;
import com.yunus.customer.dto.CustomerResponse;
import com.yunus.tag.TagMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = TagMapper.class)
public interface CustomerMapper {
    Customer toEntity(CustomerCreateRequest request);

    CustomerResponse toResponse(Customer customer);













}
