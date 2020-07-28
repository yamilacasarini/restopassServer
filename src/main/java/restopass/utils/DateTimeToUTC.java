package restopass.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.LocalDateTime;


@WritingConverter
public class DateTimeToUTC implements Converter<LocalDateTime, LocalDateTime> {

    @Override
    public LocalDateTime convert(LocalDateTime date) {
        return date;
    }
}