package restopass.utils;


import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@ReadingConverter
public class DateTimeWithZone implements Converter<LocalDateTime, LocalDateTime> {

    @Override
    public LocalDateTime convert(LocalDateTime date) {
        return date.atZone(ZoneOffset.ofHours(-3)).toLocalDateTime();
    }
}
