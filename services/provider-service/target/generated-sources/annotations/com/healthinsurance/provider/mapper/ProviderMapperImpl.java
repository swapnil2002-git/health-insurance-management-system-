package com.healthinsurance.provider.mapper;

import com.healthinsurance.provider.dto.ProviderAddressRequest;
import com.healthinsurance.provider.dto.ProviderAddressResponse;
import com.healthinsurance.provider.dto.ProviderNetworkMappingResponse;
import com.healthinsurance.provider.dto.ProviderNetworkRequest;
import com.healthinsurance.provider.dto.ProviderNetworkResponse;
import com.healthinsurance.provider.dto.ProviderRequest;
import com.healthinsurance.provider.dto.ProviderResponse;
import com.healthinsurance.provider.entity.Provider;
import com.healthinsurance.provider.entity.ProviderAddress;
import com.healthinsurance.provider.entity.ProviderNetwork;
import com.healthinsurance.provider.entity.ProviderNetworkMapping;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-05T21:41:28+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class ProviderMapperImpl implements ProviderMapper {

    @Override
    public Provider toEntity(ProviderRequest request) {
        if ( request == null ) {
            return null;
        }

        Provider provider = new Provider();

        provider.setProviderName( request.getProviderName() );
        provider.setProviderType( request.getProviderType() );
        provider.setContactEmail( request.getContactEmail() );
        provider.setContactPhone( request.getContactPhone() );
        provider.setStatus( request.getStatus() );

        return provider;
    }

    @Override
    public ProviderResponse toResponse(Provider entity) {
        if ( entity == null ) {
            return null;
        }

        ProviderResponse.ProviderResponseBuilder providerResponse = ProviderResponse.builder();

        providerResponse.providerId( entity.getProviderId() );
        providerResponse.providerName( entity.getProviderName() );
        providerResponse.providerType( entity.getProviderType() );
        providerResponse.contactEmail( entity.getContactEmail() );
        providerResponse.contactPhone( entity.getContactPhone() );
        providerResponse.status( entity.getStatus() );
        providerResponse.addresses( toAddressResponseList( entity.getAddresses() ) );
        providerResponse.createdAt( entity.getCreatedAt() );
        providerResponse.updatedAt( entity.getUpdatedAt() );

        return providerResponse.build();
    }

    @Override
    public List<ProviderResponse> toProviderResponseList(List<Provider> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ProviderResponse> list = new ArrayList<ProviderResponse>( entities.size() );
        for ( Provider provider : entities ) {
            list.add( toResponse( provider ) );
        }

        return list;
    }

    @Override
    public ProviderAddress toEntity(ProviderAddressRequest request) {
        if ( request == null ) {
            return null;
        }

        ProviderAddress providerAddress = new ProviderAddress();

        providerAddress.setStreetAddress( request.getStreetAddress() );
        providerAddress.setCity( request.getCity() );
        providerAddress.setState( request.getState() );
        providerAddress.setPostalCode( request.getPostalCode() );
        providerAddress.setCountry( request.getCountry() );
        providerAddress.setPrimary( request.isPrimary() );

        return providerAddress;
    }

    @Override
    public ProviderAddressResponse toResponse(ProviderAddress entity) {
        if ( entity == null ) {
            return null;
        }

        ProviderAddressResponse.ProviderAddressResponseBuilder providerAddressResponse = ProviderAddressResponse.builder();

        providerAddressResponse.providerId( entityProviderProviderId( entity ) );
        providerAddressResponse.providerAddressId( entity.getProviderAddressId() );
        providerAddressResponse.streetAddress( entity.getStreetAddress() );
        providerAddressResponse.city( entity.getCity() );
        providerAddressResponse.state( entity.getState() );
        providerAddressResponse.postalCode( entity.getPostalCode() );
        providerAddressResponse.country( entity.getCountry() );
        providerAddressResponse.primary( entity.isPrimary() );
        providerAddressResponse.createdAt( entity.getCreatedAt() );
        providerAddressResponse.updatedAt( entity.getUpdatedAt() );

        return providerAddressResponse.build();
    }

    @Override
    public List<ProviderAddressResponse> toAddressResponseList(List<ProviderAddress> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ProviderAddressResponse> list = new ArrayList<ProviderAddressResponse>( entities.size() );
        for ( ProviderAddress providerAddress : entities ) {
            list.add( toResponse( providerAddress ) );
        }

        return list;
    }

    @Override
    public ProviderNetwork toEntity(ProviderNetworkRequest request) {
        if ( request == null ) {
            return null;
        }

        ProviderNetwork providerNetwork = new ProviderNetwork();

        providerNetwork.setNetworkName( request.getNetworkName() );
        providerNetwork.setDescription( request.getDescription() );
        providerNetwork.setStatus( request.getStatus() );

        return providerNetwork;
    }

    @Override
    public ProviderNetworkResponse toResponse(ProviderNetwork entity) {
        if ( entity == null ) {
            return null;
        }

        ProviderNetworkResponse.ProviderNetworkResponseBuilder providerNetworkResponse = ProviderNetworkResponse.builder();

        providerNetworkResponse.networkId( entity.getNetworkId() );
        providerNetworkResponse.networkName( entity.getNetworkName() );
        providerNetworkResponse.description( entity.getDescription() );
        providerNetworkResponse.status( entity.getStatus() );
        providerNetworkResponse.createdAt( entity.getCreatedAt() );
        providerNetworkResponse.updatedAt( entity.getUpdatedAt() );

        return providerNetworkResponse.build();
    }

    @Override
    public List<ProviderNetworkResponse> toNetworkResponseList(List<ProviderNetwork> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ProviderNetworkResponse> list = new ArrayList<ProviderNetworkResponse>( entities.size() );
        for ( ProviderNetwork providerNetwork : entities ) {
            list.add( toResponse( providerNetwork ) );
        }

        return list;
    }

    @Override
    public ProviderNetworkMappingResponse toResponse(ProviderNetworkMapping entity) {
        if ( entity == null ) {
            return null;
        }

        ProviderNetworkMappingResponse.ProviderNetworkMappingResponseBuilder providerNetworkMappingResponse = ProviderNetworkMappingResponse.builder();

        providerNetworkMappingResponse.providerId( entityProviderProviderId1( entity ) );
        providerNetworkMappingResponse.providerName( entityProviderProviderName( entity ) );
        providerNetworkMappingResponse.networkId( entityNetworkNetworkId( entity ) );
        providerNetworkMappingResponse.networkName( entityNetworkNetworkName( entity ) );
        providerNetworkMappingResponse.mappingId( entity.getMappingId() );
        providerNetworkMappingResponse.active( entity.isActive() );
        providerNetworkMappingResponse.joinedDate( entity.getJoinedDate() );
        providerNetworkMappingResponse.createdAt( entity.getCreatedAt() );
        providerNetworkMappingResponse.updatedAt( entity.getUpdatedAt() );

        return providerNetworkMappingResponse.build();
    }

    @Override
    public List<ProviderNetworkMappingResponse> toMappingResponseList(List<ProviderNetworkMapping> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ProviderNetworkMappingResponse> list = new ArrayList<ProviderNetworkMappingResponse>( entities.size() );
        for ( ProviderNetworkMapping providerNetworkMapping : entities ) {
            list.add( toResponse( providerNetworkMapping ) );
        }

        return list;
    }

    private UUID entityProviderProviderId(ProviderAddress providerAddress) {
        if ( providerAddress == null ) {
            return null;
        }
        Provider provider = providerAddress.getProvider();
        if ( provider == null ) {
            return null;
        }
        UUID providerId = provider.getProviderId();
        if ( providerId == null ) {
            return null;
        }
        return providerId;
    }

    private UUID entityProviderProviderId1(ProviderNetworkMapping providerNetworkMapping) {
        if ( providerNetworkMapping == null ) {
            return null;
        }
        Provider provider = providerNetworkMapping.getProvider();
        if ( provider == null ) {
            return null;
        }
        UUID providerId = provider.getProviderId();
        if ( providerId == null ) {
            return null;
        }
        return providerId;
    }

    private String entityProviderProviderName(ProviderNetworkMapping providerNetworkMapping) {
        if ( providerNetworkMapping == null ) {
            return null;
        }
        Provider provider = providerNetworkMapping.getProvider();
        if ( provider == null ) {
            return null;
        }
        String providerName = provider.getProviderName();
        if ( providerName == null ) {
            return null;
        }
        return providerName;
    }

    private UUID entityNetworkNetworkId(ProviderNetworkMapping providerNetworkMapping) {
        if ( providerNetworkMapping == null ) {
            return null;
        }
        ProviderNetwork network = providerNetworkMapping.getNetwork();
        if ( network == null ) {
            return null;
        }
        UUID networkId = network.getNetworkId();
        if ( networkId == null ) {
            return null;
        }
        return networkId;
    }

    private String entityNetworkNetworkName(ProviderNetworkMapping providerNetworkMapping) {
        if ( providerNetworkMapping == null ) {
            return null;
        }
        ProviderNetwork network = providerNetworkMapping.getNetwork();
        if ( network == null ) {
            return null;
        }
        String networkName = network.getNetworkName();
        if ( networkName == null ) {
            return null;
        }
        return networkName;
    }
}
