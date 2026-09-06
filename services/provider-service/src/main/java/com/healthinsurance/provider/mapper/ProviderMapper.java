package com.healthinsurance.provider.mapper;

import com.healthinsurance.provider.dto.*;
import com.healthinsurance.provider.entity.Provider;
import com.healthinsurance.provider.entity.ProviderAddress;
import com.healthinsurance.provider.entity.ProviderNetwork;
import com.healthinsurance.provider.entity.ProviderNetworkMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProviderMapper {

    // Provider mappings
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "networkMappings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Provider toEntity(ProviderRequest request);

    ProviderResponse toResponse(Provider entity);

    List<ProviderResponse> toProviderResponseList(List<Provider> entities);

    // Provider Address mappings
    @Mapping(target = "providerAddressId", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProviderAddress toEntity(ProviderAddressRequest request);

    @Mapping(target = "providerId", source = "provider.providerId")
    ProviderAddressResponse toResponse(ProviderAddress entity);

    List<ProviderAddressResponse> toAddressResponseList(List<ProviderAddress> entities);

    // Provider Network mappings
    @Mapping(target = "networkId", ignore = true)
    @Mapping(target = "mappings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProviderNetwork toEntity(ProviderNetworkRequest request);

    ProviderNetworkResponse toResponse(ProviderNetwork entity);

    List<ProviderNetworkResponse> toNetworkResponseList(List<ProviderNetwork> entities);

    // Provider Network Mapping mappings
    @Mapping(target = "providerId", source = "provider.providerId")
    @Mapping(target = "providerName", source = "provider.providerName")
    @Mapping(target = "networkId", source = "network.networkId")
    @Mapping(target = "networkName", source = "network.networkName")
    ProviderNetworkMappingResponse toResponse(ProviderNetworkMapping entity);

    List<ProviderNetworkMappingResponse> toMappingResponseList(List<ProviderNetworkMapping> entities);
}
