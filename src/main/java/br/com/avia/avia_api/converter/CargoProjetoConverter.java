package br.com.avia.avia_api.converter;

import br.com.avia.avia_api.enums.CargoProjeto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CargoProjetoConverter implements AttributeConverter<CargoProjeto, String> {

    @Override
    public String convertToDatabaseColumn(CargoProjeto attribute) {
        return attribute == null ? null : attribute.getNomeCargo();
    }

    @Override
    public CargoProjeto convertToEntityAttribute(String dbData) {
        return dbData == null ? null : CargoProjeto.deString(dbData);
    }
}
