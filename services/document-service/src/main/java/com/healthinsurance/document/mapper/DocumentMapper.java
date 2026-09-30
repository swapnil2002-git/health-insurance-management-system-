package com.healthinsurance.document.mapper;

import com.healthinsurance.document.dto.DocumentResponse;
import com.healthinsurance.document.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DocumentMapper {

    DocumentResponse toResponse(Document document);

    List<DocumentResponse> toResponseList(List<Document> documents);
}
