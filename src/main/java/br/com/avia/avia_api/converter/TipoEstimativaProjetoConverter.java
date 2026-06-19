package br.com.avia.avia_api.converter;

import br.com.avia.avia_api.enums.TipoEstimativaProjeto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoEstimativaProjetoConverter implements AttributeConverter<TipoEstimativaProjeto, String> {

    @Override
    public String convertToDatabaseColumn(TipoEstimativaProjeto attribute) {
        return attribute == null ? null : attribute.getEstimativaProjeto();
    }

    @Override
    public TipoEstimativaProjeto convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TipoEstimativaProjeto.deString(dbData);
    }
}
