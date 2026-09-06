package com.healthinsurance.customer.mapper;

import com.healthinsurance.customer.dto.request.ContactRequest;
import com.healthinsurance.customer.dto.response.ContactResponse;
import com.healthinsurance.customer.entity.Contact;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-31T13:00:37+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class ContactMapperImpl implements ContactMapper {

    @Override
    public Contact toEntity(ContactRequest request) {
        if ( request == null ) {
            return null;
        }

        Contact contact = new Contact();

        contact.setContactType( request.getContactType() );
        contact.setContactValue( request.getContactValue() );
        contact.setPrimary( request.isPrimary() );

        return contact;
    }

    @Override
    public ContactResponse toResponse(Contact entity) {
        if ( entity == null ) {
            return null;
        }

        ContactResponse contactResponse = new ContactResponse();

        contactResponse.setContactId( entity.getContactId() );
        contactResponse.setContactType( entity.getContactType() );
        contactResponse.setContactValue( entity.getContactValue() );
        contactResponse.setPrimary( entity.isPrimary() );

        return contactResponse;
    }

    @Override
    public void updateEntityFromRequest(ContactRequest request, Contact entity) {
        if ( request == null ) {
            return;
        }

        entity.setContactType( request.getContactType() );
        entity.setContactValue( request.getContactValue() );
        entity.setPrimary( request.isPrimary() );
    }
}
